package me.x1no4ekk.core.gameplay.command;

import me.x1no4ekk.core.CorePlugin;
import me.x1no4ekk.core.gameplay.GameplayModule;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * /flyelytra [player] — toggles flight while wearing an elytra (costs elytra durability). Not persisted.
 */
public final class FlyElytraCommand extends TargetCommand {

    public FlyElytraCommand(CorePlugin plugin, GameplayModule module) {
        super(plugin, module, "core.flyelytra", "gameplay.flyelytra.usage");
    }

    @Override
    protected void apply(CommandSender sender, Player target, boolean self) {
        if (!module.settings().flyElytra().enabled()) {
            throw fail("gameplay.flyelytra.feature-disabled");
        }
        boolean enable = !module.flight().isElytraFly(target.getUniqueId());
        module.flight().setElytraFly(target, enable);
        report(sender, target, self,
                enable ? "gameplay.flyelytra.enabled" : "gameplay.flyelytra.disabled",
                enable ? "gameplay.flyelytra.enabled-other" : "gameplay.flyelytra.disabled-other");
    }
}
