package me.x1no4ekk.core.top;

import me.x1no4ekk.core.CorePlugin;
import me.x1no4ekk.core.command.BaseCommand;
import me.x1no4ekk.core.util.Numbers;
import me.x1no4ekk.core.util.Text;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

/**
 * /killtop, /deathtop and /baltop [page] — pages of the cached leaderboard and the viewer's own place.
 */
final class TopListCommand extends BaseCommand {

    private final TopModule module;
    private final TopType type;
    private final String permission;
    private final String keys;

    TopListCommand(CorePlugin plugin, TopModule module, TopType type) {
        super(plugin);
        this.module = module;
        this.type = type;
        this.permission = "core." + type.command();
        this.keys = "top." + type.command() + ".";
    }

    @Override
    protected void execute(CommandSender sender, String label, String[] args) {
        requirePermission(sender, permission);
        if (args.length > 1) {
            throw usage(keys + "usage");
        }
        TopManager manager = module.manager();
        if (type == TopType.BALANCE && !module.settings().balanceEnabled()) {
            throw fail("top.baltop-unavailable");
        }
        if (!manager.isReady()) {
            throw fail("top.loading");
        }
        List<TopEntry> entries = manager.get(type);
        if (entries == null) {
            throw fail(type == TopType.BALANCE ? "top.baltop-unavailable" : "top.loading");
        }
        if (entries.isEmpty()) {
            throw fail("top.empty");
        }
        int pageSize = module.settings().pageSize();
        int pages = (entries.size() + pageSize - 1) / pageSize;
        int page = args.length == 1 ? parseInt(args[0], 1, pages) : 1;

        Component title = messages().get(keys + "title");
        send(sender, "top.header", Placeholder.component("title", title),
                placeholder("page", page), placeholder("pages", pages));
        int from = (page - 1) * pageSize;
        int to = Math.min(entries.size(), from + pageSize);
        for (int index = from; index < to; index++) {
            TopEntry entry = entries.get(index);
            send(sender, "top.line", Placeholder.component("place", place(index + 1)),
                    placeholder("player", entry.name()), placeholder("value", format(entry.value())));
        }
        if (sender instanceof Player player) {
            UUID uuid = player.getUniqueId();
            int position = IntStream.range(0, entries.size())
                    .filter(index -> entries.get(index).uuid().equals(uuid))
                    .findFirst().orElse(-1);
            if (position >= 0) {
                send(sender, "top.self", placeholder("position", position + 1),
                        placeholder("value", format(entries.get(position).value())));
            } else {
                send(sender, "top.self-none", placeholder("size", entries.size()));
            }
        }
        Component next = Component.empty();
        if (page < pages) {
            String command = "/" + label + " " + (page + 1);
            next = messages().get("top.next-page", placeholder("command", command))
                    .clickEvent(ClickEvent.runCommand(command));
        }
        send(sender, "top.footer", placeholder("page", page), placeholder("pages", pages),
                Placeholder.component("next", next));
    }

    private Component place(int position) {
        String key = position <= 3 ? "top.place." + position : "top.place.other";
        String template = messages().template(key);
        return Text.parse(template == null ? "<position>." : template, placeholder("position", position));
    }

    private String format(double value) {
        if (type == TopType.BALANCE) {
            return plugin.hooks().economy().format(value);
        }
        return Numbers.format((long) value);
    }

    @Override
    protected List<String> complete(CommandSender sender, String label, String[] args) {
        if (args.length != 1 || !sender.hasPermission(permission)) {
            return List.of();
        }
        List<TopEntry> entries = module.manager().get(type);
        if (entries == null || entries.isEmpty()) {
            return List.of();
        }
        int pages = (entries.size() + module.settings().pageSize() - 1) / module.settings().pageSize();
        return filter(IntStream.rangeClosed(1, pages).mapToObj(String::valueOf), args[0]);
    }
}
