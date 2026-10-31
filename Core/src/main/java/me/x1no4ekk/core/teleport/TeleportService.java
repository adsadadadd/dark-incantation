package me.x1no4ekk.core.teleport;

import me.x1no4ekk.core.CorePlugin;
import me.x1no4ekk.core.config.ConfigFiles;
import me.x1no4ekk.core.config.ConfigUtil;
import me.x1no4ekk.core.module.CoreModule;
import me.x1no4ekk.core.user.User;
import me.x1no4ekk.core.util.Text;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * Single entry point for player teleports: warmup with a boss bar and particles, cancellation on movement,
 * damage or combat, the {@code /back} location and short invulnerability after arrival.
 * <p>
 * All state is keyed by UUID, touched only on the main thread and dropped on quit. The warmup ticker runs only
 * while at least one warmup is pending.
 */
public final class TeleportService implements CoreModule, Listener {

    public static final String BYPASS_WARMUP_PERMISSION = "core.teleport.bypass-warmup";
    public static final String BACK_ON_DEATH_PERMISSION = "core.back.ondeath";

    private final CorePlugin plugin;
    private volatile TeleportSettings settings;

    /** Pending warmups; main thread only. */
    private final Map<UUID, Pending> pending = new HashMap<>();
    /** Post-teleport invulnerability end time; main thread only. */
    private final Map<UUID, Long> protectedUntil = new HashMap<>();
    private BukkitTask ticker;

