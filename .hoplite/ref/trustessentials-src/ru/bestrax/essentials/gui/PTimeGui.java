/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Material
 *  org.bukkit.configuration.file.FileConfiguration
 *  org.bukkit.entity.Player
 */
package ru.bestrax.essentials.gui;

import java.util.List;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import ru.bestrax.essentials.api.EssentialsApi;
import ru.bestrax.essentials.api.config.Config;
import ru.bestrax.essentials.api.inventory.GItem;
import ru.bestrax.essentials.api.inventory.type.GInventory;
import ru.bestrax.essentials.base.inventory.impl.types.AbstractGui;
import ru.bestrax.essentials.utils.BukkitUtils;
import ru.bestrax.essentials.utils.ItemBuilder;

public class PTimeGui
extends AbstractGui<GInventory> {
    public PTimeGui(Player player) {
        super(player);
        this.createInventory();
    }

    @Override
    protected void createInventory() {
        Config LANG_CONFIG = EssentialsApi.getInstance().getLang();
        this.inventory = INVENTORY_API.createInventory(this.player, LANG_CONFIG.getString("GUI_NAME", new String[0]) + " \u25b8 " + LANG_CONFIG.getString("PTIME_GUI_NAME", new String[0]), 6);
    }

    @Override
    protected void setStaticItems() {
        FileConfiguration guiConfig = EssentialsApi.getInstance().getGuiConfiguration();
        INVENTORY_API.items((GInventory)this.inventory, (List<String>)guiConfig.getStringList("pTime.items"));
        Config LANG_CONFIG = EssentialsApi.getInstance().getLang();
        ((GInventory)this.inventory).setItem(guiConfig.getInt("pTime.morningSlot"), new GItem(ItemBuilder.builder(BukkitUtils.getMaterial("WATCH")).setName(LANG_CONFIG.getString("PTIME_ITEM_WATCH_NAME", new String[0])).setLore(LANG_CONFIG.getStringList("PTIME_ITEM_WATCH_LORE", "%time%", LANG_CONFIG.getString("MORNING", new String[0]))).build(), (player, clickType, i) -> {
            player.setPlayerTime(0L, false);
            player.sendMessage(LANG_CONFIG.getString("PTIME_CHANGE", "%ticks%", LANG_CONFIG.getString("MORNING", new String[0])));
            player.closeInventory();
        }));
        ((GInventory)this.inventory).setItem(guiConfig.getInt("pTime.daySlot"), new GItem(ItemBuilder.builder(BukkitUtils.getMaterial("WATCH")).setName(LANG_CONFIG.getString("PTIME_ITEM_WATCH_NAME", new String[0])).setLore(LANG_CONFIG.getStringList("PTIME_ITEM_WATCH_LORE", "%time%", LANG_CONFIG.getString("DAY", new String[0]))).build(), (player, clickType, i) -> {
            player.setPlayerTime(6000L, false);
            player.sendMessage(LANG_CONFIG.getString("PTIME_CHANGE", "%ticks%", LANG_CONFIG.getString("DAY", new String[0])));
            player.closeInventory();
        }));
        ((GInventory)this.inventory).setItem(guiConfig.getInt("pTime.nightSlot"), new GItem(ItemBuilder.builder(BukkitUtils.getMaterial("WATCH")).setName(LANG_CONFIG.getString("PTIME_ITEM_WATCH_NAME", new String[0])).setLore(LANG_CONFIG.getStringList("PTIME_ITEM_WATCH_LORE", "%time%", LANG_CONFIG.getString("NIGHT", new String[0]))).build(), (player, clickType, i) -> {
            player.setPlayerTime(18000L, false);
            player.sendMessage(LANG_CONFIG.getString("PTIME_CHANGE", "%ticks%", LANG_CONFIG.getString("NIGHT", new String[0])));
            player.closeInventory();
        }));
        ((GInventory)this.inventory).setItem(guiConfig.getInt("pTime.resetSlot"), new GItem(ItemBuilder.builder(Material.BUCKET).setName(LANG_CONFIG.getString("PTIME_ITEM_RESET_NAME", new String[0])).setLore(LANG_CONFIG.getStringList("PTIME_ITEM_RESET_LORE", new String[0])).build(), (player, clickType, i) -> {
            player.chat("/ptime reset");
            player.closeInventory();
        }));
        INVENTORY_API.backButton((GInventory)this.inventory, (player1, clickType, i) -> player1.chat("/menu"), guiConfig.getInt("pTime.backSlot"));
    }
}
