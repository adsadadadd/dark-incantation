package me.x1no4ekk.core.gameplay.command;

import me.x1no4ekk.core.CorePlugin;
import me.x1no4ekk.core.command.BaseCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * /suicide — kills the sender.
 */
public final class SuicideCommand extends BaseCommand {

    public SuicideCommand(CorePlugin plugin) {
        super(plugin);
    }

    @Override
    protected void execute(CommandSender sender, String label, String[] args) {
        Player player = playerOnly(sender);
        requirePermission(sender, "core.suicide");
        if (player.isDead()) {
            return;
        }
        player.setHealth(0.0);
        send(player, "gameplay.suicide.done");
    }
}
