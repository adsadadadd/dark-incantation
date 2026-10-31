package me.x1no4ekk.core.gameplay.command;

import me.x1no4ekk.core.CorePlugin;
import me.x1no4ekk.core.gameplay.GameplayModule;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * /heal [player] — full health, hunger, no fire and (optionally) no harmful effects.
 */
public final class HealCommand extends TargetCommand {

    public HealCommand(CorePlugin plugin, GameplayModule module) {
        super(plugin, module, "core.heal", "gameplay.heal.usage");
    }

    @Override
    protected void apply(CommandSender sender, Player target, boolean self) {
        if (target.isDead()) {
            throw fail("gameplay.heal.dead", placeholder("player", target.getName()));
        }
        PlayerCare.restoreHealth(target);
        PlayerCare.feed(target);
        if (module.settings().healRemoveNegativeEffects()) {
            PlayerCare.removeHarmfulEffects(target);
        }
        report(sender, target, self, "gameplay.heal.self", "gameplay.heal.other");
    }
}
