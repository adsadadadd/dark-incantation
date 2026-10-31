package me.x1no4ekk.core.warp;

import me.x1no4ekk.core.CorePlugin;
import me.x1no4ekk.core.teleport.TeleportOptions;
import me.x1no4ekk.core.util.Durations;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.logging.Level;

/**
 * All warps cached in memory as immutable records; MySQL is written through asynchronously on the serial writer.
 * The cache is updated optimistically and rolled back when a write fails. Mutations happen on the main thread.
 */
public final class WarpManager {

    public enum CreateResult {
        CREATED, NOT_LOADED, INVALID_NAME, ALREADY_EXISTS, LIMIT_REACHED, DISABLED_WORLD, SPECTATOR
    }

    public static final String UNLIMITED_PERMISSION = "core.warp.unlimited";
    public static final String LIMIT_PERMISSION_PREFIX = "core.warp.limit.";

    private static final long RETRY_DELAY_TICKS = 20L * 60L;

    private final CorePlugin plugin;
    private final Supplier<WarpSettings> settings;
    private final Map<String, Warp> warps = new ConcurrentHashMap<>();
    private final Map<String, WarpRating> ratings = new ConcurrentHashMap<>();
    private volatile boolean loaded;
    private boolean running;
    private BukkitTask retryTask;

    WarpManager(CorePlugin plugin, Supplier<WarpSettings> settings) {
        this.plugin = plugin;
        this.settings = settings;
    }

    // ---------------------------------------------------------------- lifecycle

    void start() {
        running = true;
        load();
    }

    private void load() {
        retryTask = null;
        plugin.database().query(WarpRepository::loadAll).whenCompleteAsync((snapshot, error) -> {
            if (!running) {
                return;
            }
            if (error != null) {
                plugin.getLogger().log(Level.SEVERE, "Failed to load warps; retrying in a minute", error);
                retryTask = Bukkit.getScheduler().runTaskLater(plugin, this::load, RETRY_DELAY_TICKS);
                return;
            }
            warps.clear();
            ratings.clear();
            for (Warp warp : snapshot.warps()) {
                warps.put(warp.key(), warp);
            }
            ratings.putAll(snapshot.ratings());
            loaded = true;
            plugin.getLogger().info("Loaded " + warps.size() + " warp(s).");
        }, plugin.mainThread());
    }

    void shutdown() {
        running = false;
        if (retryTask != null) {
            retryTask.cancel();
            retryTask = null;
        }
        loaded = false;
        warps.clear();
        ratings.clear();
    }

    public boolean isLoaded() {
        return loaded;
    }

    // ---------------------------------------------------------------- queries

    public Warp get(String name) {
        return warps.get(Warp.key(name));
    }

    public WarpRating rating(String key) {
        return ratings.getOrDefault(key, WarpRating.NONE);
    }

    public List<Warp> find(Predicate<Warp> filter) {
        List<Warp> result = new ArrayList<>();
        for (Warp warp : warps.values()) {
            if (filter.test(warp)) {
                result.add(warp);
            }
        }
        result.sort(Comparator.comparing(Warp::name, String.CASE_INSENSITIVE_ORDER));
        return result;
    }

    public List<Warp> systemWarps() {
        return find(Warp::system);
    }

    public List<Warp> playerWarps() {
        return find(warp -> !warp.system());
    }

    public List<Warp> ownedBy(UUID uuid) {
        return find(warp -> !warp.system() && warp.isOwnedBy(uuid));
    }

    public int countOwned(UUID uuid) {
        int count = 0;
        for (Warp warp : warps.values()) {
            if (!warp.system() && warp.isOwnedBy(uuid)) {
                count++;
            }
        }
        return count;
    }

    /**
     * Highest limit among {@code core.warp.limit.<group>}; {@code core.warp.unlimited} removes the cap.
     */
    public int limit(Player player) {
        if (player.hasPermission(UNLIMITED_PERMISSION)) {
            return Integer.MAX_VALUE;
        }
        WarpSettings config = settings.get();
        int limit = config.defaultLimit();
        for (Map.Entry<String, Integer> entry : config.limits().entrySet()) {
            if (entry.getValue() > limit && player.hasPermission(LIMIT_PERMISSION_PREFIX + entry.getKey())) {
                limit = entry.getValue();
            }
        }
        return limit;
    }

