package me.x1no4ekk.core.chat.announce;

import me.x1no4ekk.core.CorePlugin;
import me.x1no4ekk.core.command.BaseCommand;
import me.x1no4ekk.core.user.User;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * /bossbartoggle — shows or hides the boss bar announcements at once.
 */
public final class BossBarToggleCommand extends BaseCommand {

    private final BossBarRotator bossBars;

    public BossBarToggleCommand(CorePlugin plugin, BossBarRotator bossBars) {
        super(plugin);
        this.bossBars = bossBars;
    }

    @Override
    protected void execute(CommandSender sender, String label, String[] args) {
        Player player = playerOnly(sender);
        User user = user(player);
        boolean enabled = !user.isBossbarToggle();
        user.setBossbarToggle(enabled);
        if (enabled) {
            bossBars.show(player);
        } else {
            bossBars.hide(player);
        }
        send(player, enabled ? "announcer.bossbar-on" : "announcer.bossbar-off");
    }
}
