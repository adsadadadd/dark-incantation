/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.Material
 *  org.bukkit.configuration.file.FileConfiguration
 *  org.bukkit.entity.Player
 */
package ru.bestrax.essentials.gui;

import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import ru.bestrax.essentials.api.EssentialsApi;
import ru.bestrax.essentials.api.config.Config;
import ru.bestrax.essentials.api.cooldown.Cooldowns;
import ru.bestrax.essentials.api.inventory.GItem;
import ru.bestrax.essentials.api.inventory.type.GInventory;
import ru.bestrax.essentials.base.inventory.impl.types.UpdatableGui;
import ru.bestrax.essentials.utils.BukkitUtils;
import ru.bestrax.essentials.utils.ItemBuilder;
import ru.bestrax.essentials.utils.TeleportingUtils;
import ru.bestrax.essentials.utils.TimeUtils;

public class RtpGui
extends UpdatableGui<GInventory> {
    public RtpGui(Player player) {
        super(player);
        this.createInventory();
        this.setActions();
    }

    @Override
    protected void createInventory() {
        Config lang = EssentialsApi.getInstance().getLang();
        this.inventory = INVENTORY_API.createInventory(lang.getString("RTP_GUI_NAME", new String[0]), 6);
    }

    @Override
    protected void setDynamicItems() {
        List<String> regionLore;
        Config lang = EssentialsApi.getInstance().getLang();
        Config settings = EssentialsApi.getInstance().getSettings();
        FileConfiguration config = EssentialsApi.getInstance().getGuiConfiguration();
        boolean rtpRegionEnabled = EssentialsApi.getRegionManager() != null && settings.getInt("rtpRegionCooldown") >= 0;
        rtpRegionEnabled = rtpRegionEnabled && settings.getBoolean("rtpRegion");
        boolean rtpPvpEnabled = settings.getBoolean("rtpPvp");
        boolean rtpBigEnabled = settings.getBoolean("rtpBig");
        boolean rtpEnabled = settings.getBoolean("rtpDefault");
        int rtpCooldown = Cooldowns.get(this.player.getName(), "RTP");
        int rtpBigCooldown = Cooldowns.get(this.player.getName(), "RTP_BIG");
        int rtpPvpCooldown = Cooldowns.get(this.player.getName(), "RTP_PVP");
        int rtpRegionCooldown = Cooldowns.get(this.player.getName(), "RTP_REGION");
        if (rtpEnabled) {
            ((GInventory)this.inventory).setItem(config.getInt("rtp.rtpSlot"), new GItem(ItemBuilder.builder("head:eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODc5ZTU0Y2JlODc4NjdkMTRiMmZiZGYzZjE4NzA4OTQzNTIwNDhkZmVjZDk2Mjg0NmRlYTg5M2IyMTU0Yzg1In19fQ==").setName(lang.getString("RTP_ITEM_NAME", new String[0])).setLore(rtpCooldown <= 0 ? lang.getStringList("RTP_ITEM_LORE", new String[0]) : lang.getStringList("RTP_COOLDOWN_ITEM_LORE", "%time%", TimeUtils.getTimeInSeconds(rtpCooldown / 1000))).build(), (player1, clickType, i) -> {
                if (rtpCooldown > 0) {
                    player1.sendMessage(lang.getString("RTP_COOLDOWN", "%time%", TimeUtils.getTimeInSeconds(rtpCooldown / 1000)));
                    return;
                }
                player1.closeInventory();
                TeleportingUtils.teleportRandom(player1, Bukkit.getWorld((String)settings.getString("rtpWorld", new String[0])), settings.getInt("rtpSize"), false);
                if (!settings.getBoolean("rtpCooldownBypass") || !player1.hasPermission("trustessentials.rtp.cooldown.bypass")) {
                    Cooldowns.set(player1.getName(), "RTP", settings.getInt("rtpCooldown"));
                }
            }));
        }
        if (rtpPvpEnabled) {
            List<String> pvpLore = !this.player.hasPermission("trustessentials.rtp.near") ? lang.getStringList("RTP_PVP_ITEM_DENY_LORE", new String[0]) : (rtpPvpCooldown <= 0 ? lang.getStringList("RTP_PVP_ITEM_LORE", new String[0]) : lang.getStringList("RTP_PVP_COOLDOWN_ITEM_LORE", "%time%", TimeUtils.getTimeInSeconds(rtpPvpCooldown / 1000)));
            ((GInventory)this.inventory).setItem(config.getInt("rtp.rtpPlayerSlot"), new GItem(ItemBuilder.builder(Material.DIAMOND_SWORD).setName(lang.getString("RTP_PVP_ITEM_NAME", new String[0])).setLore(pvpLore).build(), (player1, clickType, i) -> {
                if (!player1.hasPermission("trustessentials.rtp.near")) {
                    player1.sendMessage(lang.getString("NO_PERMISSION", "%permission%", "trustessentials.rtp.near"));
                    return;
                }
                if (rtpPvpCooldown > 0) {
                    player1.sendMessage(lang.getString("RTP_COOLDOWN", "%time%", TimeUtils.getTimeInSeconds(rtpPvpCooldown / 1000)));
                    return;
                }
                player1.closeInventory();
                if (!TeleportingUtils.teleportPvp(player1, Bukkit.getWorld((String)settings.getString("rtpWorld", new String[0])), settings.getInt("rtpNearSize"), settings.getInt("rtpTargetMinimalTime") * 20 * 60)) {
                    player1.sendMessage(lang.getString("RTP_PVP_TELEPORT_ERROR", new String[0]));
                    return;
                }
                if (!settings.getBoolean("rtpCooldownBypass") || !player1.hasPermission("trustessentials.rtp.cooldown.bypass")) {
                    Cooldowns.set(player1.getName(), "RTP_PVP", settings.getInt("rtpNearCooldown"));
                }
            }));
        }
        if (rtpBigEnabled) {
            List<String> bigLore = !this.player.hasPermission("trustessentials.rtp.big") ? lang.getStringList("RTP_BIG_ITEM_DENY_LORE", new String[0]) : (rtpBigCooldown <= 0 ? lang.getStringList("RTP_BIG_ITEM_LORE", new String[0]) : lang.getStringList("RTP_BIG_COOLDOWN_ITEM_LORE", "%time%", TimeUtils.getTimeInSeconds(rtpBigCooldown / 1000)));
            ((GInventory)this.inventory).setItem(config.getInt("rtp.rtpBigSlot"), new GItem(ItemBuilder.builder("HEAD:eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZmZkMjFmMTYzNDdhNDAxY2E1ZTk3ZjQ3NzgyZjQ3MzliZjU0ODBhMWMyOWUxOTc0Mzk0NWI0ZGM1MjlmNTg1OSJ9fX0=").setName(lang.getString("RTP_BIG_ITEM_NAME", new String[0])).setLore(bigLore).glowing(true).build(), (player1, clickType, i) -> {
                if (!player1.hasPermission("trustessentials.rtp.big")) {
                    player1.sendMessage(lang.getString("NO_PERMISSION", "%permission%", "trustessentials.rtp.big"));
                    return;
                }
                if (rtpBigCooldown > 0) {
                    player1.sendMessage(lang.getString("RTP_COOLDOWN", "%time%", TimeUtils.getTimeInSeconds(rtpBigCooldown / 1000)));
                    return;
                }
                player1.closeInventory();
                TeleportingUtils.teleportRandom(player1, Bukkit.getWorld((String)settings.getString("rtpWorld", new String[0])), settings.getInt("rtpBigSize"), false);
                if (!settings.getBoolean("rtpCooldownBypass") || !player1.hasPermission("trustessentials.rtp.cooldown.bypass")) {
                    Cooldowns.set(player1.getName(), "RTP_BIG", settings.getInt("rtpBigCooldown"));
                }
            }));
        }
        List<String> list = !this.player.hasPermission("trustessentials.rtp.region") ? lang.getStringList("RTP_REGION_ITEM_DENY_LORE", new String[0]) : (regionLore = rtpBigCooldown <= 0 ? lang.getStringList("RTP_REGION_ITEM_LORE", new String[0]) : lang.getStringList("RTP_REGION_COOLDOWN_ITEM_LORE", "%time%", TimeUtils.getTimeInSeconds(rtpRegionCooldown / 1000)));
        if (rtpRegionEnabled) {
            ((GInventory)this.inventory).setItem(config.getInt("rtp.rtpRegionSlot"), new GItem(ItemBuilder.builder(BukkitUtils.getMaterial("ENDER_PORTAL_FRAME")).setName(lang.getString("RTP_REGION_ITEM_NAME", new String[0])).setLore(regionLore).build(), (player1, clickType, i) -> {
                if (!player1.hasPermission("trustessentials.rtp.region")) {
                    player1.sendMessage(lang.getString("NO_PERMISSION", "%permission%", "trustessentials.rtp.region"));
                    return;
                }
                if (rtpRegionCooldown > 0) {
                    player1.sendMessage(lang.getString("RTP_COOLDOWN", "%time%", TimeUtils.getTimeInSeconds(rtpRegionCooldown / 1000)));
                    return;
                }
                player1.closeInventory();
                if (!EssentialsApi.getRegionManager().teleportRandomRegion(player1, Bukkit.getWorld((String)settings.getString("rtpWorld", new String[0])))) {
                    player1.sendMessage(lang.getString("RTP_REGION_TELEPORT_ERROR", new String[0]));
                    return;
                }
                if (!settings.getBoolean("rtpCooldownBypass") || !player1.hasPermission("trustessentials.rtp.cooldown.bypass")) {
                    Cooldowns.set(player1.getName(), "RTP_REGION", settings.getInt("rtpRegionCooldown"));
                }
            }));
        }
        INVENTORY_API.backButton((GInventory)this.inventory, (player1, clickType, i) -> player1.chat("/menu"), config.getInt("rtp.backSlot"));
    }

    @Override
    protected void setStaticItems() {
        FileConfiguration guiConfig = EssentialsApi.getInstance().getGuiConfiguration();
        INVENTORY_API.items((GInventory)this.inventory, (List<String>)guiConfig.getStringList("rtp.items"));
    }
}
