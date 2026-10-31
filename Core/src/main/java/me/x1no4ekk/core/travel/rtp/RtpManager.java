package me.x1no4ekk.core.travel.rtp;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import me.x1no4ekk.core.CorePlugin;
import me.x1no4ekk.core.teleport.TeleportOptions;
import me.x1no4ekk.core.user.User;
import me.x1no4ekk.core.util.Durations;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.HeightMap;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Statistic;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;
import java.util.logging.Level;

/**
 * Asynchronous random-teleport search and per-mode cooldowns. Main thread only.
 * <p>
 * A search tries one random column per attempt: the chunk is loaded with {@code getChunkAtAsync}, the column is
 * checked on the main thread, and the next attempt is chained only after the previous one finished, so at most
 * one chunk load per player is in flight.
 */
public final class RtpManager {

    public static final String BYPASS_COOLDOWN_PERMISSION = "core.rtp.bypass-cooldown";

    private final CorePlugin plugin;
    private final Supplier<RtpSettings> settings;
    /** One running search per player; the value identifies the search so stale callbacks stop. */
    private final Map<UUID, Search> searches = new HashMap<>();
    /** player → mode → cooldown end; purged periodically. */
    private final Map<UUID, Map<String, Long>> cooldowns = new HashMap<>();

    RtpManager(CorePlugin plugin, Supplier<RtpSettings> settings) {
        this.plugin = plugin;
        this.settings = settings;
    }

    private record Search(UUID player, RtpSettings.RtpMode mode, String world, double centerX, double centerZ) {
    }

    // ---------------------------------------------------------------- cooldowns

    public long cooldownLeft(UUID uuid, String mode) {
        Map<String, Long> map = cooldowns.get(uuid);
        Long until = map == null ? null : map.get(mode);
        return until == null ? 0L : Math.max(0L, until - System.currentTimeMillis());
    }

    private void startCooldown(Player player, RtpSettings.RtpMode mode) {
        if (mode.cooldownMillis() > 0 && !player.hasPermission(BYPASS_COOLDOWN_PERMISSION)) {
            cooldowns.computeIfAbsent(player.getUniqueId(), key -> new HashMap<>())
                    .put(mode.id(), System.currentTimeMillis() + mode.cooldownMillis());
        }
    }

    void purgeCooldowns() {
        long now = System.currentTimeMillis();
        cooldowns.values().removeIf(map -> {
            map.values().removeIf(until -> until <= now);
            return map.isEmpty();
        });
    }

    public boolean canUse(Player player, RtpSettings.RtpMode mode) {
        return mode.permission().isEmpty() || player.hasPermission(mode.permission());
    }

    // ---------------------------------------------------------------- search

    /**
     * Validates the request and starts an asynchronous search; all outcomes are reported to the player.
     */
    public void start(Player player, RtpSettings.RtpMode mode) {
        UUID uuid = player.getUniqueId();
        if (!canUse(player, mode)) {
            plugin.messages().send(player, "general.no-permission");
            return;
        }
        long cooldown = player.hasPermission(BYPASS_COOLDOWN_PERMISSION) ? 0L : cooldownLeft(uuid, mode.id());
        if (cooldown > 0) {
            plugin.messages().send(player, "general.cooldown", Placeholder.unparsed("time", Durations.format(cooldown)));
            return;
        }
        if (searches.containsKey(uuid)) {
            plugin.messages().send(player, "travel.rtp.already-searching");
            return;
        }
        if (plugin.combatManager().isInCombat(uuid)) {
            plugin.messages().send(player, "teleport.in-combat",
                    Placeholder.unparsed("seconds", String.valueOf(plugin.combatManager().secondsLeft(uuid))));
            return;
        }

        Search search;
        if (mode.type() == RtpSettings.ModeType.NEAR_PLAYER) {
            Player target = pickTarget(player, mode);
            if (target == null) {
                plugin.messages().send(player, "travel.rtp.no-players");
                return;
            }
            Location at = target.getLocation();
            search = new Search(uuid, mode, at.getWorld().getName(), at.getX(), at.getZ());
        } else {
            World world = Bukkit.getWorld(mode.world());
            if (world == null || world.hasCeiling()) {
                plugin.messages().send(player, "travel.rtp.world-unavailable", Placeholder.unparsed("world", mode.world()));
                return;
            }
            Location center = switch (mode.center()) {
                case WORLD_SPAWN -> world.getSpawnLocation();
                case BORDER_CENTER -> world.getWorldBorder().getCenter();
                case CUSTOM -> new Location(world, mode.centerX(), 0D, mode.centerZ());
            };
            search = new Search(uuid, mode, world.getName(), center.getX(), center.getZ());
        }
        searches.put(uuid, search);
        plugin.messages().send(player, "travel.rtp.searching");
        attempt(search, 0);
    }

