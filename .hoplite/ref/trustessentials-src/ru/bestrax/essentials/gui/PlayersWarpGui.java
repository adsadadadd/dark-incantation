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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import ru.bestrax.essentials.api.EssentialsApi;
import ru.bestrax.essentials.api.Warp;
import ru.bestrax.essentials.api.config.Config;
import ru.bestrax.essentials.api.inventory.GItem;
import ru.bestrax.essentials.api.inventory.type.GInventory;
import ru.bestrax.essentials.api.inventory.type.MultiInventory;
import ru.bestrax.essentials.api.inventory.type.ScrollInventory;
import ru.bestrax.essentials.api.manager.WarpManager;
import ru.bestrax.essentials.base.inventory.impl.types.UpdatableGui;
import ru.bestrax.essentials.gui.WarpGradeGui;
import ru.bestrax.essentials.utils.BukkitUtils;
import ru.bestrax.essentials.utils.ItemBuilder;

public class PlayersWarpGui
extends UpdatableGui<MultiInventory> {
    private static final WarpManager WARP_MANAGER = EssentialsApi.getWarpManager();
    private static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("dd-MM-yyyy");

    public PlayersWarpGui(Player player) {
        super(player);
        this.createInventory();
        this.setActions();
    }

    public void open(int page) {
        if (this.recreateOnOpen) {
            this.createInventory();
        } else if (this.inventory instanceof MultiInventory) {
            ((MultiInventory)this.inventory).clearInventories();
        } else if (this.inventory instanceof ScrollInventory) {
            ((ScrollInventory)this.inventory).clearInventory();
        } else if (this.inventory instanceof GInventory) {
            ((GInventory)this.inventory).clearInventory();
        }
        this.setStaticItems();
        this.setDynamicItems();
        ((MultiInventory)this.inventory).openInventory(this.player, page);
        EssentialsApi.getGuiManager().putPlayerGui(this, this.player);
    }

    @Override
    protected void createInventory() {
        Config LANG_CONFIG = EssentialsApi.getInstance().getLang();
        this.inventory = INVENTORY_API.createMultiInventory(LANG_CONFIG.getString("GUI_NAME", new String[0]) + " \u25b8 " + LANG_CONFIG.getString("WARP_GUI_NAME", new String[0]) + " \u25b8 " + LANG_CONFIG.getString("PLAYER_WARP_GUI_NAME", new String[0]), 6);
    }

    @Override
    protected void setStaticItems() {
    }

    @Override
    public void setDynamicItems() {
        Config LANG_CONFIG = EssentialsApi.getInstance().getLang();
        Map<String, Warp> warps = WARP_MANAGER.getPlayersWarps();
        int size = 0;
        for (Warp warp : warps.values()) {
            ++size;
        }
        FileConfiguration guiConfig = EssentialsApi.getInstance().getGuiConfiguration();
        if (size == 0) {
            ((MultiInventory)this.inventory).setItem(0, 22, new GItem(ItemBuilder.builder(Material.GLASS_BOTTLE).setName(LANG_CONFIG.getString("WARP_ITEM_EMPTY_NAME", new String[0])).setLore(LANG_CONFIG.getStringList("WARP_ITEM_EMPTY_LORE", new String[0])).build(), (player, clickType, slot1) -> player.playSound(player.getLocation(), BukkitUtils.getBlockBellSound(), 1.0f, 1.0f)));
            INVENTORY_API.items(((MultiInventory)this.inventory).getInventories().get(0), (List<String>)guiConfig.getStringList("playersWarp.items"));
            INVENTORY_API.backButton((MultiInventory)this.inventory, (player1, clickType, i) -> player1.chat("/warp"), 49);
        } else {
            int startSlot = guiConfig.getInt("playersWarp.startSlot");
            int pageNum = 0;
            int slot = startSlot;
            INVENTORY_API.items((MultiInventory)this.inventory, pageNum, guiConfig.getStringList("playersWarp.items"));
            for (Warp warp : warps.values().stream().sorted(Comparator.comparingLong(value -> value.getDate().getTime())).collect(Collectors.toList())) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(warp.getDate().getTime());
                String date = SIMPLE_DATE_FORMAT.format(calendar.getTime());
                String name = warp.getName();
                Location location = warp.getLocation();
                int players = warp.getNearbyPlayers(900).size();
                int finalPageNum = pageNum;
                ((MultiInventory)this.inventory).setItem(pageNum, slot, new GItem(ItemBuilder.builder(warp.getIcon()).setName("\u00a7a" + name).setLore(LANG_CONFIG.getStringList("WARP_ITEM_LORE", "%grade%", String.valueOf(warp.getGrade() > 0 ? Integer.valueOf(warp.getGrade()) : LANG_CONFIG.getString("NO_GRADE", new String[0])), "%owner%", warp.getOwnerName(), "%create%", date, "%world%", location.getWorld().getName(), "%x%", String.valueOf((int)location.getX()), "%y%", String.valueOf((int)location.getY()), "%z%", String.valueOf((int)location.getZ()), "%players%", String.valueOf(players))).build(), (player, clickType, i) -> {
                    if (clickType.isRightClick()) {
                        new WarpGradeGui(player, finalPageNum, warp).open();
                    } else {
                        player.chat("/warp " + name);
                        player.playSound(player.getLocation(), BukkitUtils.getBlockBellSound(), 1.0f, 1.0f);
                        player.closeInventory();
                    }
                }));
                boolean nextRow = false;
                for (String row : guiConfig.getStringList("playersWarp.rows")) {
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
                        INVENTORY_API.items((MultiInventory)this.inventory, ++pageNum, guiConfig.getStringList("playersWarp.items"));
                    } else {
                        slot = newSlot;
                    }
                    nextRow = true;
                }
                if (nextRow) continue;
                ++slot;
            }
            INVENTORY_API.backButton((MultiInventory)this.inventory, (player1, clickType, i) -> player1.chat("/warp"), guiConfig.getInt("playersWarp.backSlot"));
        }
        INVENTORY_API.pageButton((MultiInventory)this.getInventory(), guiConfig.getInt("playersWarp.downSlot"), guiConfig.getInt("playersWarp.upSlot"));
    }
}
