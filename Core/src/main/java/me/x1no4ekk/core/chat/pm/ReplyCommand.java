package me.x1no4ekk.core.chat.pm;

import me.x1no4ekk.core.CorePlugin;
import me.x1no4ekk.core.chat.PrivateMessageModule;
import me.x1no4ekk.core.command.BaseCommand;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

/**
 * /reply &lt;message&gt; — answers the last conversation partner.
 */
public final class ReplyCommand extends BaseCommand {

    private final PrivateMessageModule module;

    public ReplyCommand(CorePlugin plugin, PrivateMessageModule module) {
        super(plugin);
        this.module = module;
    }

    @Override
    protected void execute(CommandSender sender, String label, String[] args) {
        Player player = playerOnly(sender);
        String text = String.join(" ", args).strip();
        if (text.isEmpty()) {
            throw usage("pm.reply-usage");
        }
        UUID targetId = module.service().replyTarget(player.getUniqueId());
        Player target = targetId == null ? null : Bukkit.getPlayer(targetId);
        if (target == null || !canSee(player, target)) {
            throw fail("pm.no-reply-target");
        }
        module.service().send(player, target, text);
    }

    @Override
    protected List<String> complete(CommandSender sender, String label, String[] args) {
        return args.length == 0 ? List.of() : players(sender, args[args.length - 1]);
    }
}
