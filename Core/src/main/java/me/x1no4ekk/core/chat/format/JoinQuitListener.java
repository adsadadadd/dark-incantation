package me.x1no4ekk.core.chat.format;

import me.x1no4ekk.core.CorePlugin;
import me.x1no4ekk.core.chat.ChatModule;
import me.x1no4ekk.core.hook.ChatHook;
import me.x1no4ekk.core.user.User;
import me.x1no4ekk.core.util.Text;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Join, first-join and quit messages (hidden for vanished players), the welcome text and title, chat sessions.
 */
public final class JoinQuitListener implements Listener {

    private final CorePlugin plugin;
    private final ChatModule module;

    public JoinQuitListener(CorePlugin plugin, ChatModule module) {
        this.plugin = plugin;
        this.module = module;
    }

    // HIGH: the display name (nickname) is applied by other modules at a lower priority.
    @EventHandler(priority = EventPriority.HIGH)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        module.sessions().start(player, true);
        JoinQuitSettings settings = module.settings().joinQuit();
        User user = plugin.userManager().getUser(player);
        // Unknown vanish state (data still loading) is treated as hidden so a vanished player is never revealed.
        boolean hidden = user == null || user.isVanish();
        boolean firstJoin = user != null ? user.isNewPlayer() : !player.hasPlayedBefore();
        String template = firstJoin && !settings.firstJoin().isEmpty() ? settings.firstJoin() : settings.join();
        event.joinMessage(hidden || template.isEmpty() ? null : render(template, player));

        if (!settings.welcome().isEmpty()) {
            player.sendMessage(render(settings.welcome(), player));
        }
        if (settings.title()) {
            player.showTitle(Title.title(render(settings.titleText(), player), render(settings.subtitleText(), player),
                    settings.titleTimes()));
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        User user = plugin.userManager().getUser(player);
        String template = module.settings().joinQuit().quit();
        boolean hidden = user == null || user.isVanish();
        event.quitMessage(hidden || template.isEmpty() ? null : render(template, player));
        module.forget(player.getUniqueId());
    }

    private Component render(String template, Player player) {
        if (template.isEmpty()) {
            return Component.empty();
        }
        ChatHook hook = plugin.hooks().chat();
        return Text.parse(ChatFormatter.template(player, template),
                Placeholder.component("player", player.displayName()),
                Placeholder.unparsed("name", player.getName()),
                Placeholder.unparsed("online", String.valueOf(Bukkit.getOnlinePlayers().size())),
                Placeholder.component("prefix", ChatFormatter.prefix(hook, player)),
                Placeholder.component("suffix", ChatFormatter.suffix(hook, player)));
    }
}
