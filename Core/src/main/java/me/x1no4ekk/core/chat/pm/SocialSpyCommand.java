package me.x1no4ekk.core.chat.pm;

import me.x1no4ekk.core.CorePlugin;
import me.x1no4ekk.core.chat.ChatPermissions;
import me.x1no4ekk.core.command.BaseCommand;
import me.x1no4ekk.core.user.User;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * /socialspy — shows every private message to the staff member.
 */
public final class SocialSpyCommand extends BaseCommand {

    public SocialSpyCommand(CorePlugin plugin) {
        super(plugin);
    }

    @Override
    protected void execute(CommandSender sender, String label, String[] args) {
        Player player = playerOnly(sender);
        requirePermission(player, ChatPermissions.SOCIAL_SPY);
        User user = user(player);
        boolean enabled = !user.isSocialSpy();
        user.setSocialSpy(enabled);
        send(player, enabled ? "pm.socialspy-on" : "pm.socialspy-off");
    }
}
