package me.x1no4ekk.core.utility.moderation;

import me.x1no4ekk.core.CorePlugin;
import me.x1no4ekk.core.command.BaseCommand;
import me.x1no4ekk.core.user.User;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * {@code /vanish [player]}: toggles invisibility.
 */
final class VanishCommand extends BaseCommand {

    private final ModerationModule module;

    VanishCommand(CorePlugin plugin, ModerationModule module) {
        super(plugin);
        this.module = module;
    }

    @Override
    protected void execute(CommandSender sender, String label, String[] args) {
        requirePermission(sender, "core.vanish");
        if (args.length > 1) {
            throw usage("utility.vanish.usage");
        }
        Player target = args.length == 0 ? playerOnly(sender) : otherPlayer(sender, args[0], "core.vanish.others");
        User user = user(target);
        boolean vanished = !user.isVanish();
        module.vanish().setVanished(target, user, vanished);
        send(target, vanished ? "utility.vanish.enabled" : "utility.vanish.disabled");
        if (!target.equals(sender)) {
            send(sender, vanished ? "utility.vanish.enabled-other" : "utility.vanish.disabled-other",
                    Placeholder.component("player", target.displayName()));
        }
    }

    @Override
    protected List<String> complete(CommandSender sender, String label, String[] args) {
        if (args.length == 1 && sender.hasPermission("core.vanish.others")) {
            return players(sender, args[0]);
        }
        return List.of();
    }
}
