package me.x1no4ekk.core.gameplay.command;

import me.x1no4ekk.core.CorePlugin;
import me.x1no4ekk.core.command.BaseCommand;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * /more — fills the held stack up to its maximum stack size.
 */
public final class MoreCommand extends BaseCommand {

    public MoreCommand(CorePlugin plugin) {
        super(plugin);
    }

    @Override
    protected void execute(CommandSender sender, String label, String[] args) {
        Player player = playerOnly(sender);
        requirePermission(sender, "core.more");
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.isEmpty()) {
            throw fail("general.no-item-in-hand");
        }
        int max = item.getMaxStackSize();
        if (item.getAmount() >= max) {
            throw fail("gameplay.more.full");
        }
        item.setAmount(max);
        player.getInventory().setItemInMainHand(item);
        send(player, "gameplay.more.done", Placeholder.component("item", item.effectiveName()), placeholder("amount", max));
    }
}
