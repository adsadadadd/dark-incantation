package me.x1no4ekk.core.chat.command;

import me.x1no4ekk.core.CorePlugin;
import me.x1no4ekk.core.command.BaseCommand;
import me.x1no4ekk.core.config.Messages;
import me.x1no4ekk.core.user.User;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * /chattoggle — hides or shows the public chat for the player.
 */
public final class ChatToggleCommand extends BaseCommand {

    public ChatToggleCommand(CorePlugin plugin) {
        super(plugin);
    }

    @Override
    protected void execute(CommandSender sender, String label, String[] args) {
        Player player = playerOnly(sender);
        toggle(messages(), player, user(player));
    }

    static void toggle(Messages messages, Player player, User user) {
        boolean enabled = !user.isChatToggle();
        user.setChatToggle(enabled);
        messages.send(player, enabled ? "chat.toggle-on" : "chat.toggle-off");
    }
}
