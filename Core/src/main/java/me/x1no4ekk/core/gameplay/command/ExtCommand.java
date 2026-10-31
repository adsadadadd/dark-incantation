package me.x1no4ekk.core.gameplay.command;

import me.x1no4ekk.core.CorePlugin;
import me.x1no4ekk.core.gameplay.GameplayModule;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * /ext [player] — extinguishes fire.
 */
public final class ExtCommand extends TargetCommand {

    public ExtCommand(CorePlugin plugin, GameplayModule module) {
        super(plugin, module, "core.ext", "gameplay.ext.usage");
    }

    @Override
    protected void apply(CommandSender sender, Player target, boolean self) {
        target.setFireTicks(0);
        report(sender, target, self, "gameplay.ext.self", "gameplay.ext.other");
    }
}
