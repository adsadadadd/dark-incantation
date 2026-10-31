package me.x1no4ekk.core.gameplay.command;

import me.x1no4ekk.core.CorePlugin;
import me.x1no4ekk.core.gameplay.GameplayModule;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * /clearinventory [player] — clears the whole inventory including armour, off hand and cursor.
 */
public final class ClearInventoryCommand extends TargetCommand {

    public ClearInventoryCommand(CorePlugin plugin, GameplayModule module) {
        super(plugin, module, "core.clearinventory", "gameplay.clearinventory.usage");
    }

    @Override
    protected void apply(CommandSender sender, Player target, boolean self) {
        target.getInventory().clear();
        target.setItemOnCursor(null);
        report(sender, target, self, "gameplay.clearinventory.self", "gameplay.clearinventory.other");
    }
}
