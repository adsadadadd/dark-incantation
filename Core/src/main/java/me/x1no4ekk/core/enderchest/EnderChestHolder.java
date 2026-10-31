package me.x1no4ekk.core.enderchest;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Holder of one editable ender chest page. Like {@link me.x1no4ekk.core.gui.Gui}, it is referenced only by its own
 * inventory (and, while the page has viewers, by its {@link EnderChestSession}).
 */
final class EnderChestHolder implements InventoryHolder {

    private final UUID owner;
    private final int page;
    private final int storageSize;
    /** Stored items beyond {@link #storageSize} (after {@code storage-rows} was reduced); written back unchanged. */
    private final ItemStack[] overflow;
    private Inventory inventory;

    EnderChestHolder(UUID owner, int page, int storageSize, ItemStack[] overflow) {
        this.owner = owner;
        this.page = page;
        this.storageSize = storageSize;
        this.overflow = overflow;
    }

    void attach(Inventory inventory) {
        this.inventory = inventory;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

    UUID owner() {
        return owner;
    }

    int page() {
        return page;
    }

    int storageSize() {
        return storageSize;
    }

    ItemStack[] overflow() {
        return overflow;
    }
}
