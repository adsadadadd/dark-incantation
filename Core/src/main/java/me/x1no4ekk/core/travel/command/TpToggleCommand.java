package me.x1no4ekk.core.travel.command;

import me.x1no4ekk.core.CorePlugin;
import me.x1no4ekk.core.command.BaseCommand;
import me.x1no4ekk.core.user.User;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * /tptoggle — allow or forbid teleports to (and requests for) the sender.
 */
public final class TpToggleCommand extends BaseCommand {

    private static final String PERMISSION = "core.tptoggle";

    public TpToggleCommand(CorePlugin plugin) {
        super(plugin);
    }

    @Override
    protected void execute(CommandSender sender, String label, String[] args) {
        Player player = playerOnly(sender);
        requirePermission(sender, PERMISSION);
        if (args.length != 0) {
            throw usage("travel.tptoggle.usage");
        }
        User user = user(player);
        boolean enabled = !user.isTpToggle();
        user.setTpToggle(enabled);
        send(player, enabled ? "travel.tptoggle.enabled" : "travel.tptoggle.disabled");
    }
}
