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
import ru.bestrax.essentials.api.VaultAPI;
import ru.bestrax.essentials.api.config.Config;
import ru.bestrax.essentials.api.inventory.GItem;
import ru.bestrax.essentials.api.inventory.GuiManager;
import ru.bestrax.essentials.api.inventory.type.GInventory;
import ru.bestrax.essentials.api.manager.WarpManager;
import ru.bestrax.essentials.base.inventory.impl.types.AbstractGui;
import ru.bestrax.essentials.gui.MyWarpGui;
import ru.bestrax.essentials.gui.PlayersWarpGui;
import ru.bestrax.essentials.gui.SystemWarpGui;
import ru.bestrax.essentials.utils.BukkitUtils;
import ru.bestrax.essentials.utils.ItemBuilder;

public class WarpGui
extends AbstractGui<GInventory> {
    private static final GuiManager<AbstractGui<?>> GUI_MANAGER = EssentialsApi.getGuiManager();
    private static final WarpManager WARP_MANAGER = EssentialsApi.getWarpManager();
    private static final int SYSTEM_WARP_SIZE = EssentialsApi.getWarpManager().getSystemWarps().size();

    public WarpGui(Player player) {
        super(player);
        this.createInventory();
    }

    @Override
    protected void createInventory() {
        Config LANG_CONFIG = EssentialsApi.getInstance().getLang();
        this.inventory = INVENTORY_API.createInventory(this.player, LANG_CONFIG.getString("GUI_NAME", new String[0]) + " \u25b8 " + LANG_CONFIG.getString("WARP_GUI_NAME", new String[0]), 6);
    }

    @Override
    public void setStaticItems() {
        FileConfiguration config = EssentialsApi.getInstance().getGuiConfiguration();
        INVENTORY_API.items((GInventory)this.inventory, (List<String>)config.getStringList("warp.items"));
        Config LANG_CONFIG = EssentialsApi.getInstance().getLang();
        ((GInventory)this.inventory).setItem(config.getInt("warp.systemWarpSlot"), new GItem(ItemBuilder.builder(BukkitUtils.getMaterial("KNOWLEDGE_BOOK")).setName(LANG_CONFIG.getString("SYSTEM_WARP_GUI_ITEM_NAME", new String[0])).setLore(LANG_CONFIG.getStringList("SYSTEM_WARP_GUI_ITEM_LORE", "%size%", String.valueOf(SYSTEM_WARP_SIZE))).build(), (player1, clickType, i) -> {
            this.player.playSound(this.player.getLocation(), BukkitUtils.getBlockBellSound(), 1.0f, 1.0f);
            GUI_MANAGER.getGui(SystemWarpGui.class, this.player).open();
        }));
        ((GInventory)this.inventory).setItem(config.getInt("warp.myWarpSlot"), new GItem(ItemBuilder.builder(BukkitUtils.getMaterial("SIGN")).setName(LANG_CONFIG.getString("MYWARP_GUI_ITEM_NAME", new String[0])).setLore(LANG_CONFIG.getStringList("MYWARP_GUI_ITEM_LORE", "%size%", String.valueOf(WARP_MANAGER.getWarps(this.player.getName()).size()), "%max%", String.valueOf(EssentialsApi.getWarpManager().getWarpLimit(VaultAPI.getPrimaryGroup(this.player))))).build(), (player, clickType, i) -> {
            player.playSound(player.getLocation(), BukkitUtils.getBlockBellSound(), 1.0f, 1.0f);
            GUI_MANAGER.getGui(MyWarpGui.class, player).open();
        }));
        ((GInventory)this.inventory).setItem(config.getInt("warp.playersWarpSlot"), new GItem(ItemBuilder.builder(Material.BOOK).setName(LANG_CONFIG.getString("PLAYERS_WARP_GUI_ITEM_NAME", new String[0])).setLore(LANG_CONFIG.getStringList("PLAYERS_WARP_GUI_ITEM_LORE", "%size%", String.valueOf(WARP_MANAGER.getPlayersWarps().size()))).build(), (player1, clickType, i) -> {
            this.player.playSound(this.player.getLocation(), BukkitUtils.getBlockBellSound(), 1.0f, 1.0f);
            GUI_MANAGER.getGui(PlayersWarpGui.class, this.player).open();
        }));
        INVENTORY_API.backButton((GInventory)this.inventory, (player1, clickType, i) -> player1.chat("/menu"), config.getInt("warp.backSlot"));
    }
}
