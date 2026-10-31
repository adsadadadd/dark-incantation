package me.x1no4ekk.core.utility.moderation;

import me.x1no4ekk.core.CorePlugin;
import me.x1no4ekk.core.command.BaseCommand;
import me.x1no4ekk.core.utility.UtilityText;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerKickEvent;

import java.util.Arrays;
import java.util.List;

/**
 * {@code /kick <player> [reason...]}.
 */
final class KickCommand extends BaseCommand {

    private static final String NOTIFY = "core.kick.notify";

    KickCommand(CorePlugin plugin) {
        super(plugin);
    }

    @Override
    protected void execute(CommandSender sender, String label, String[] args) {
        requirePermission(sender, "core.kick");
        if (args.length < 1) {
            throw usage("utility.kick.usage");
        }
        Player target = player(sender, args[0]);
        if (target.equals(sender)) {
            throw fail("utility.kick.self");
        }
        // The console may kick anyone, e.g. to remove a misbehaving administrator.
        if (sender instanceof Player && target.hasPermission("core.kick.exempt")) {
            throw fail("utility.kick.exempt", Placeholder.component("player", target.displayName()));
        }
        Component reason;
        if (args.length < 2) {
            reason = messages().get("utility.kick.default-reason");
        } else {
            String raw = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
            reason = sender.hasPermission("core.kick.color") ? UtilityText.styled(raw) : Component.text(raw);
        }
        TagResolver placeholders = TagResolver.resolver(
                Placeholder.component("player", target.displayName()),
                Placeholder.component("staff", UtilityText.senderName(messages(), sender)),
                Placeholder.component("reason", reason));
        target.kick(messages().get("utility.kick.screen", placeholders), PlayerKickEvent.Cause.KICK_COMMAND);
        send(sender, "utility.kick.success", placeholders);
        for (Player staff : Bukkit.getOnlinePlayers()) {
            if (!staff.equals(sender) && staff.hasPermission(NOTIFY)) {
                send(staff, "utility.kick.notify", placeholders);
            }
        }
        if (sender instanceof Player) {
            send(Bukkit.getConsoleSender(), "utility.kick.notify", placeholders);
        }
    }

    @Override
    protected List<String> complete(CommandSender sender, String label, String[] args) {
        return args.length == 1 ? players(sender, args[0]) : List.of();
    }
}
