package me.x1no4ekk.core.chat.pm;

import me.x1no4ekk.core.CorePlugin;
import me.x1no4ekk.core.command.BaseCommand;
import me.x1no4ekk.core.user.User;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * /msgtoggle — allows or blocks incoming private messages.
 */
public final class MsgToggleCommand extends BaseCommand {

    public MsgToggleCommand(CorePlugin plugin) {
        super(plugin);
    }

    @Override
    protected void execute(CommandSender sender, String label, String[] args) {
        Player player = playerOnly(sender);
        User user = user(player);
        boolean enabled = !user.isMsgToggle();
        user.setMsgToggle(enabled);
        send(player, enabled ? "pm.toggle-on" : "pm.toggle-off");
    }
}
