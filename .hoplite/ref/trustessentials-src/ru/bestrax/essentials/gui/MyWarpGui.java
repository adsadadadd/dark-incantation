/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Material
 *  org.bukkit.configuration.file.FileConfiguration
 *  org.bukkit.entity.Player
 *  org.bukkit.inventory.ItemStack
 */
package ru.bestrax.essentials.gui;

import java.util.List;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ru.bestrax.essentials.api.EssentialsApi;
import ru.bestrax.essentials.api.Warp;
import ru.bestrax.essentials.api.config.Config;
import ru.bestrax.essentials.api.inventory.GItem;
import ru.bestrax.essentials.api.inventory.type.MultiInventory;
import ru.bestrax.essentials.base.inventory.impl.types.AbstractGui;
import ru.bestrax.essentials.utils.BukkitUtils;
import ru.bestrax.essentials.utils.ItemBuilder;

public class MyWarpGui
extends AbstractGui<MultiInventory> {
    public MyWarpGui(Player player) {
        super(player);
        this.createInventory();
    }

    @Override
    protected void createInventory() {
        Config LANG_CONFIG = EssentialsApi.getInstance().getLang();
        this.inventory = INVENTORY_API.createMultiInventory(LANG_CONFIG.getString("GUI_NAME", new String[0]) + " \u25b8 " + LANG_CONFIG.getString("WARP_GUI_NAME", new String[0]) + " \u25b8 " + LANG_CONFIG.getString("MYWARP_GUI_NAME", new String[0]), 6);
    }

    @Override
    public void setStaticItems() {
        Config LANG_CONFIG = EssentialsApi.getInstance().getLang();
        List<Warp> warps = EssentialsApi.getWarpManager().getWarps(this.player.getName());
        FileConfiguration guiConfig = EssentialsApi.getInstance().getGuiConfiguration();
        int size = warps.size();
        if (size == 0) {
            ((MultiInventory)this.inventory).setItem(0, 22, new GItem(ItemBuilder.builder(Material.GLASS_BOTTLE).setName(LANG_CONFIG.getString("MYWARP_ITEM_EMPTY_NAME", new String[0])).setLore(LANG_CONFIG.getStringList("MYWARP_ITEM_EMPTY_LORE", new String[0])).build(), (player, clickType, slot1) -> player.playSound(player.getLocation(), BukkitUtils.getBlockBellSound(), 1.0f, 1.0f)));
            INVENTORY_API.items(((MultiInventory)this.inventory).getInventories().get(0), (List<String>)guiConfig.getStringList("myWarp.items"));
            INVENTORY_API.backButton((MultiInventory)this.inventory, (player1, clickType, i) -> player1.chat("/warp"), 49);
        } else {
            int startSlot = guiConfig.getInt("myWarp.startSlot");
            int pageNum = 0;
            int slot = startSlot;
            INVENTORY_API.items((MultiInventory)this.inventory, pageNum, guiConfig.getStringList("myWarp.items"));
            for (Warp warp : warps) {
                if (((MultiInventory)this.inventory).getInventories().size() == 0) continue;
                ItemStack itemWarp = ItemBuilder.builder(BukkitUtils.getMaterial("SIGN")).setName("\u00a7a" + warp.getName()).setLore(LANG_CONFIG.getStringList("MYWARP_ITEM_LORE", "%grade%", String.valueOf(warp.getGrade() > 0 ? Integer.valueOf(warp.getGrade()) : LANG_CONFIG.getString("NO_GRADE", new String[0])))).build();
                ((MultiInventory)this.inventory).setItem(pageNum, slot, new GItem(itemWarp, (player, clickType, i) -> {
                    warp.teleport(player);
                    player.playSound(player.getLocation(), BukkitUtils.getBlockBellSound(), 1.0f, 1.0f);
                }));
                boolean nextRow = false;
                for (String row : guiConfig.getStringList("myWarp.rows")) {
                    int newSlot;
                    int endSlot;
                    try {
                        endSlot = Integer.parseInt(row.split(":")[0]);
                        newSlot = Integer.parseInt(row.split(":")[1]);
                    }
                    catch (NumberFormatException e) {
                        continue;
                    }
                    if (slot != endSlot) continue;
                    if (newSlot == startSlot) {
                        slot = newSlot;
                        INVENTORY_API.items((MultiInventory)this.inventory, ++pageNum, guiConfig.getStringList("myWarp.items"));
                    } else {
                        slot = newSlot;
                    }
                    nextRow = true;
                }
                if (nextRow) continue;
                ++slot;
            }
            INVENTORY_API.backButton((MultiInventory)this.inventory, (player1, clickType, i) -> player1.chat("/warp"), guiConfig.getInt("myWarp.backSlot"));
        }
        INVENTORY_API.pageButton((MultiInventory)this.getInventory(), guiConfig.getInt("myWarp.downSlot"), guiConfig.getInt("myWarp.upSlot"));
    }
}
