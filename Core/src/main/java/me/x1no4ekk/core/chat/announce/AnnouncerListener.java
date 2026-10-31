package me.x1no4ekk.core.chat.announce;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Shows the current boss bar on join and hides it on quit.
 */
public final class AnnouncerListener implements Listener {

    private final BossBarRotator bossBars;

    public AnnouncerListener(BossBarRotator bossBars) {
        this.bossBars = bossBars;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        bossBars.show(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        bossBars.hide(event.getPlayer());
    }
}
