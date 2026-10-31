package me.x1no4ekk.core.kit;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * A kit from kits.yml. The stored item stacks are private: always hand out {@link #copyItems()}.
 *
 * @param id              lower-case key in kits.yml (also the key in core_kit_cooldowns)
 * @param cooldownSeconds {@code -1} — one-time, {@code 0} — no cooldown
 * @param price           price in АР; {@code 0} — not for sale
 * @param free            whether it can be claimed for free (with {@link #permission()})
 * @param permission      permission for the free claim; empty — everyone with {@code core.kit}
 * @param slot            explicit slot in the kit menu, or {@code -1} to place it automatically
 */
public record Kit(String id, String displayName, Material iconMaterial, String iconName, List<String> iconLore,
                  boolean iconGlint, int slot, long cooldownSeconds, double price, boolean free, String permission,
                  boolean autoEquip, boolean start, List<ItemStack> items) {

    public boolean isOneTime() {
        return cooldownSeconds < 0;
    }

    public boolean isBuyable() {
        return price > 0;
    }

    public boolean requiresPermission() {
        return !permission.isEmpty();
    }

    public List<ItemStack> copyItems() {
        List<ItemStack> copy = new ArrayList<>(items.size());
        for (ItemStack item : items) {
            copy.add(item.clone());
        }
        return copy;
    }
}
