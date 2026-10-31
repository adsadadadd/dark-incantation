package me.x1no4ekk.core.kit;

import me.x1no4ekk.core.CorePlugin;
import me.x1no4ekk.core.user.User;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.UUID;

/**
 * Gives start kits to players on their first join.
 */
final class KitListener implements Listener {

    private final CorePlugin plugin;
    private final KitModule module;

    KitListener(CorePlugin plugin, KitModule module) {
        this.plugin = plugin;
        this.module = module;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        User user = plugin.userManager().getUser(event.getPlayer());
        if (user == null || !user.isNewPlayer()) {
            return;
        }
        UUID uuid = user.getUuid();
        // One tick later, so plugins that set up the inventory on join have finished.
        Bukkit.getScheduler().runTask(plugin, () -> {
            Player player = Bukkit.getPlayer(uuid);
            User current = plugin.userManager().getUser(uuid);
            if (player != null && current != null) {
                module.service().giveStartKits(player, current);
            }
        });
    }
}