    private Player pickTarget(Player seeker, RtpSettings.RtpMode mode) {
        List<Player> candidates = new ArrayList<>();
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online.equals(seeker) || !seeker.canSee(online) || online.getGameMode() == GameMode.SPECTATOR) {
                continue;
            }
            User user = plugin.userManager().getUser(online);
            if (user == null || user.isVanish()) {
                continue;
            }
            World world = online.getWorld();
            String worldName = world.getName();
            boolean allowedWorld = mode.world().isEmpty()
                    ? !mode.disabledWorlds().contains(worldName.toLowerCase(Locale.ROOT))
                    : mode.world().equalsIgnoreCase(worldName);
            if (!allowedWorld || world.hasCeiling()) {
                continue;
            }
            if (mode.minPlaytimeTicks() > 0 && online.getStatistic(Statistic.PLAY_ONE_MINUTE) < mode.minPlaytimeTicks()) {
                continue;
            }
            if (mode.playerMaxDistance() > 0) {
                Location spawn = world.getSpawnLocation();
                Location at = online.getLocation();
                double dx = at.getX() - spawn.getX();
                double dz = at.getZ() - spawn.getZ();
                if (dx * dx + dz * dz > (double) mode.playerMaxDistance() * mode.playerMaxDistance()) {
                    continue;
                }
            }
            candidates.add(online);
        }
        return candidates.isEmpty() ? null : candidates.get(ThreadLocalRandom.current().nextInt(candidates.size()));
    }

    private void attempt(Search search, int attempt) {
        if (searches.get(search.player()) != search) {
            return; // cancelled: player left or module disabled
        }
        RtpSettings config = settings.get();
        World world = Bukkit.getWorld(search.world());
        if (world == null) {
            fail(search);
            return;
        }
        ThreadLocalRandom random = ThreadLocalRandom.current();
        RtpSettings.RtpMode mode = search.mode();
        int tries = attempt;
        int x;
        int z;
        // Points outside the world border cost nothing to reject, so they do not need a chunk load.
        while (true) {
            if (tries >= config.maxAttempts()) {
                fail(search);
                return;
            }
            tries++;
            double angle = random.nextDouble(Math.PI * 2D);
            double minSq = (double) mode.minRadius() * mode.minRadius();
            double maxSq = (double) mode.maxRadius() * mode.maxRadius();
            double distance = Math.sqrt(minSq + random.nextDouble() * (maxSq - minSq)); // uniform over the ring area
            x = (int) Math.floor(search.centerX() + Math.cos(angle) * distance);
            z = (int) Math.floor(search.centerZ() + Math.sin(angle) * distance);
            if (world.getWorldBorder().isInside(new Location(world, x + 0.5D, world.getMinHeight(), z + 0.5D))) {
                break;
            }
        }
        int nextAttempt = tries;
        int blockX = x;
        int blockZ = z;
        world.getChunkAtAsync(x >> 4, z >> 4, true).whenCompleteAsync((chunk, error) -> {
            if (searches.get(search.player()) != search) {
                return;
            }
            if (error != null) {
                plugin.getLogger().log(Level.FINE, "RTP chunk load failed", error);
                attempt(search, nextAttempt);
                return;
            }
            Location found = check(search.world(), blockX, blockZ);
            if (found == null) {
                attempt(search, nextAttempt);
            } else {
                found(search, found);
            }
        }, plugin.mainThread());
    }

    /**
     * @return a safe standing location on the column, or {@code null}
     */
    private Location check(String worldName, int x, int z) {
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            return null;
        }
        RtpSettings config = settings.get();
        Block ground = world.getHighestBlockAt(x, z, HeightMap.MOTION_BLOCKING_NO_LEAVES);
        if (ground.getY() <= world.getMinHeight() || ground.getY() + 2 >= world.getMaxHeight()) {
            return null;
        }
        if (ground.isLiquid() || !ground.getType().isSolid() || config.unsafeBlocks().contains(ground.getType())) {
            return null;
        }
        Block feet = ground.getRelative(BlockFace.UP);
        Block head = feet.getRelative(BlockFace.UP);
        if (!isFree(feet, config) || !isFree(head, config)) {
            return null;
        }
        if (!config.blockedBiomes().isEmpty()) {
            Biome biome = world.getBiome(x, feet.getY(), z);
            NamespacedKey key = RegistryAccess.registryAccess().getRegistry(RegistryKey.BIOME).getKey(biome);
            if (key != null && config.blockedBiomes().contains(key)) {
                return null;
            }
        }
        Location location = new Location(world, x + 0.5D, feet.getY(), z + 0.5D);
        return world.getWorldBorder().isInside(location) ? location : null;
    }

    private static boolean isFree(Block block, RtpSettings config) {
        return block.isPassable() && !block.isLiquid() && !config.unsafeBlocks().contains(block.getType());
    }

    private void found(Search search, Location location) {
        searches.remove(search.player());
        Player player = Bukkit.getPlayer(search.player());
        if (player == null) {
            return;
        }
        location.setYaw(player.getLocation().getYaw());
        location.setPitch(player.getLocation().getPitch());
        // The search already spent chunk loads, so the cooldown starts even if the warmup is then cancelled.
        startCooldown(player, search.mode());
        UUID uuid = search.player();
        String world = location.getWorld().getName();
        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();
        plugin.teleportService().teleport(player, location, TeleportOptions.DEFAULT).thenAccept(result -> {
            Player online = Bukkit.getPlayer(uuid);
            if (result.isSuccess() && online != null) {
                plugin.messages().send(online, "travel.rtp.teleported",
                        Placeholder.unparsed("world", world),
                        Placeholder.unparsed("x", String.valueOf(x)),
                        Placeholder.unparsed("y", String.valueOf(y)),
                        Placeholder.unparsed("z", String.valueOf(z)));
            }
        });
    }

    private void fail(Search search) {
        searches.remove(search.player());
        Player player = Bukkit.getPlayer(search.player());
        if (player != null) {
            plugin.messages().send(player, "travel.rtp.failed");
        }
    }

    void handleQuit(UUID uuid) {
        searches.remove(uuid);
    }

    void clear() {
        searches.clear();
        cooldowns.clear();
    }
}
