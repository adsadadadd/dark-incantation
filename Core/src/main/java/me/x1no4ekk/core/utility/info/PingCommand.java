package me.x1no4ekk.core.utility.info;

import me.x1no4ekk.core.CorePlugin;
import me.x1no4ekk.core.command.BaseCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * {@code /ping [player]}.
 */
final class PingCommand extends BaseCommand {

    PingCommand(CorePlugin plugin) {
        super(plugin);
    }

    @Override
    protected void execute(CommandSender sender, String label, String[] args) {
        requirePermission(sender, "core.ping");
        if (args.length > 1) {
            throw usage("utility.ping.usage");
        }
        Player target = args.length == 0 ? playerOnly(sender) : otherPlayer(sender, args[0], "core.ping.others");
        int ping = target.getPing();
        NamedTextColor color = ping < 80 ? NamedTextColor.GREEN : ping < 150 ? NamedTextColor.YELLOW : NamedTextColor.RED;
        Component value = Component.text(ping, color);
        if (target.equals(sender)) {
            send(sender, "utility.ping.self", Placeholder.component("ping", value));
        } else {
            send(sender, "utility.ping.other", Placeholder.component("ping", value),
                    Placeholder.component("player", target.displayName()));
        }
    }

    @Override
    protected List<String> complete(CommandSender sender, String label, String[] args) {
        if (args.length == 1 && sender.hasPermission("core.ping.others")) {
            return players(sender, args[0]);
        }
        return List.of();
    }
}
