package me.x1no4ekk.core.gameplay.command;

import me.x1no4ekk.core.CorePlugin;
import me.x1no4ekk.core.gameplay.GameplayModule;
import me.x1no4ekk.core.user.User;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * /god [player] — toggles the persisted god mode (protection itself is in the gameplay listener).
 */
public final class GodCommand extends TargetCommand {

    public GodCommand(CorePlugin plugin, GameplayModule module) {
        super(plugin, module, GameplayModule.GOD_PERMISSION, "gameplay.god.usage");
    }

    @Override
    protected void apply(CommandSender sender, Player target, boolean self) {
        User user = user(target);
        boolean enable = !user.isGod();
        user.setGod(enable);
        if (enable && module.settings().god().healOnEnable() && !target.isDead()) {
            PlayerCare.restoreHealth(target);
            PlayerCare.feed(target);
        }
        report(sender, target, self,
                enable ? "gameplay.god.enabled" : "gameplay.god.disabled",
                enable ? "gameplay.god.enabled-other" : "gameplay.god.disabled-other");
    }
}
