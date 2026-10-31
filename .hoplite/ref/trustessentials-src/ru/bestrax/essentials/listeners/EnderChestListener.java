/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Material
 *  org.bukkit.entity.Player
 *  org.bukkit.event.EventHandler
 *  org.bukkit.event.EventPriority
 *  org.bukkit.event.Listener
 *  org.bukkit.event.block.Action
 *  org.bukkit.event.inventory.InventoryClickEvent
 *  org.bukkit.event.inventory.InventoryCloseEvent
 *  org.bukkit.event.inventory.InventoryInteractEvent
 *  org.bukkit.event.player.PlayerInteractEvent
 *  org.bukkit.inventory.Inventory
 *  org.bukkit.inventory.ItemStack
 */
package ru.bestrax.essentials.listeners;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import ru.bestrax.essentials.api.CustomEnderChest;
import ru.bestrax.essentials.api.EssentialsApi;
import ru.bestrax.essentials.api.User;
import ru.bestrax.essentials.api.inventory.GItem;
import ru.bestrax.essentials.api.inventory.action.ClickAction;
import ru.bestrax.essentials.api.inventory.action.ClickActionWithCursor;
import ru.bestrax.essentials.api.manager.EnderChestManager;
import ru.bestrax.essentials.api.manager.UserManager;

public class EnderChestListener
implements Listener {
    private final UserManager userManager = EssentialsApi.getUserManager();
    private final EnderChestManager enderChestManager = EssentialsApi.getEnderChestManager();

    @EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=true)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        if (event.getClickedBlock() == null || event.getClickedBlock().getType() != Material.ENDER_CHEST) {
            return;
        }
        ItemStack hand = event.getPlayer().getInventory().getItemInMainHand();
        if (event.getPlayer().isSneaking()) {
            return;
        }
        User user = this.userManager.getUser(event.getPlayer());
        if (user != null) {
            if (event.getClickedBlock().hasMetadata("antiBreak")) {
                return;
            }
            event.setCancelled(true);
            this.enderChestManager.openEnderChest(event.getPlayer(), user.getEnderChest());
        }
    }

    @EventHandler(priority=EventPriority.MONITOR)
    public void onClick(InventoryClickEvent e) {
        CustomEnderChest inventory = this.getInventory((InventoryInteractEvent)e);
        if (inventory == null) {
            return;
        }
        Inventory inv = e.getView().getTopInventory();
        if (!e.getView().getTitle().equals(inventory.getName())) {
            return;
        }
        int slot = e.getRawSlot();
        if (!inventory.getPlayerName().equalsIgnoreCase(((Player)e.getWhoClicked()).getName()) && !((Player)e.getWhoClicked()).hasPermission("trustessentials.enderchest.other.change")) {
            e.setCancelled(true);
        }
        if (slot < (EssentialsApi.getInstance().getSettings().getBoolean("pumpingEnderChest") ? 54 : inventory.getRows() * 9) && slot >= inventory.getRows() * 9) {
            e.setCancelled(true);
            GItem dItem = inventory.getImmutableItems().get(slot);
            if (dItem == null || inv.getItem(slot) == null) {
                return;
            }
            ClickAction clickAction = dItem.getClickAction();
            if (clickAction instanceof ClickActionWithCursor) {
                ((ClickActionWithCursor)clickAction).setCursor(e.getCursor());
            }
            clickAction.onClick((Player)e.getWhoClicked(), e.getClick(), slot);
            if (clickAction instanceof ClickActionWithCursor) {
                e.setCursor(((ClickActionWithCursor)clickAction).getCursor());
            }
        }
    }

    private CustomEnderChest getInventory(InventoryInteractEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) {
            e.setCancelled(true);
            return null;
        }
        return this.enderChestManager.getOpenedEnderChest((Player)e.getWhoClicked());
    }

    @EventHandler
    public void onCloseInv(InventoryCloseEvent e) {
        this.enderChestManager.closeEnderChest((Player)e.getPlayer());
    }
}
