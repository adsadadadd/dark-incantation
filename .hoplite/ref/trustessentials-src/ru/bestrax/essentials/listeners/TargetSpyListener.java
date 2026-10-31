/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.ChatColor
 *  org.bukkit.entity.Player
 *  org.bukkit.event.EventHandler
 *  org.bukkit.event.Listener
 *  org.bukkit.event.player.PlayerCommandPreprocessEvent
 *  org.bukkit.event.player.PlayerQuitEvent
 */
package ru.bestrax.essentials.listeners;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import ru.bestrax.essentials.api.EssentialsApi;
import ru.bestrax.essentials.api.User;
import ru.bestrax.essentials.api.VaultAPI;
import ru.bestrax.essentials.api.config.Config;

public class TargetSpyListener
implements Listener {
    @EventHandler
    public void onChat(PlayerCommandPreprocessEvent e) {
        Config settingsConfig = EssentialsApi.getInstance().getSettings();
        Player player = e.getPlayer();
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            User user = EssentialsApi.getUserManager().getUser(onlinePlayer);
            if (user.getTargetSpy() == null || !user.getTargetSpy().equalsIgnoreCase(player.getName())) continue;
            String message = e.getMessage();
            String format = settingsConfig.getString("targetSpyFormat", new String[0]);
            format = settingsConfig.replace(format, "%prefixSender%", VaultAPI.getPrefix(player), "%sender%", ChatColor.translateAlternateColorCodes((char)'&', (String)player.getDisplayName()), "%command%", message);
            onlinePlayer.sendMessage(format);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        Config langConfig = EssentialsApi.getInstance().getLang();
        Player player = e.getPlayer();
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            User user = EssentialsApi.getUserManager().getUser(onlinePlayer);
            if (user.getTargetSpy() == null || !user.getTargetSpy().equalsIgnoreCase(player.getName())) continue;
            onlinePlayer.sendMessage(langConfig.getString("TARGETSPY_OFF", "%name%", user.getTargetSpy()));
            user.setTargetSpy(null);
        }
    }
}
