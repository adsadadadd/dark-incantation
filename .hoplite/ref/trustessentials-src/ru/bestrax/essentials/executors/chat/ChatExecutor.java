/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.ChatColor
 *  org.bukkit.OfflinePlayer
 *  org.bukkit.command.CommandSender
 *  org.bukkit.entity.Player
 */
package ru.bestrax.essentials.executors.chat;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.bestrax.essentials.EssentialsPlugin;
import ru.bestrax.essentials.api.EssentialsApi;
import ru.bestrax.essentials.api.EssentialsStatistic;
import ru.bestrax.essentials.api.PlaceholderAPI;
import ru.bestrax.essentials.api.VaultAPI;
import ru.bestrax.essentials.api.commands.TrustCommand;
import ru.bestrax.essentials.api.config.Config;
import ru.bestrax.essentials.api.cooldown.Cooldowns;
import ru.bestrax.essentials.utils.StringUtils;
import ru.bestrax.essentials.utils.TimeUtils;

public abstract class ChatExecutor
extends TrustCommand {
    public ChatExecutor(String command, String ... aliases) {
        super(command, aliases);
        this.setPermission("trustessentials." + command);
        this.register();
    }

    public void send(CommandSender sender, String chat, String ... args) {
        EssentialsPlugin plugin = EssentialsApi.getInstance();
        if (args.length == 0) {
            this.sendMessages(sender, chat.toUpperCase() + "CHAT_USAGE", new String[0]);
            return;
        }
        Config settings = plugin.getSettings();
        long playOneTicks = EssentialsApi.getStatisticManager().getUser((OfflinePlayer)((Player)sender)).getStatistic(EssentialsStatistic.PLAY_ONE_TICK);
        if (playOneTicks < (long)settings.getInt("chatUseMinimalTime") && !sender.hasPermission("trstessentials.chat.newplayerscooldown.bypass")) {
            this.sendMessage(sender, "CHAT_MINIMAL_TIME", "%time%", TimeUtils.getTimeInSeconds(((long)settings.getInt("chatUseMinimalTime") - playOneTicks) / 20L));
            return;
        }
        if (Cooldowns.has(sender.getName(), "ESSENTIALS_" + chat.toUpperCase() + "CHAT")) {
            this.sendMessage(sender, "COOLDOWN_" + chat.toUpperCase() + "CHAT", "%time%", TimeUtils.getTimeInSeconds(Cooldowns.get(sender.getName(), "ESSENTIALS_" + chat.toUpperCase() + "CHAT") / 1000));
            return;
        }
        String message = StringUtils.buildString(args, " ");
        message = settings.getBoolean("rainbow" + chat + "ChatMessage") ? (sender.hasPermission("trustessentials." + chat.toLowerCase() + "chat.rainbow") ? ChatColor.translateAlternateColorCodes((char)'&', (String)message) : message) : message;
        String name = sender instanceof Player ? ((Player)sender).getDisplayName() : sender.getName();
        String prefix = sender instanceof Player ? VaultAPI.getPrefix((Player)sender) : "&c";
        String suffix = sender instanceof Player ? VaultAPI.getSuffix((Player)sender) : "&c";
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (!onlinePlayer.hasPermission("trustessentials." + chat.toLowerCase() + "chat")) continue;
            if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null && sender instanceof Player) {
                message = PlaceholderAPI.replacePlaceHolder((Player)sender, message);
            }
            onlinePlayer.sendMessage(settings.getString(chat.toLowerCase() + "ChatFormat", "%message%", message, "%player%", name, "%prefix%", prefix, "%suffix%", suffix));
        }
        if (!sender.hasPermission("trustessentials." + chat.toLowerCase() + "chat.cooldown.bypass") && settings.getBoolean(chat.toLowerCase() + "ChatCooldown")) {
            Cooldowns.set(sender.getName(), "ESSENTIALS_" + chat.toUpperCase() + "CHAT", settings.getInt(chat.toLowerCase() + "ChatCooldownMillis"));
        }
    }
}