    public TeleportService(CorePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public String id() {
        return "teleport";
    }

    @Override
    public Runnable loadConfig(ConfigFiles files) throws Exception {
        TeleportSettings loaded = TeleportSettings.load(ConfigUtil.section(files.settings(), "teleport"));
        return () -> this.settings = loaded;
    }

    @Override
    public void enable() {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void disable() {
        for (Pending entry : new ArrayList<>(pending.values())) {
            finish(entry, TeleportResult.CANCELLED, null);
        }
        pending.clear();
        protectedUntil.clear();
        stopTicker();
    }

    public TeleportSettings settings() {
        return settings;
    }

    // ---------------------------------------------------------------- public API

    /**
     * Teleports to a fixed location. The returned future completes on the main thread.
     */
    public CompletableFuture<TeleportResult> teleport(Player player, Location destination, TeleportOptions options) {
        Location copy = destination.clone();
        return teleport(player, () -> copy, options);
    }

    /**
     * Teleports to a location resolved when the warmup ends (e.g. the current position of another player).
     * The supplier must capture UUIDs, not players; returning {@code null} yields {@link TeleportResult#NO_DESTINATION}.
     * The returned future completes on the main thread.
     */
    public CompletableFuture<TeleportResult> teleport(Player player, Supplier<Location> destination, TeleportOptions options) {
        if (!Bukkit.isPrimaryThread()) {
            UUID uuid = player.getUniqueId();
            return CompletableFuture.supplyAsync(() -> {
                Player online = Bukkit.getPlayer(uuid);
                return online == null
                        ? CompletableFuture.completedFuture(TeleportResult.OFFLINE)
                        : teleport(online, destination, options);
            }, plugin.mainThread()).thenCompose(future -> future);
        }

        UUID uuid = player.getUniqueId();
        Pending previous = pending.remove(uuid);
        if (previous != null) {
            finish(previous, TeleportResult.CANCELLED, null);
        }
        if (options.checkCombat() && plugin.combatManager().isInCombat(uuid)) {
            plugin.messages().send(player, "teleport.in-combat",
                    Placeholder.unparsed("seconds", String.valueOf(plugin.combatManager().secondsLeft(uuid))));
            return CompletableFuture.completedFuture(TeleportResult.IN_COMBAT);
        }

        TeleportSettings config = settings;
        int warmupTicks = options.warmup() && !player.hasPermission(BYPASS_WARMUP_PERMISSION) ? config.warmupSeconds() * 20 : 0;
        if (warmupTicks <= 0) {
            return execute(player, destination, options);
        }

        Location origin = player.getLocation();
        BossBar bar = null;
        if (config.bossbarEnabled()) {
            bar = BossBar.bossBar(bossBarTitle(config.warmupSeconds()), 1F, config.bossbarColor(), config.bossbarOverlay());
            player.showBossBar(bar);
        }
        Pending entry = new Pending(uuid, origin.getWorld().getName(), origin.getBlockX(), origin.getBlockY(), origin.getBlockZ(),
                destination, options, warmupTicks, bar);
        pending.put(uuid, entry);
        plugin.messages().send(player, "teleport.warmup", Placeholder.unparsed("seconds", String.valueOf(config.warmupSeconds())));
        startTicker();
        return entry.future;
    }

    public boolean isTeleporting(UUID uuid) {
        return pending.containsKey(uuid);
    }

    /**
     * Cancels a pending warmup.
     *
     * @param messageKey message sent to the player, or {@code null} for none
     * @return {@code true} if a warmup was cancelled
     */
    public boolean cancel(UUID uuid, TeleportResult result, String messageKey) {
        Pending entry = pending.remove(uuid);
        if (entry == null) {
            return false;
        }
        finish(entry, result, messageKey);
        return true;
    }

    /**
     * Stores the player's current position as their {@code /back} location.
     */
    public void recordBack(Player player) {
        recordBack(player.getUniqueId(), player.getLocation());
    }

    public void recordBack(UUID uuid, Location location) {
        User user = plugin.userManager().getUser(uuid);
        if (user != null && location != null && location.getWorld() != null) {
            user.setLastLocation(location);
        }
    }

    /**
     * @return the {@code /back} location, or {@code null} if unknown or its world is not loaded
     */
    public Location getBackLocation(UUID uuid) {
        User user = plugin.userManager().getUser(uuid);
        return user == null ? null : user.getLastLocation();
    }

    /**
     * Makes the player invulnerable for {@code teleport.protection-seconds}.
     */
    public void protect(UUID uuid) {
        long millis = settings.protectionMillis();
        if (millis > 0) {
            protectedUntil.put(uuid, System.currentTimeMillis() + millis);
        }
    }

    // ---------------------------------------------------------------- execution

    private CompletableFuture<TeleportResult> execute(Player player, Supplier<Location> destination, TeleportOptions options) {
        UUID uuid = player.getUniqueId();
        Location target = destination.get();
        if (target == null || target.getWorld() == null || Bukkit.getWorld(target.getWorld().getUID()) == null) {
            plugin.messages().send(player, "teleport.no-destination");
            return CompletableFuture.completedFuture(TeleportResult.NO_DESTINATION);
        }
        if (options.checkCombat() && plugin.combatManager().isInCombat(uuid)) {
            plugin.messages().send(player, "teleport.in-combat",
                    Placeholder.unparsed("seconds", String.valueOf(plugin.combatManager().secondsLeft(uuid))));
            return CompletableFuture.completedFuture(TeleportResult.IN_COMBAT);
        }
        Location origin = player.getLocation();
        return player.teleportAsync(target, options.cause()).thenApplyAsync(success -> {
            Player online = Bukkit.getPlayer(uuid);
            if (online == null) {
                return TeleportResult.OFFLINE;
            }
            if (!Boolean.TRUE.equals(success)) {
                plugin.messages().send(online, "teleport.failed");
                return TeleportResult.FAILED;
            }
            if (options.recordBack()) {
                recordBack(uuid, origin);
            }
            protect(uuid);
            settings.arrivalSound().play(online);
            return TeleportResult.SUCCESS;
        }, plugin.mainThread());
    }

    private void startTicker() {
        if (ticker == null || ticker.isCancelled()) {
            ticker = Bukkit.getScheduler().runTaskTimer(plugin, this::tick, 1L, 1L);
        }
    }

    private void stopTicker() {
        if (ticker != null) {
            ticker.cancel();
            ticker = null;
        }
    }

    private void tick() {
        if (pending.isEmpty()) {
            stopTicker();
            return;
        }
        TeleportSettings config = settings;
        for (Pending entry : new ArrayList<>(pending.values())) {
            if (pending.get(entry.uuid) != entry) {
                continue; // replaced while iterating
            }
            Player player = Bukkit.getPlayer(entry.uuid);
            if (player == null) {
                pending.remove(entry.uuid);
                finish(entry, TeleportResult.OFFLINE, null);
                continue;
            }
            if (config.cancelOnMove() && entry.hasMoved(player.getLocation())) {
                pending.remove(entry.uuid);
                finish(entry, TeleportResult.CANCELLED_MOVE, "teleport.cancelled-move");
                continue;
            }
            entry.remainingTicks--;
            if (entry.remainingTicks <= 0) {
                pending.remove(entry.uuid);
                hideBar(player, entry);
                execute(player, entry.destination, entry.options).thenAccept(entry.future::complete);
                continue;
            }
            if (entry.bar != null) {
                entry.bar.progress(Math.clamp((float) entry.remainingTicks / entry.totalTicks, 0F, 1F));
                int seconds = (entry.remainingTicks + 19) / 20;
                if (seconds != entry.shownSeconds) {
                    entry.shownSeconds = seconds;
                    entry.bar.name(bossBarTitle(seconds));
                }
            }
            if (config.particlesEnabled() && config.particleCount() > 0 && entry.remainingTicks % 4 == 0) {
                Location at = player.getLocation().add(0, 1, 0);
                player.getWorld().spawnParticle(Particle.DUST, at, config.particleCount(), 0.4, 0.8, 0.4, 0,
                        new Particle.DustOptions(config.particleColor(), config.particleSize()));
            }
        }
        if (pending.isEmpty()) {
            stopTicker();
        }
    }

    private net.kyori.adventure.text.Component bossBarTitle(int seconds) {
        return plugin.messages().get("teleport.bossbar", Placeholder.unparsed("seconds", String.valueOf(seconds)));
    }

    /**
     * Completes a pending warmup that has already been removed from the map.
     */
    private void finish(Pending entry, TeleportResult result, String messageKey) {
        Player player = Bukkit.getPlayer(entry.uuid);
        if (player != null) {
            hideBar(player, entry);
            if (messageKey != null) {
                plugin.messages().send(player, messageKey);
            }
        }
        entry.future.complete(result);
    }

    private static void hideBar(Player player, Pending entry) {
        if (entry.bar != null) {
            player.hideBossBar(entry.bar);
        }
    }

    // ---------------------------------------------------------------- listeners

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDamageCancelWarmup(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player && settings.cancelOnDamage()) {
            cancel(player.getUniqueId(), TeleportResult.CANCELLED_DAMAGE, "teleport.cancelled-damage");
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onDamageProtect(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        Long until = protectedUntil.get(player.getUniqueId());
        if (until == null) {
            return;
        }
        if (until < System.currentTimeMillis()) {
            protectedUntil.remove(player.getUniqueId());
            return;
        }
        EntityDamageEvent.DamageCause cause = event.getCause();
        if (cause != EntityDamageEvent.DamageCause.VOID && cause != EntityDamageEvent.DamageCause.KILL
                && cause != EntityDamageEvent.DamageCause.WORLD_BORDER) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        cancel(player.getUniqueId(), TeleportResult.CANCELLED, null);
        protectedUntil.remove(player.getUniqueId());
        if (settings.backOnDeath() && player.hasPermission(BACK_ON_DEATH_PERMISSION)) {
            recordBack(player);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        cancel(uuid, TeleportResult.OFFLINE, null);
        protectedUntil.remove(uuid);
    }

    // ---------------------------------------------------------------- state

    private static final class Pending {
        private final UUID uuid;
        private final String world;
        private final int blockX;
        private final int blockY;
        private final int blockZ;
        private final Supplier<Location> destination;
        private final TeleportOptions options;
        private final int totalTicks;
        private final BossBar bar;
        private final CompletableFuture<TeleportResult> future = new CompletableFuture<>();
        private int remainingTicks;
        private int shownSeconds = -1;

        private Pending(UUID uuid, String world, int blockX, int blockY, int blockZ, Supplier<Location> destination,
                        TeleportOptions options, int totalTicks, BossBar bar) {
            this.uuid = uuid;
            this.world = world;
            this.blockX = blockX;
            this.blockY = blockY;
            this.blockZ = blockZ;
            this.destination = destination;
            this.options = options;
            this.totalTicks = totalTicks;
            this.remainingTicks = totalTicks;
            this.bar = bar;
        }

        private boolean hasMoved(Location now) {
            World nowWorld = now.getWorld();
            return nowWorld == null || !nowWorld.getName().equals(world)
                    || now.getBlockX() != blockX || now.getBlockY() != blockY || now.getBlockZ() != blockZ;
        }
    }

    /**
     * MiniMessage-safe display of a location for messages: {@code world x y z}.
     */
    public static String describe(Location location) {
        if (location == null || location.getWorld() == null) {
            return "?";
        }
        return Text.escape(location.getWorld().getName()) + " " + location.getBlockX() + " " + location.getBlockY() + " " + location.getBlockZ();
    }
}
