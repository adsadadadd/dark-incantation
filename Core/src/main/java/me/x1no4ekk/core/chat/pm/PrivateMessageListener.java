package me.x1no4ekk.core.chat.pm;

import me.x1no4ekk.core.CorePlugin;
import me.x1no4ekk.core.chat.ChatPermissions;
import me.x1no4ekk.core.chat.PrivateMessageModule;
import me.x1no4ekk.core.chat.format.ChatFormatter;
import me.x1no4ekk.core.util.Text;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Set;
import java.util.UUID;

/**
 * Forwards commands of watched players to their spies and cleans private-message state on quit.
 */
public final class PrivateMessageListener implements Listener {

    private final CorePlugin plugin;
    private final PrivateMessageModule module;

    public PrivateMessageListener(CorePlugin plugin, PrivateMessageModule module) {
        this.plugin = plugin;
        this.module = module;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        UUID targetId = player.getUniqueId();
        Set<UUID> spies = module.targetSpy().spiesOf(targetId);
        if (spies.isEmpty()) {
            return;
        }
        Component line = Text.parse(ChatFormatter.template(player, module.settings().targetSpyFormat()),
                Placeholder.component("player", player.displayName()),
                Placeholder.unparsed("name", player.getName()),
                Placeholder.component("prefix", ChatFormatter.prefix(plugin.hooks().chat(), player)),
                Placeholder.unparsed("command", event.getMessage()));
        for (UUID spyId : spies) {
            Player spy = Bukkit.getPlayer(spyId);
            if (spy == null || !spy.hasPermission(ChatPermissions.TARGET_SPY)) {
                module.targetSpy().unwatch(spyId, targetId);
                continue;
            }
            spy.sendMessage(line);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        module.service().quit(uuid);
        for (UUID spyId : module.targetSpy().quit(uuid)) {
            Player spy = Bukkit.getPlayer(spyId);
            if (spy != null) {
                plugin.messages().send(spy, "pm.targetspy-target-left", Placeholder.unparsed("player", player.getName()));
            }
        }
    }
}
