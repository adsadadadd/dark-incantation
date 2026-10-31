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
import ru.bestrax.essentials.api.Home;
import ru.bestrax.essentials.api.config.Config;
import ru.bestrax.essentials.api.inventory.GItem;
import ru.bestrax.essentials.api.inventory.type.MultiInventory;
import ru.bestrax.essentials.base.inventory.impl.types.AbstractGui;
import ru.bestrax.essentials.base.objects.TrustUser;
import ru.bestrax.essentials.utils.BukkitUtils;
import ru.bestrax.essentials.utils.ItemBuilder;

public class HomeGui
extends AbstractGui<MultiInventory> {
    public HomeGui(Player player) {
        super(player);
        this.createInventory();
    }

    @Override
    protected void createInventory() {
        Config LANG_CONFIG = EssentialsApi.getInstance().getLang();
        this.inventory = INVENTORY_API.createMultiInventory(this.player, LANG_CONFIG.getString("GUI_NAME", new String[0]) + " \u25b8 " + LANG_CONFIG.getString("HOME_GUI_NAME", new String[0]), 6);
    }

    @Override
    public void setStaticItems() {
        Config LANG_CONFIG = EssentialsApi.getInstance().getLang();
        TrustUser trustUser = (TrustUser)EssentialsApi.getUserManager().getUser(this.player);
        FileConfiguration guiConfig = EssentialsApi.getInstance().getGuiConfiguration();
        if (trustUser != null) {
            int size = trustUser.getHomes().size();
            if (size == 0) {
                ((MultiInventory)this.inventory).setItem(0, 22, new GItem(ItemBuilder.builder(Material.GLASS_BOTTLE).setName(LANG_CONFIG.getString("HOME_ITEM_EMPTY_NAME", new String[0])).setLore(LANG_CONFIG.getStringList("HOME_ITEM_EMPTY_LORE", new String[0])).build()));
                INVENTORY_API.items(((MultiInventory)this.inventory).getInventories().get(0), (List<String>)guiConfig.getStringList("home.items"));
            } else {
                int startSlot = guiConfig.getInt("home.startSlot");
                int pageNum = 0;
                int slot = startSlot;
                int count = 0;
                INVENTORY_API.items((MultiInventory)this.inventory, pageNum, guiConfig.getStringList("home.items"));
                for (String home : trustUser.getHomes().keySet()) {
                    Home h;
                    if (((MultiInventory)this.inventory).getInventories().size() == 0) continue;
                    if (++count == 15) {
                        count = 0;
                    }
                    if ((h = trustUser.getHomes().get(home)) == null) continue;
                    Location loc = h.getLocation();
                    ((MultiInventory)this.inventory).setItem(pageNum, slot, new GItem(ItemBuilder.builder(BukkitUtils.getMaterial("BANNER")).setDurability((short)count).setName("\u00a7a" + home).setLore(LANG_CONFIG.getStringList("HOME_LORE", "%world%", loc.getWorld().getName(), "%x%", String.valueOf((int)loc.getX()), "%y%", String.valueOf((int)loc.getY()), "%z%", String.valueOf((int)loc.getZ()))).build(), (player, clickType, i) -> {
                        player.chat("/homes " + home);
                        player.closeInventory();
                    }));
                    boolean nextRow = false;
                    for (String row : guiConfig.getStringList("home.rows")) {
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
                            INVENTORY_API.items((MultiInventory)this.inventory, ++pageNum, guiConfig.getStringList("home.items"));
                        } else {
                            slot = newSlot;
                        }
                        nextRow = true;
                    }
                    if (nextRow) continue;
                    ++slot;
                }
                INVENTORY_API.backButton((MultiInventory)this.inventory, (player1, clickType, i) -> player1.chat("/menu"), guiConfig.getInt("home.backSlot"));
            }
        }
        INVENTORY_API.pageButton((MultiInventory)this.getInventory(), guiConfig.getInt("home.downSlot"), guiConfig.getInt("home.upSlot"));
    }
}
