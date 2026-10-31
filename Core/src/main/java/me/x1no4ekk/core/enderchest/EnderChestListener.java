package me.x1no4ekk.core.enderchest;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.PlayerInventory;

/**
 * Routes inventory events of ender chest pages to the service and optionally replaces the vanilla ender chest block.
 */
final class EnderChestListener implements Listener {

    private final EnderChestModule module;
    private final EnderChestService service;

    EnderChestListener(EnderChestModule module, EnderChestService service) {
        this.module = module;
        this.service = service;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onClick(InventoryClickEvent event) {
        if (!(event.getView().getTopInventory().getHolder(false) instanceof EnderChestHolder holder)) {
            return;
        }
        if (event.getWhoClicked() instanceof Player player) {
            service.handleClick(player, holder, event);
        } else {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDrag(InventoryDragEvent event) {
        if (!(event.getView().getTopInventory().getHolder(false) instanceof EnderChestHolder holder)) {
            return;
        }
        if (event.getWhoClicked() instanceof Player player) {
            service.handleDrag(player, holder, event);
        } else {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onClose(InventoryCloseEvent event) {
        if (event.getInventory().getHolder(false) instanceof EnderChestHolder holder) {
            service.handleClose(event.getPlayer().getUniqueId(), holder);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        service.handleQuit(event.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getHand() != EquipmentSlot.HAND
                || event.useInteractedBlock() == Event.Result.DENY || !module.settings().replaceVanilla()) {
            return;
        }
        Block block = event.getClickedBlock();
        if (block == null || block.getType() != Material.ENDER_CHEST) {
            return;
        }
        Player player = event.getPlayer();
        PlayerInventory inventory = player.getInventory();
        // Vanilla: sneaking with an item uses the item instead of opening the chest.
        if (player.isSneaking() && (!inventory.getItemInMainHand().isEmpty() || !inventory.getItemInOffHand().isEmpty())) {
            return;
        }
        if (!player.hasPermission(EnderChestService.PERMISSION)) {
            return;
        }
        event.setCancelled(true);
        service.open(player, player.getUniqueId(), player.getName());
    }
}