    public boolean isValidName(String name) {
        return name.length() <= Warp.MAX_NAME_LENGTH && settings.get().namePattern().matcher(name).matches();
    }

    // ---------------------------------------------------------------- mutations

    /**
     * Creates a player warp at the player's position.
     *
     * @param onFailure runs on the main thread if the database write fails (cache already rolled back)
     */
    public CreateResult create(Player player, String name, String description, Consumer<Throwable> onFailure) {
        CreateResult check = validateNew(player, name);
        if (check != null) {
            return check;
        }
        UUID uuid = player.getUniqueId();
        if (countOwned(uuid) >= limit(player)) {
            return CreateResult.LIMIT_REACHED;
        }
        insert(Warp.create(name, uuid, player.getName(), false, player.getLocation(), null, description, -1), onFailure);
        return CreateResult.CREATED;
    }

    /**
     * Creates a system warp (no owner, no limit) at the player's position.
     */
    public CreateResult createSystem(Player player, String name, Material icon, int slot, Consumer<Throwable> onFailure) {
        CreateResult check = validateNew(player, name);
        if (check != null && check != CreateResult.DISABLED_WORLD) {
            return check;
        }
        insert(Warp.create(name, null, null, true, player.getLocation(), icon, null, slot), onFailure);
        return CreateResult.CREATED;
    }

    private CreateResult validateNew(Player player, String name) {
        if (!loaded) {
            return CreateResult.NOT_LOADED;
        }
        if (!isValidName(name)) {
            return CreateResult.INVALID_NAME;
        }
        if (player.getGameMode() == GameMode.SPECTATOR) {
            return CreateResult.SPECTATOR;
        }
        if (warps.containsKey(Warp.key(name))) {
            return CreateResult.ALREADY_EXISTS;
        }
        if (isDisabledWorld(player.getLocation())) {
            return CreateResult.DISABLED_WORLD;
        }
        return null;
    }

    public boolean isDisabledWorld(Location location) {
        return settings.get().disabledWorlds().contains(location.getWorld().getName().toLowerCase(Locale.ROOT));
    }

    private void insert(Warp warp, Consumer<Throwable> onFailure) {
        warps.put(warp.key(), warp);
        ratings.remove(warp.key());
        plugin.database().update(connection -> WarpRepository.insert(connection, warp))
                .whenCompleteAsync((ignored, error) -> {
                    if (error != null) {
                        if (!isDuplicate(error)) {
                            plugin.getLogger().log(Level.SEVERE, "Failed to save warp '" + warp.name() + "'", error);
                        }
                        warps.remove(warp.key(), warp);
                        onFailure.accept(error);
                    }
                }, plugin.mainThread());
    }

    /**
     * Removes a warp and its grades.
     */
    public void delete(Warp warp, Consumer<Throwable> onFailure) {
        String key = warp.key();
        Warp removed = warps.remove(key);
        if (removed == null) {
            return;
        }
        WarpRating rating = ratings.remove(key);
        plugin.database().updateInTransaction(connection -> WarpRepository.delete(connection, removed.name()))
                .whenCompleteAsync((ignored, error) -> {
                    if (error != null) {
                        plugin.getLogger().log(Level.SEVERE, "Failed to delete warp '" + removed.name() + "'", error);
                        if (warps.putIfAbsent(key, removed) == null && rating != null) {
                            ratings.putIfAbsent(key, rating);
                        }
                        onFailure.accept(error);
                    }
                }, plugin.mainThread());
    }

    /**
     * Replaces the location, icon, description or slot of a warp.
     */
    public void update(Warp before, Warp after, Consumer<Throwable> onFailure) {
        String key = after.key();
        warps.put(key, after);
        plugin.database().update(connection -> WarpRepository.updateDetails(connection, after))
                .whenCompleteAsync((ignored, error) -> {
                    if (error != null) {
                        plugin.getLogger().log(Level.SEVERE, "Failed to update warp '" + after.name() + "'", error);
                        warps.replace(key, after, before);
                        onFailure.accept(error);
                    }
                }, plugin.mainThread());
    }

    // ---------------------------------------------------------------- teleport & grades

