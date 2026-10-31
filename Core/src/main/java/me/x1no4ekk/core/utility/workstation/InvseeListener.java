package me.x1no4ekk.core.utility.workstation;

import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;

/**
 * Makes {@code /invsee} views read-only and closes them when the inspected player leaves.
 * Views are recognised by their top inventory: another player's {@link org.bukkit.inventory.PlayerInventory},
 * so no per-viewer state is kept.
 */
final class InvseeListener implements Listener {

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onClick(InventoryClickEvent event) {
        if (isReadOnly(event.getView(), event.getWhoClicked())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onDrag(InventoryDragEvent event) {
        if (isReadOnly(event.getView(), event.getWhoClicked())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onQuit(PlayerQuitEvent event) {
        Player leaving = event.getPlayer();
        for (Player viewer : Bukkit.getOnlinePlayers()) {
            if (!viewer.equals(leaving) && ownerOf(viewer.getOpenInventory().getTopInventory()) == leaving) {
                viewer.closeInventory();
            }
        }
    }

    /**
     * Closes every open {@code /invsee} view; their read-only protection ends with this listener.
     */
    static void closeAll() {
        for (Player viewer : Bukkit.getOnlinePlayers()) {
            HumanEntity owner = ownerOf(viewer.getOpenInventory().getTopInventory());
            if (owner != null && !owner.equals(viewer)) {
                viewer.closeInventory();
            }
        }
    }

    private static boolean isReadOnly(InventoryView view, HumanEntity viewer) {
        HumanEntity owner = ownerOf(view.getTopInventory());
        return owner != null && !owner.equals(viewer) && !viewer.hasPermission(InvseeCommand.MODIFY);
    }

    /**
     * Owner of a player inventory shown as a container; {@code null} for any other inventory.
     */
    private static HumanEntity ownerOf(Inventory top) {
        // Type check first: getHolder() of block inventories would create a block-state snapshot.
        if (top.getType() != InventoryType.PLAYER) {
            return null;
        }
        return top.getHolder(false) instanceof HumanEntity owner ? owner : null;
    }
}
