/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Material
 *  org.bukkit.entity.Player
 *  org.bukkit.inventory.ItemStack
 *  org.bukkit.inventory.meta.ItemMeta
 */
package ru.bestrax.essentials.gui;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import ru.bestrax.essentials.api.EssentialsApi;
import ru.bestrax.essentials.api.inventory.GItem;
import ru.bestrax.essentials.api.inventory.type.GInventory;
import ru.bestrax.essentials.base.inventory.impl.types.UpdatableGui;
import ru.bestrax.essentials.utils.BukkitUtils;
import ru.bestrax.essentials.utils.ItemBuilder;

public class InvseeGui
extends UpdatableGui<GInventory> {
    private Player target;
    private int heldItemSlot;

    public InvseeGui(Player player, Player target) {
        super(player);
        this.target = target;
        this.setOnClickAction((clicker, clickType, slot) -> {
            if (player.hasPermission("trustessentials.invsee.change")) {
                new Thread(() -> {
                    try {
                        Thread.sleep(1L);
                    }
                    catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    this.updateTargetInventory();
                }).start();
                return slot >= 36 && slot <= 44 || slot == this.heldItemSlot;
            }
            return true;
        });
        this.createInventory();
        this.setActions();
    }

    @Override
    protected void createInventory() {
        this.inventory = INVENTORY_API.createInventory(this.player, EssentialsApi.getInstance().getLang().getString("INVSEE_NAME", new String[0]).replace("%name%", this.target.getName()), 6);
    }

    @Override
    protected void setStaticItems() {
        ItemStack glassItem = new ItemStack(BukkitUtils.getMaterial("STAINED_GLASS_PANE"), 1, 1);
        ItemMeta itemMeta = glassItem.getItemMeta();
        itemMeta.setDisplayName("\u00a76");
        glassItem.setItemMeta(itemMeta);
        for (int i = 0; i < 9; ++i) {
            ((GInventory)this.inventory).setItem(36 + i, new GItem(glassItem.clone()));
        }
    }

    private void updateTargetInventory() {
        ItemStack[] contents = new ItemStack[36];
        for (int i = 0; i < contents.length; ++i) {
            if (i == this.heldItemSlot) continue;
            contents[i] = ((GInventory)this.inventory).getInventory().getItem(i);
        }
        this.target.getInventory().setContents(contents);
        this.target.getInventory().setHelmet(((GInventory)this.inventory).getInventory().getItem(45));
        this.target.getInventory().setChestplate(((GInventory)this.inventory).getInventory().getItem(46));
        this.target.getInventory().setLeggings(((GInventory)this.inventory).getInventory().getItem(47));
        this.target.getInventory().setBoots(((GInventory)this.inventory).getInventory().getItem(48));
        this.target.getInventory().setItemInMainHand(((GInventory)this.inventory).getInventory().getItem(49));
        this.target.getInventory().setItemInOffHand(((GInventory)this.inventory).getInventory().getItem(50));
    }

    @Override
    protected void setDynamicItems() {
        this.heldItemSlot = this.target.getInventory().getHeldItemSlot();
        if (!this.target.isOnline()) {
            this.player.closeInventory();
            return;
        }
        int i = 0;
        for (ItemStack content : this.target.getInventory().getContents()) {
            if (i >= 36) break;
            GItem item = i == this.heldItemSlot ? new GItem(ItemBuilder.builder(Material.BARRIER).setName(EssentialsApi.getInstance().getLang().getString("SELECTED_ITEM", new String[0])).build()) : new GItem(content);
            ((GInventory)this.inventory).setItem(i, item);
            ++i;
        }
        ((GInventory)this.inventory).setItem(45, new GItem(this.target.getInventory().getHelmet()));
        ((GInventory)this.inventory).setItem(46, new GItem(this.target.getInventory().getChestplate()));
        ((GInventory)this.inventory).setItem(47, new GItem(this.target.getInventory().getLeggings()));
        ((GInventory)this.inventory).setItem(48, new GItem(this.target.getInventory().getBoots()));
        ((GInventory)this.inventory).setItem(49, new GItem(this.target.getInventory().getItemInMainHand()));
        ((GInventory)this.inventory).setItem(50, new GItem(this.target.getInventory().getItemInOffHand()));
    }
}
