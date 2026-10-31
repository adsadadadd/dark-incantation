package me.x1no4ekk.core.gui;

import me.x1no4ekk.core.CorePlugin;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Base menu built on the {@link InventoryHolder} pattern.
 * <p>
 * The holder is referenced only by its own inventory, and the inventory only by the viewers' open views.
 * There is no global registry and no update thread: once the last viewer closes the menu, the holder,
 * its inventory and all click handlers become unreachable and are collected by the GC.
 * Implementations must store viewer UUIDs, never {@link Player} instances.
 */
public abstract class Gui implements InventoryHolder {

    protected final Inventory inventory;
    private final Map<Integer, GuiAction> actions = new HashMap<>();

    protected Gui(int rows, Component title) {
        this.inventory = Bukkit.createInventory(this, Math.clamp(rows, 1, 6) * 9, title);
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

    public void open(Player player) {
        player.openInventory(inventory);
    }

    protected void setItem(int slot, ItemStack item) {
        inventory.setItem(slot, item);
        actions.remove(slot);
    }

    protected void setItem(int slot, ItemStack item, GuiAction action) {
        inventory.setItem(slot, item);
        if (action == null) {
            actions.remove(slot);
        } else {
            actions.put(slot, action);
        }
    }

    protected void clear() {
        inventory.clear();
        actions.clear();
    }

    /**
     * Fills every empty slot with {@code filler} (no click action).
     */
    protected void fillEmpty(ItemStack filler) {
        for (int slot = 0; slot < inventory.getSize(); slot++) {
            ItemStack current = inventory.getItem(slot);
            if (current == null || current.getType().isAir()) {
                inventory.setItem(slot, filler);
            }
        }
    }

    /**
     * Every click while the menu is open is cancelled; only clicks on the top inventory reach actions.
     */
    void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);
        int rawSlot = event.getRawSlot();
        // Raw slots below the top size belong to this menu; everything else is the player's inventory.
        if (rawSlot < 0 || rawSlot >= inventory.getSize() || !(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        GuiAction action = actions.get(rawSlot);
        if (action != null) {
            action.onClick(player, event);
        }
    }

    void handleDrag(InventoryDragEvent event) {
        int topSize = inventory.getSize();
        for (int rawSlot : event.getRawSlots()) {
            if (rawSlot < topSize) {
                event.setCancelled(true);
                return;
            }
        }
    }

    /**
     * Hook for subclasses; called when a viewer closes the menu.
     */
    protected void onClose(InventoryCloseEvent event) {
    }

    /**
     * Runs {@code action} on the next tick if the player is still online. Use it for anything that closes or
     * replaces the open inventory, which must not happen inside {@code InventoryClickEvent}.
     * Only the UUID is captured.
     */
    public static void nextTick(Player player, Consumer<Player> action) {
        UUID uuid = player.getUniqueId();
        Bukkit.getScheduler().runTask(JavaPlugin.getPlugin(CorePlugin.class), () -> {
            Player online = Bukkit.getPlayer(uuid);
            if (online != null) {
                action.accept(online);
            }
        });
    }

    /**
     * Closes the player's inventory on the next tick.
     */
    public static void closeLater(Player player) {
        nextTick(player, Player::closeInventory);
    }
}
