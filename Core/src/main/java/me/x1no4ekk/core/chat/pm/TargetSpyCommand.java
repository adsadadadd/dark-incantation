package me.x1no4ekk.core.chat.pm;

import me.x1no4ekk.core.CorePlugin;
import me.x1no4ekk.core.chat.ChatPermissions;
import me.x1no4ekk.core.chat.PrivateMessageModule;
import me.x1no4ekk.core.command.BaseCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * /targetspy &lt;player&gt; — toggles watching the commands of a player; /targetspy off — stops every watch.
 */
public final class TargetSpyCommand extends BaseCommand {

    private static final String OFF = "off";

    private final PrivateMessageModule module;

    public TargetSpyCommand(CorePlugin plugin, PrivateMessageModule module) {
        super(plugin);
        this.module = module;
    }

    @Override
    protected void execute(CommandSender sender, String label, String[] args) {
        Player player = playerOnly(sender);
        requirePermission(player, ChatPermissions.TARGET_SPY);
        if (args.length != 1) {
            throw usage("pm.targetspy-usage");
        }
        if (args[0].equalsIgnoreCase(OFF)) {
            int stopped = module.targetSpy().stopAll(player.getUniqueId());
            send(player, stopped > 0 ? "pm.targetspy-off-all" : "pm.targetspy-none");
            return;
        }
        Player target = player(sender, args[0]);
        if (target.equals(player)) {
            throw fail("pm.targetspy-self");
        }
        boolean watching = module.targetSpy().toggle(player.getUniqueId(), target.getUniqueId());
        send(player, watching ? "pm.targetspy-on" : "pm.targetspy-off", placeholder("player", target.getName()));
    }

    @Override
    protected List<String> complete(CommandSender sender, String label, String[] args) {
        if (args.length != 1 || !sender.hasPermission(ChatPermissions.TARGET_SPY)) {
            return List.of();
        }
        List<String> options = new ArrayList<>(filter(List.of(OFF), args[0]));
        options.addAll(players(sender, args[0]));
        return options;
    }
}