    /**
     * Teleports with warmup; the destination is the warp's position when the warmup ends.
     * A successful visit is counted asynchronously.
     */
    public void teleport(Player player, Warp warp) {
        if (warp.toLocation() == null) {
            plugin.messages().send(player, "warp.world-unavailable", Placeholder.unparsed("warp", warp.name()));
            return;
        }
        String key = warp.key();
        String name = warp.name();
        UUID uuid = player.getUniqueId();
        plugin.teleportService().teleport(player, () -> {
            Warp current = warps.get(key);
            return current == null ? null : current.toLocation();
        }, TeleportOptions.DEFAULT).thenAccept(result -> {
            if (!result.isSuccess()) {
                return;
            }
            Player online = Bukkit.getPlayer(uuid);
            if (online != null) {
                plugin.messages().send(online, "warp.teleported", Placeholder.unparsed("warp", name));
            }
            Warp current = warps.get(key);
            if (current != null && (settings.get().countOwnerVisits() || !current.isOwnedBy(uuid))) {
                countVisit(current);
            }
        });
    }

    private void countVisit(Warp warp) {
        warps.computeIfPresent(warp.key(), (key, current) -> current.withVisits(current.visits() + 1));
        plugin.database().update(connection -> WarpRepository.incrementVisits(connection, warp.name()))
                .whenComplete((ignored, error) -> {
                    if (error != null) {
                        plugin.getLogger().log(Level.WARNING, "Failed to count a visit of warp '" + warp.name() + "'", error);
                    }
                });
    }

    /**
     * Stores the player's grade (1-5) asynchronously and reports the outcome.
     * Re-grading the same warp is allowed after {@code grading.cooldown-seconds}.
     */
    public void grade(Player player, Warp warp, int grade) {
        UUID uuid = player.getUniqueId();
        String playerName = player.getName();
        String key = warp.key();
        String name = warp.name();
        long now = System.currentTimeMillis();
        long cooldown = settings.get().gradeCooldownMillis();
        AtomicReference<WarpRepository.GradeOutcome> outcome = new AtomicReference<>();
        plugin.database().updateInTransaction(connection ->
                        outcome.set(WarpRepository.grade(connection, name, uuid, grade, now, cooldown)))
                .whenCompleteAsync((ignored, error) -> {
                    Player online = Bukkit.getPlayer(uuid);
                    if (error != null) {
                        plugin.getLogger().log(Level.SEVERE, "Failed to save a grade of warp '" + name + "'", error);
                        if (online != null) {
                            plugin.messages().send(online, "general.database-error");
                        }
                        return;
                    }
                    WarpRepository.GradeOutcome result = outcome.get();
                    switch (result.status()) {
                        case NOT_FOUND -> {
                            if (online != null) {
                                plugin.messages().send(online, "warp.not-found", Placeholder.unparsed("warp", name));
                            }
                        }
                        case COOLDOWN -> {
                            if (online != null) {
                                plugin.messages().send(online, "warp.grade-cooldown",
                                        Placeholder.unparsed("time", Durations.format(result.remainingMillis())));
                            }
                        }
                        case GRADED -> {
                            if (warps.containsKey(key)) {
                                ratings.compute(key, (k, current) ->
                                        (current == null ? WarpRating.NONE : current).apply(result.previous(), grade));
                            }
                            if (online != null) {
                                plugin.messages().send(online, "warp.graded", Placeholder.unparsed("warp", name),
                                        Placeholder.unparsed("grade", String.valueOf(grade)));
                            }
                            if (settings.get().gradeBroadcast()) {
                                plugin.messages().broadcast("warp.grade-broadcast", Placeholder.unparsed("player", playerName),
                                        Placeholder.unparsed("warp", name), Placeholder.unparsed("grade", String.valueOf(grade)));
                            }
                        }
                    }
                }, plugin.mainThread());
    }

    /**
     * Whether a failed write was a duplicate key (e.g. names equal under MySQL collation rules).
     */
    public static boolean isDuplicate(Throwable error) {
        for (Throwable cause = error; cause != null; cause = cause.getCause()) {
            if (cause instanceof SQLIntegrityConstraintViolationException) {
                return true;
            }
        }
        return false;
    }
}
