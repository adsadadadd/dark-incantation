package me.x1no4ekk.core.chat.pm;

import me.x1no4ekk.core.CorePlugin;
import me.x1no4ekk.core.chat.PrivateMessageModule;
import me.x1no4ekk.core.command.BaseCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

/**
 * /msg &lt;player&gt; &lt;message&gt;
 */
public final class MsgCommand extends BaseCommand {

    private final PrivateMessageModule module;

    public MsgCommand(CorePlugin plugin, PrivateMessageModule module) {
        super(plugin);
        this.module = module;
    }

    @Override
    protected void execute(CommandSender sender, String label, String[] args) {
        Player player = playerOnly(sender);
        String text = args.length < 2 ? "" : String.join(" ", Arrays.copyOfRange(args, 1, args.length)).strip();
        if (text.isEmpty()) {
            throw usage("pm.msg-usage");
        }
        module.service().send(player, player(sender, args[0]), text);
    }

    @Override
    protected List<String> complete(CommandSender sender, String label, String[] args) {
        return args.length == 0 ? List.of() : players(sender, args[args.length - 1]);
    }
}
