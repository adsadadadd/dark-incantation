package me.x1no4ekk.core.utility.info;

import me.x1no4ekk.core.CorePlugin;
import me.x1no4ekk.core.command.BaseCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * {@code /getpos [player]}: coordinates; clicking the message copies them.
 */
final class GetPosCommand extends BaseCommand {

    GetPosCommand(CorePlugin plugin) {
        super(plugin);
    }

    @Override
    protected void execute(CommandSender sender, String label, String[] args) {
        requirePermission(sender, "core.getpos");
        if (args.length > 1) {
            throw usage("utility.getpos.usage");
        }
        Player target = args.length == 0 ? playerOnly(sender) : otherPlayer(sender, args[0], "core.getpos.others");
        String key = target.equals(sender) ? "utility.getpos.self" : "utility.getpos.other";
        if (!messages().isEnabled(key)) {
            return;
        }
        Location location = target.getLocation();
        String coordinates = location.getBlockX() + " " + location.getBlockY() + " " + location.getBlockZ();
        Component message = messages().get(key,
                Placeholder.component("player", target.displayName()),
                placeholder("world", location.getWorld().getName()),
                placeholder("x", location.getBlockX()),
                placeholder("y", location.getBlockY()),
                placeholder("z", location.getBlockZ()),
                placeholder("yaw", Math.round(location.getYaw())),
                placeholder("pitch", Math.round(location.getPitch())));
        sender.sendMessage(message
                .clickEvent(ClickEvent.copyToClipboard(coordinates))
                .hoverEvent(HoverEvent.showText(messages().get("utility.getpos.hover"))));
    }

    @Override
    protected List<String> complete(CommandSender sender, String label, String[] args) {
        if (args.length == 1 && sender.hasPermission("core.getpos.others")) {
            return players(sender, args[0]);
        }
        return List.of();
    }
}
