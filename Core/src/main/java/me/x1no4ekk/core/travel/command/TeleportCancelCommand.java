package me.x1no4ekk.core.travel.command;

import me.x1no4ekk.core.CorePlugin;
import me.x1no4ekk.core.command.BaseCommand;
import me.x1no4ekk.core.teleport.TeleportResult;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * /teleportcancel — aborts the sender's own pending teleport warmup.
 */
public final class TeleportCancelCommand extends BaseCommand {

    private static final String PERMISSION = "core.teleportcancel";

    public TeleportCancelCommand(CorePlugin plugin) {
        super(plugin);
    }

    @Override
    protected void execute(CommandSender sender, String label, String[] args) {
        Player player = playerOnly(sender);
        requirePermission(sender, PERMISSION);
        if (args.length != 0) {
            throw usage("travel.teleportcancel.usage");
        }
        if (!plugin.teleportService().cancel(player.getUniqueId(), TeleportResult.CANCELLED, null)) {
            throw fail("travel.teleportcancel.none");
        }
        send(player, "travel.teleportcancel.cancelled");
    }
}
