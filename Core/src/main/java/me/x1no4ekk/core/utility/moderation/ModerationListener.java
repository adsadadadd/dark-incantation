package me.x1no4ekk.core.utility.moderation;

import com.destroystokyo.paper.event.server.PaperServerListPingEvent;
import me.x1no4ekk.core.CorePlugin;
import me.x1no4ekk.core.user.User;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.player.PlayerAttemptPickupItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Join/quit handling for vanish, nicknames and spectating, plus the gameplay side of vanish.
 */
final class ModerationListener implements Listener {

    private final CorePlugin plugin;
    private final ModerationModule module;

    ModerationListener(CorePlugin plugin, ModerationModule module) {
        this.plugin = plugin;
        this.module = module;
    }

    /**
     * LOW: after the user cache is updated (LOWEST) and before join messages that use the display name.
     */
    @EventHandler(priority = EventPriority.LOW)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        module.vanish().hideVanishedFrom(player);
        module.whenLoaded(player, module::applyJoinState);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onJoinMessage(PlayerJoinEvent event) {
        if (module.vanish().isVanished(event.getPlayer().getUniqueId())) {
            event.joinMessage(null);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onQuitMessage(PlayerQuitEvent event) {
        if (module.vanish().isVanished(event.getPlayer().getUniqueId())) {
            event.quitMessage(null);
        }
    }

    /**
     * NORMAL: the user is unloaded at MONITOR, and the spectator must be restored before the player is saved.
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onQuit(PlayerQuitEvent event) {
        module.spectate().handleQuit(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPickup(PlayerAttemptPickupItemEvent event) {
        if (module.vanish().isVanished(event.getPlayer().getUniqueId())) {
            event.setFlyAtPlayer(false);
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onTarget(EntityTargetLivingEntityEvent event) {
        if (event.getTarget() instanceof Player player && module.vanish().isVanished(player.getUniqueId())) {
            event.setCancelled(true);
        }
    }

    /**
     * May run off the main thread: only the thread-safe user cache is read.
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onServerListPing(PaperServerListPingEvent event) {
        if (!module.settings().vanish().hideFromServerList()) {
            return;
        }
        int vanished = 0;
        for (User user : plugin.userManager().getUsers()) {
            if (user.isVanish()) {
                vanished++;
            }
        }
        if (vanished == 0) {
            return;
        }
        event.getListedPlayers().removeIf(info -> module.vanish().isVanished(info.id()));
        event.setNumPlayers(Math.max(0, event.getNumPlayers() - vanished));
    }
}
