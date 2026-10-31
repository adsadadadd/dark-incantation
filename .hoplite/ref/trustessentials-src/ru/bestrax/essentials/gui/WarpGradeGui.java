/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.Location
 *  org.bukkit.configuration.file.FileConfiguration
 *  org.bukkit.entity.Player
 */
package ru.bestrax.essentials.gui;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import ru.bestrax.essentials.api.EssentialsApi;
import ru.bestrax.essentials.api.Warp;
import ru.bestrax.essentials.api.config.Config;
import ru.bestrax.essentials.api.cooldown.Cooldowns;
import ru.bestrax.essentials.api.inventory.GItem;
import ru.bestrax.essentials.api.inventory.type.GInventory;
import ru.bestrax.essentials.base.inventory.impl.types.AbstractGui;
import ru.bestrax.essentials.gui.PlayersWarpGui;
import ru.bestrax.essentials.utils.ItemBuilder;
import ru.bestrax.essentials.utils.TimeUtils;

public class WarpGradeGui
extends AbstractGui<GInventory> {
    private static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("dd-MM-yyyy");
    private Warp warp;
    private int page;

    public WarpGradeGui(Player player, int page, Warp warp) {
        super(player);
        this.page = page;
        this.warp = warp;
        this.createInventory();
    }

    @Override
    protected void createInventory() {
        Config LANG_CONFIG = EssentialsApi.getInstance().getLang();
        this.inventory = INVENTORY_API.createInventory(this.player, LANG_CONFIG.getString("GUI_NAME", new String[0]) + " \u25b8 " + LANG_CONFIG.getString("WARP_GUI_NAME", new String[0]) + " \u25b8 " + LANG_CONFIG.getString("PLAYER_WARP_GUI_NAME", new String[0]) + " | " + (this.page + 1) + " \u25b8 " + LANG_CONFIG.getString("WARP_GRADE_GUI_NAME", "%warp%", this.warp.getName()), 6);
    }

    @Override
    protected void setStaticItems() {
        FileConfiguration guiConfig = EssentialsApi.getInstance().getGuiConfiguration();
        INVENTORY_API.items((GInventory)this.inventory, (List<String>)guiConfig.getStringList("gradeWarp.items"));
        Config LANG_CONFIG = EssentialsApi.getInstance().getLang();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(this.warp.getDate().getTime());
        String date = SIMPLE_DATE_FORMAT.format(calendar.getTime());
        String name = this.warp.getName();
        Location location = this.warp.getLocation();
        int players = this.warp.getNearbyPlayers(900).size();
        ((GInventory)this.inventory).setItem(guiConfig.getInt("gradeWarp.warpSlot"), new GItem(ItemBuilder.builder(this.warp.getIcon()).setName("\u00a7a" + name).setLore(LANG_CONFIG.getStringList("GRADE_WARP_ITEM_LORE", "%grade%", String.valueOf(this.warp.getGrade() > 0 ? Integer.valueOf(this.warp.getGrade()) : LANG_CONFIG.getString("NO_GRADE", new String[0])), "%owner%", this.warp.getOwnerName(), "%create%", date, "%world%", location.getWorld().getName(), "%x%", String.valueOf((int)location.getX()), "%y%", String.valueOf((int)location.getY()), "%z%", String.valueOf((int)location.getZ()), "%players%", String.valueOf(players))).build()));
        for (int i2 = 0; i2 < Grade.values().length; ++i2) {
            Grade grade = Grade.values()[i2];
            int gradeInt = i2 + 1;
            ((GInventory)this.getInventory()).setItem(guiConfig.getInt("gradeWarp." + grade.name().toLowerCase() + "GradeSlot"), new GItem(ItemBuilder.builder(guiConfig.getString("gradeWarp." + grade.name().toLowerCase() + "GradeItem")).setName(LANG_CONFIG.getString("WARP_GRADE_ITEM_NAME", "%grade%", String.valueOf(gradeInt))).setLore(LANG_CONFIG.getStringList("WARP_GRADE_ITEM_LORE", "%grade%", String.valueOf(gradeInt))).build(), (clicker, clickType, slot) -> {
                if (Cooldowns.has(this.player.getName(), "grade")) {
                    this.player.sendMessage(LANG_CONFIG.getString("WARP_GRADE_COOLDOWN", "%time%", TimeUtils.getTimeInSeconds(Cooldowns.get(this.player.getName(), "grade") / 1000)));
                } else {
                    this.player.closeInventory();
                    if (this.warp.getOwnerName().equalsIgnoreCase(this.player.getName())) {
                        this.player.sendMessage(LANG_CONFIG.getString("WARP_GRADE_YOU", new String[0]));
                        return;
                    }
                    for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                        onlinePlayer.sendMessage(LANG_CONFIG.getString("WARP_GRADE", "%warp%", this.warp.getName(), "%player%", this.player.getName(), "%grade%", String.valueOf(gradeInt)));
                    }
                    this.warp.grade(this.player, gradeInt);
                    Cooldowns.set(this.player.getName(), "grade", EssentialsApi.getInstance().getSettings().getInt("gradeCooldown"));
                }
            }));
        }
        INVENTORY_API.backButton((GInventory)this.inventory, (player1, clickType, i) -> {
            PlayersWarpGui playersWarpGui = new PlayersWarpGui(player1);
            playersWarpGui.open(this.page);
        }, guiConfig.getInt("gradeWarp.backSlot"));
    }

    static enum Grade {
        ONE,
        TWO,
        THREE,
        FOUR,
        FIVE;

    }
}
