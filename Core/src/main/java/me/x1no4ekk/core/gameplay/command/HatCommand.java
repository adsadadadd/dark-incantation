package me.x1no4ekk.core.gameplay.command;

import me.x1no4ekk.core.CorePlugin;
import me.x1no4ekk.core.command.BaseCommand;
import me.x1no4ekk.core.util.Inventories;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

/**
 * /hat — puts one held item on the head and gives the previous helmet back (empty hand: takes the helmet off).
 */
public final class HatCommand extends BaseCommand {

    public HatCommand(CorePlugin plugin) {
        super(plugin);
    }

    @Override
    protected void execute(CommandSender sender, String label, String[] args) {
        Player player = playerOnly(sender);
        requirePermission(sender, "core.hat");
        PlayerInventory inventory = player.getInventory();
        ItemStack hand = inventory.getItemInMainHand();
        ItemStack helmet = inventory.getHelmet();
        boolean hasHelmet = helmet != null && !helmet.isEmpty();
        if (hand.isEmpty() && !hasHelmet) {
            throw fail("general.no-item-in-hand");
        }
        if (hasHelmet && helmet.containsEnchantment(Enchantment.BINDING_CURSE) && player.getGameMode() != GameMode.CREATIVE) {
            throw fail("gameplay.hat.binding");
        }

        if (hand.isEmpty()) {
            inventory.setHelmet(null);
            inventory.setItemInMainHand(helmet);
            send(player, "gameplay.hat.removed");
            return;
        }
        ItemStack newHelmet = hand.asOne();
        if (hand.getAmount() > 1) {
            inventory.setItemInMainHand(hand.asQuantity(hand.getAmount() - 1));
            inventory.setHelmet(newHelmet);
            if (hasHelmet) {
                Inventories.giveOrDrop(player, helmet);
            }
        } else {
            inventory.setHelmet(newHelmet);
            inventory.setItemInMainHand(hasHelmet ? helmet : null);
        }
        send(player, "gameplay.hat.done", Placeholder.component("item", newHelmet.effectiveName()));
    }
}
