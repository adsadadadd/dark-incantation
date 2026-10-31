/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Location
 *  org.bukkit.Material
 *  org.bukkit.configuration.file.FileConfiguration
 *  org.bukkit.entity.Player
 */
package ru.bestrax.essentials.gui;

import java.util.List;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import ru.bestrax.essentials.api.EssentialsApi;
import ru.bestrax.essentials.api.Warp;
import ru.bestrax.essentials.api.config.Config;
import ru.bestrax.essentials.api.inventory.GItem;
import ru.bestrax.essentials.api.inventory.type.MultiInventory;
import ru.bestrax.essentials.api.manager.WarpManager;
import ru.bestrax.essentials.base.inventory.impl.types.AbstractGui;
import ru.bestrax.essentials.base.objects.TrustWarp;
import ru.bestrax.essentials.utils.BukkitUtils;
import ru.bestrax.essentials.utils.ItemBuilder;

public class SystemWarpGui
extends AbstractGui<MultiInventory> {
    private static final WarpManager WARP_MANAGER = EssentialsApi.getWarpManager();

    public SystemWarpGui(Player player) {
        super(player);
        this.createInventory();
    }

    @Override
    protected void createInventory() {
        Config LANG_CONFIG = EssentialsApi.getInstance().getLang();
        this.inventory = INVENTORY_API.createMultiInventory(LANG_CONFIG.getString("GUI_NAME", new String[0]) + " \u25b8 " + LANG_CONFIG.getString("WARP_GUI_NAME", new String[0]) + " \u25b8 " + LANG_CONFIG.getString("SYSTEM_WARP_GUI_NAME", new String[0]), 6);
    }

    @Override
    public void setStaticItems() {
        Config LANG_CONFIG = EssentialsApi.getInstance().getLang();
        List<Warp> warps = WARP_MANAGER.getSystemWarps();
        FileConfiguration config = EssentialsApi.getInstance().getGuiConfiguration();
        int size = warps.size();
        if (size == 0) {
            ((MultiInventory)this.inventory).setItem(0, 22, new GItem(ItemBuilder.builder(Material.GLASS_BOTTLE).setName(LANG_CONFIG.getString("WARP_ITEM_EMPTY_NAME", new String[0])).setLore(LANG_CONFIG.getStringList("WARP_ITEM_EMPTY_LORE", new String[0])).build(), (player, clickType, slot1) -> player.playSound(player.getLocation(), BukkitUtils.getBlockBellSound(), 1.0f, 1.0f)));
            INVENTORY_API.items(((MultiInventory)this.inventory).getInventories().get(0), (List<String>)config.getStringList("systemWarp.items"));
            INVENTORY_API.backButton((MultiInventory)this.inventory, (player1, clickType, i) -> player1.chat("/warp"), 49);
        } else {
            int pageNum = 0;
            INVENTORY_API.items((MultiInventory)this.inventory, pageNum, config.getStringList("systemWarp.items"));
            for (Warp warp : warps) {
                int slot = ((TrustWarp)warp).getGuiSlot();
                if (slot >= config.getInt("systemWarp.endSlot")) {
                    slot -= config.getInt("systemWarp.endSlot");
                    INVENTORY_API.items((MultiInventory)this.inventory, ++pageNum, config.getStringList("systemWarp.items"));
                }
                String name = warp.getName();
                Location location = warp.getLocation();
                int players = warp.getNearbyPlayers(30).size();
                ((MultiInventory)this.inventory).setItem(pageNum, slot, new GItem(ItemBuilder.builder(warp.getIcon()).setName("\u00a7a" + warp.getLocalizedName()).setLore(LANG_CONFIG.getStringList("SYSTEM_WARP_ITEM_LORE", "%world%", location.getWorld().getName(), "%x%", String.valueOf((int)location.getX()), "%y%", String.valueOf((int)location.getY()), "%z%", String.valueOf((int)location.getZ()), "%players%", String.valueOf(players))).build(), (player, clickType, i) -> {
                    player.chat("/warp " + name);
                    player.playSound(player.getLocation(), BukkitUtils.getBlockBellSound(), 1.0f, 1.0f);
                    player.closeInventory();
                }));
            }
            INVENTORY_API.backButton((MultiInventory)this.inventory, (player1, clickType, i) -> player1.chat("/warp"), config.getInt("systemWarp.backSlot"));
        }
        INVENTORY_API.pageButton((MultiInventory)this.getInventory(), 47, 51);
    }
}
