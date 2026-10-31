package me.x1no4ekk.core.util;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Inventory helpers.
 */
public final class Inventories {

    private Inventories() {
    }

    /**
     * Checks whether all items fit into the player's storage slots (main inventory + hotbar)
     * without changing it: the items are added to a temporary copy.
     */
    public static boolean canFit(Player player, Collection<ItemStack> items) {
        ItemStack[] storage = player.getInventory().getStorageContents();
        Inventory copy = Bukkit.createInventory(null, ((storage.length + 8) / 9) * 9);
        for (int i = 0; i < storage.length; i++) {
            if (storage[i] != null) {
                copy.setItem(i, storage[i].clone());
            }
        }
        ItemStack[] toAdd = items.stream()
                .filter(item -> item != null && !item.getType().isAir())
                .map(ItemStack::clone)
                .toArray(ItemStack[]::new);
        return copy.addItem(toAdd).isEmpty();
    }

    public static boolean canFit(Player player, ItemStack... items) {
        return canFit(player, List.of(items));
    }

    /**
     * Adds the items to the inventory; what does not fit is dropped at the player's feet.
     *
     * @return {@code true} if something had to be dropped
     */
    public static boolean giveOrDrop(Player player, Collection<ItemStack> items) {
        ItemStack[] toAdd = items.stream()
                .filter(item -> item != null && !item.getType().isAir())
                .map(ItemStack::clone)
                .toArray(ItemStack[]::new);
        if (toAdd.length == 0) {
            return false;
        }
        Map<Integer, ItemStack> leftover = player.getInventory().addItem(toAdd);
        leftover.values().forEach(item -> player.getWorld().dropItemNaturally(player.getLocation(), item));
        return !leftover.isEmpty();
    }

    public static boolean giveOrDrop(Player player, ItemStack... items) {
        return giveOrDrop(player, List.of(items));
    }
}
