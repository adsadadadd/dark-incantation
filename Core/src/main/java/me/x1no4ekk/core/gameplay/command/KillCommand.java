package me.x1no4ekk.core.gameplay.command;

import me.x1no4ekk.core.CorePlugin;
import me.x1no4ekk.core.command.BaseCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * /kill &lt;player&gt; — kills a player. Setting health directly bypasses god mode and totems on purpose.
 * Vanilla /kill with selectors stays available as /minecraft:kill.
 */
public final class KillCommand extends BaseCommand {

    public KillCommand(CorePlugin plugin) {
        super(plugin);
    }

    @Override
    protected void execute(CommandSender sender, String label, String[] args) {
        requirePermission(sender, "core.kill");
        if (args.length != 1) {
            throw usage("gameplay.kill.usage");
        }
        Player target = player(sender, args[0]);
        if (target.isDead()) {
            throw fail("gameplay.kill.already-dead", placeholder("player", target.getName()));
        }
        target.setHealth(0.0);
        send(sender, "gameplay.kill.done", placeholder("player", target.getName()));
    }

    @Override
    protected List<String> complete(CommandSender sender, String label, String[] args) {
        return args.length == 1 && sender.hasPermission("core.kill") ? players(sender, args[0]) : List.of();
    }
}
