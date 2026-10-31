/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Material
 *  org.bukkit.WeatherType
 *  org.bukkit.configuration.file.FileConfiguration
 *  org.bukkit.entity.Player
 */
package ru.bestrax.essentials.gui;

import java.util.List;
import org.bukkit.Material;
import org.bukkit.WeatherType;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import ru.bestrax.essentials.api.EssentialsApi;
import ru.bestrax.essentials.api.config.Config;
import ru.bestrax.essentials.api.inventory.GItem;
import ru.bestrax.essentials.api.inventory.type.GInventory;
import ru.bestrax.essentials.base.inventory.impl.types.AbstractGui;
import ru.bestrax.essentials.utils.BukkitUtils;
import ru.bestrax.essentials.utils.ItemBuilder;

public class PWeatherGui
extends AbstractGui<GInventory> {
    public PWeatherGui(Player player) {
        super(player);
        this.createInventory();
    }

    @Override
    protected void createInventory() {
        Config LANG_CONFIG = EssentialsApi.getInstance().getLang();
        this.inventory = INVENTORY_API.createInventory(this.player, LANG_CONFIG.getString("GUI_NAME", new String[0]) + " \u25b8 " + LANG_CONFIG.getString("PWEATHER_GUI_NAME", new String[0]), 6);
    }

    @Override
    public void setStaticItems() {
        FileConfiguration guiConfig = EssentialsApi.getInstance().getGuiConfiguration();
        INVENTORY_API.items((GInventory)this.inventory, (List<String>)guiConfig.getStringList("pWeather.items"));
        Config LANG_CONFIG = EssentialsApi.getInstance().getLang();
        ((GInventory)this.inventory).setItem(guiConfig.getInt("pWeather.sunnySlot"), new GItem(ItemBuilder.builder(BukkitUtils.getMaterial("DOUBLE_PLANT")).setName(LANG_CONFIG.getString("PWEATHER_ITEM_NAME", new String[0])).setLore(LANG_CONFIG.getStringList("PWEATHER_ITEM_LORE", "%weather%", LANG_CONFIG.getString("SUNNY", new String[0]))).build(), (player, clickType, i) -> {
            player.setPlayerWeather(WeatherType.CLEAR);
            player.sendMessage(LANG_CONFIG.getString("PWEATHER_CHANGE", "%weather%", LANG_CONFIG.getString("SUNNY", new String[0])));
            player.closeInventory();
        }));
        ((GInventory)this.inventory).setItem(guiConfig.getInt("pWeather.rainySlot"), new GItem(ItemBuilder.builder(Material.WATER_BUCKET).setName(LANG_CONFIG.getString("PWEATHER_ITEM_NAME", new String[0])).setLore(LANG_CONFIG.getStringList("PWEATHER_ITEM_LORE", "%weather%", LANG_CONFIG.getString("RAINY", new String[0]))).build(), (player, clickType, i) -> {
            player.setPlayerWeather(WeatherType.DOWNFALL);
            player.sendMessage(LANG_CONFIG.getString("PWEATHER_CHANGE", "%weather%", LANG_CONFIG.getString("RAINY", new String[0])));
            player.closeInventory();
        }));
        ((GInventory)this.inventory).setItem(guiConfig.getInt("pWeather.resetSlot"), new GItem(ItemBuilder.builder(Material.BUCKET).setName(LANG_CONFIG.getString("PWEATHER_ITEM_RESET_NAME", new String[0])).setLore(LANG_CONFIG.getStringList("PWEATHER_ITEM_RESET_LORE", new String[0])).build(), (player, clickType, i) -> {
            player.chat("/pweather reset");
            player.closeInventory();
        }));
        INVENTORY_API.backButton((GInventory)this.inventory, (player1, clickType, i) -> player1.chat("/menu"), guiConfig.getInt("pWeather.backSlot"));
    }
}
