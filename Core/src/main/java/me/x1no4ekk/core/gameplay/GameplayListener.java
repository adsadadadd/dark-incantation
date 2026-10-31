package me.x1no4ekk.core.gameplay;

import me.x1no4ekk.core.CorePlugin;
import me.x1no4ekk.core.user.User;
import org.bukkit.GameMode;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.persistence.PersistentDataType;

import java.util.UUID;

/**
 * God mode protection, fly restoration, pickup restrictions and per-player cleanup.
 */
final class GameplayListener implements Listener {

    private static final long PICKUP_NOTICE_MILLIS = 2000L;

    private final CorePlugin plugin;
    private final GameplayModule module;

    GameplayListener(CorePlugin plugin, GameplayModule module) {
        this.plugin = plugin;
        this.module = module;
    }

    // ---------------------------------------------------------------- join / quit / fly restore

    @EventHandler(priority = EventPriority.NORMAL)
    public void onJoin(PlayerJoinEvent event) {
        module.handleJoin(event.getPlayer());
    }

    /**
     * HIGH: runs before the user is unloaded at MONITOR.
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onQuit(PlayerQuitEvent event) {
        module.handleQuit(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onWorldChange(PlayerChangedWorldEvent event) {
        module.flight().scheduleRestore(event.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onRespawn(PlayerRespawnEvent event) {
        module.flight().scheduleRestore(event.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onGameModeChange(PlayerGameModeChangeEvent event) {
        GameMode mode = event.getNewGameMode();
        if (mode == GameMode.SURVIVAL || mode == GameMode.ADVENTURE) {
            module.flight().scheduleRestore(event.getPlayer().getUniqueId());
        }
    }

    // ---------------------------------------------------------------- god mode

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        EntityDamageEvent.DamageCause cause = event.getCause();
        if (cause == EntityDamageEvent.DamageCause.VOID || cause == EntityDamageEvent.DamageCause.KILL) {
            return;
        }
        if (isGod(player)) {
            event.setCancelled(true);
            if (cause == EntityDamageEvent.DamageCause.FIRE_TICK) {
                player.setFireTicks(0);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDamageByEntity(EntityDamageByEntityEvent event) {
        Entity damager = event.getDamager();
        if (damager instanceof Firework firework && firework.getPersistentDataContainer()
                .has(module.harmlessFireworkKey(), PersistentDataType.BOOLEAN)) {
            event.setCancelled(true);
            return;
        }
        if (!module.settings().god().blockPvp() || !(event.getEntity() instanceof Player victim)) {
            return;
        }
        Player attacker = attacker(damager);
        if (attacker != null && !attacker.equals(victim) && isGod(attacker)) {
            event.setCancelled(true);
            plugin.messages().sendActionBar(attacker, "gameplay.god.pvp-blocked");
        }
    }

    private static Player attacker(Entity damager) {
        if (damager instanceof Player player) {
            return player;
        }
        if (damager instanceof Projectile projectile && projectile.getShooter() instanceof Player shooter) {
            return shooter;
        }
        return null;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onFood(FoodLevelChangeEvent event) {
        if (module.settings().god().keepFood() && event.getEntity() instanceof Player player
                && event.getFoodLevel() < player.getFoodLevel() && isGod(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onTarget(EntityTargetLivingEntityEvent event) {
        if (module.settings().god().preventMobTarget() && event.getTarget() instanceof Player player && isGod(player)) {
            event.setCancelled(true);
        }
    }

    private boolean isGod(Player player) {
        User user = plugin.userManager().getUser(player);
        return user != null && user.isGod();
    }

    // ---------------------------------------------------------------- pickup

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPickup(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        User user = plugin.userManager().getUser(player);
        if (user == null) {
            return;
        }
        GameplaySettings config = module.settings();
        UUID uuid = player.getUniqueId();
        boolean flying = user.isFly() && FlightService.survivalLike(player) || module.flight().isElytraFly(uuid);
        boolean blocked = config.fly().blockPickup() && flying && !player.hasPermission("core.fly.bypass-pickup")
                || config.god().blockPickup() && user.isGod() && !player.hasPermission("core.god.bypass-pickup");
        if (!blocked) {
            return;
        }
        event.setCancelled(true);
        Cooldowns notices = module.pickupNotices();
        if (notices.remaining(uuid) == 0) {
            notices.start(uuid, PICKUP_NOTICE_MILLIS);
            plugin.messages().send(player, "gameplay.pickup-blocked");
        }
    }
}
