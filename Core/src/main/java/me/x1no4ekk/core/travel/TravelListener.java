package me.x1no4ekk.core.travel;

import me.x1no4ekk.core.CorePlugin;
import me.x1no4ekk.core.teleport.TeleportOptions;
import me.x1no4ekk.core.user.User;
import me.x1no4ekk.core.util.Durations;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/**
 * Spawn on join/respawn, void rescue, the /back pickup ban and cleanup of travel state on quit.
 */
final class TravelListener implements Listener {

    /** System teleports: instant, never refused in combat and not stored as /back. */
    private static final TeleportOptions SYSTEM = TeleportOptions.INSTANT
            .withRecordBack(false)
            .withCause(PlayerTeleportEvent.TeleportCause.PLUGIN);

    private final CorePlugin plugin;
    private final TravelModule module;
    private final PickupGuard pickupGuard;
    /** Players with a void-rescue teleport in flight; main thread only. */
    private final Set<UUID> rescuing = new HashSet<>();

    TravelListener(CorePlugin plugin, TravelModule module, PickupGuard pickupGuard) {
        this.plugin = plugin;
        this.module = module;
        this.pickupGuard = pickupGuard;
    }

    void clear() {
        rescuing.clear();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        TravelSettings config = module.settings();
        Player player = event.getPlayer();
        User user = plugin.userManager().getUser(player);
        boolean first = user != null && user.isNewPlayer() && config.spawnNewPlayers();
        if (!first && !config.spawnOnJoin()) {
            return;
        }
        UUID uuid = player.getUniqueId();
        // One tick later: teleporting inside the join event can be overridden by the login position.
        Bukkit.getScheduler().runTask(plugin, () -> {
            Player online = Bukkit.getPlayer(uuid);
            Location spawn = module.spawn().location();
            if (online != null && spawn != null) {
                plugin.teleportService().teleport(online, spawn, SYSTEM);
            }
        });
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onRespawn(PlayerRespawnEvent event) {
        if (!module.settings().respawnAtSpawn()
                || event.getRespawnReason() != PlayerRespawnEvent.RespawnReason.DEATH
                || event.isBedSpawn() || event.isAnchorSpawn()) {
            return;
        }
        Location spawn = module.spawn().location();
        if (spawn != null) {
            event.setRespawnLocation(spawn);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        TravelSettings config = module.settings();
        Location to = event.getTo();
        if (!config.voidRescue() || to.getY() >= config.voidLevel() || !isRescueWorld(config, to)) {
            return;
        }
        Player player = event.getPlayer();
        // Flying below the level on purpose; real falls are still caught by the void damage handler.
        if (player.getGameMode() == GameMode.SPECTATOR || player.getGameMode() == GameMode.CREATIVE) {
            return;
        }
        rescue(player);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onVoidDamage(EntityDamageEvent event) {
        if (event.getCause() != EntityDamageEvent.DamageCause.VOID || !(event.getEntity() instanceof Player player)) {
            return;
        }
        TravelSettings config = module.settings();
        if (config.voidRescue() && isRescueWorld(config, player.getLocation())) {
            event.setCancelled(true);
            rescue(player);
        }
    }

    private static boolean isRescueWorld(TravelSettings config, Location location) {
        return location.getWorld() != null
                && config.voidWorlds().contains(location.getWorld().getName().toLowerCase(Locale.ROOT));
    }

    private void rescue(Player player) {
        UUID uuid = player.getUniqueId();
        if (!rescuing.add(uuid)) {
            return;
        }
        Location destination = module.spawn().location();
        if (destination == null) {
            destination = player.getWorld().getSpawnLocation();
        }
        player.setFallDistance(0F);
        plugin.teleportService().teleport(player, destination, SYSTEM).whenComplete((result, error) -> {
            rescuing.remove(uuid);
            Player online = Bukkit.getPlayer(uuid);
            if (online != null && result != null && result.isSuccess()) {
                online.setFallDistance(0F);
                plugin.messages().send(online, "travel.spawn.void-rescued");
            }
        });
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPickup(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        UUID uuid = player.getUniqueId();
        long left = pickupGuard.remaining(uuid);
        if (left <= 0) {
            return;
        }
        event.setCancelled(true);
        if (pickupGuard.shouldNotify(uuid)) {
            plugin.messages().sendActionBar(player, "travel.back.pickup-blocked-bar",
                    Placeholder.unparsed("time", Durations.format(left)));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        module.tpa().handleQuit(uuid);
        pickupGuard.remove(uuid);
        rescuing.remove(uuid);
    }
}
