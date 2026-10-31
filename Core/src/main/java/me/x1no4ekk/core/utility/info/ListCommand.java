package me.x1no4ekk.core.utility.info;

import me.x1no4ekk.core.CorePlugin;
import me.x1no4ekk.core.command.BaseCommand;
import me.x1no4ekk.core.hook.ChatHook;
import me.x1no4ekk.core.user.User;
import me.x1no4ekk.core.util.Text;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * {@code /list}: visible online players grouped by their primary permission group.
 */
final class ListCommand extends BaseCommand {

    private static final String DEFAULT_GROUP = "default";

    private final InfoModule module;

    ListCommand(CorePlugin plugin, InfoModule module) {
        super(plugin);
        this.module = module;
    }

    @Override
    protected void execute(CommandSender sender, String label, String[] args) {
        requirePermission(sender, "core.list");
        ChatHook chat = plugin.hooks().chat();
        Map<String, List<Player>> byGroup = new HashMap<>();
        int visible = 0;
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (!canSee(sender, online)) {
                continue;
            }
            visible++;
            String group = chat.isAvailable() ? chat.getPrimaryGroup(online) : "";
            String key = group == null || group.isBlank() ? DEFAULT_GROUP : group.toLowerCase(Locale.ROOT);
            byGroup.computeIfAbsent(key, ignored -> new ArrayList<>()).add(online);
        }
        send(sender, "utility.list.header", placeholder("online", visible), placeholder("max", Bukkit.getMaxPlayers()));
        Map<String, String> configured = module.settings().listGroups();
        List<String> order = new ArrayList<>(configured.keySet());
        byGroup.keySet().stream()
                .filter(group -> !configured.containsKey(group))
                .sorted()
                .forEach(order::add);
        Component separator = messages().get("utility.list.separator");
        for (String group : order) {
            List<Player> players = byGroup.get(group);
            if (players == null) {
                continue;
            }
            players.sort(Comparator.comparing(Player::getName, String.CASE_INSENSITIVE_ORDER));
            List<Component> names = new ArrayList<>(players.size());
            for (Player player : players) {
                names.add(entry(player));
            }
            String title = configured.get(group);
            send(sender, "utility.list.group",
                    Placeholder.component("group", title != null ? Text.parse(title) : Component.text(group)),
                    placeholder("count", players.size()),
                    Placeholder.component("players", Component.join(JoinConfiguration.separator(separator), names)));
        }
        send(sender, "utility.list.footer");
    }

    private Component entry(Player player) {
        User user = plugin.userManager().getUser(player);
        if (user != null && user.isVanish()) {
            return player.displayName().append(messages().get("utility.list.vanished-tag"));
        }
        return player.displayName();
    }
}
