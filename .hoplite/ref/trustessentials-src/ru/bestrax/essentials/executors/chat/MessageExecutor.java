/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.md_5.bungee.api.chat.BaseComponent
 *  net.md_5.bungee.api.chat.ClickEvent
 *  net.md_5.bungee.api.chat.ClickEvent$Action
 *  net.md_5.bungee.api.chat.HoverEvent
 *  net.md_5.bungee.api.chat.HoverEvent$Action
 *  net.md_5.bungee.api.chat.TextComponent
 *  org.bukkit.Bukkit
 *  org.bukkit.ChatColor
 *  org.bukkit.OfflinePlayer
 *  org.bukkit.command.CommandSender
 *  org.bukkit.entity.Player
 */
package ru.bestrax.essentials.executors.chat;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.bestrax.essentials.api.EssentialsApi;
import ru.bestrax.essentials.api.EssentialsStatistic;
import ru.bestrax.essentials.api.User;
import ru.bestrax.essentials.api.VaultAPI;
import ru.bestrax.essentials.api.commands.TrustCommand;
import ru.bestrax.essentials.api.config.Config;
import ru.bestrax.essentials.api.cooldown.Cooldowns;
import ru.bestrax.essentials.utils.BukkitUtils;
import ru.bestrax.essentials.utils.TimeUtils;

public abstract class MessageExecutor
extends TrustCommand {
    public MessageExecutor(String command, String ... aliases) {
        super(command, aliases);
    }

    public void sendBot(Player player, String bot, String oldMessage) {
        Config settings = EssentialsApi.getInstance().getSettings();
        long playOneTicks = EssentialsApi.getStatisticManager().getUser((OfflinePlayer)player).getStatistic(EssentialsStatistic.PLAY_ONE_TICK);
        if (playOneTicks < (long)settings.getInt("chatUseMinimalTime") && !player.hasPermission("trstessentials.chat.newplayerscooldown.bypass")) {
            this.sendMessage((CommandSender)player, "CHAT_MINIMAL_TIME", "%time%", TimeUtils.getTimeInSeconds(((long)settings.getInt("chatUseMinimalTime") - playOneTicks) / 20L));
            return;
        }
        player.playSound(player.getLocation(), BukkitUtils.getBlockBellSound(), 1.0f, 1.0f);
        String message = oldMessage;
        String formatSender = settings.getString("privateChatFormatSender", new String[0]);
        formatSender = settings.replace(formatSender, "%prefixSender%", VaultAPI.getPrefix(player), "%sender%", ChatColor.translateAlternateColorCodes((char)'&', (String)player.getDisplayName()), "%prefixTarget%", EssentialsApi.getBotManager().getChatPrefix(bot), "%target%", ChatColor.translateAlternateColorCodes((char)'&', (String)bot), "%message%", message);
        EssentialsApi.getUserManager().getUser(player).setReplyPlayer(bot);
        player.sendMessage(formatSender);
        if (settings.getBoolean("privateChatCooldown")) {
            Cooldowns.set(player.getName(), "PRIVATECHAT", settings.getInt("privateChatCooldownMillis"));
        }
    }

    public BaseComponent[] getClickMessage(String format, HoverEvent hoverEvent, ClickEvent clickEvent) {
        BaseComponent[] text;
        for (BaseComponent baseComponent : text = TextComponent.fromLegacyText((String)format)) {
            baseComponent.setHoverEvent(hoverEvent);
            baseComponent.setClickEvent(clickEvent);
        }
        return text;
    }

    public void send(Player who, Player target, String message) {
        if (Cooldowns.has(who.getName(), "PRIVATECHAT") && !who.hasPermission("trustessentials.privatechat.cooldown.bypass")) {
            this.sendMessage((CommandSender)who, "PRIVATECHAT_COOLDOWN", "%time%", TimeUtils.getTimeInSeconds(Cooldowns.get(who.getName(), "PRIVATECHAT") / 1000));
            return;
        }
        Config settings = EssentialsApi.getInstance().getSettings();
        long playOneTicks = EssentialsApi.getStatisticManager().getUser((OfflinePlayer)who).getStatistic(EssentialsStatistic.PLAY_ONE_TICK);
        if (playOneTicks < (long)settings.getInt("chatUseMinimalTime") && !who.hasPermission("trstessentials.chat.newplayerscooldown.bypass")) {
            this.sendMessage((CommandSender)who, "CHAT_MINIMAL_TIME", "%time%", TimeUtils.getTimeInSeconds(((long)settings.getInt("chatUseMinimalTime") - playOneTicks) / 20L));
            return;
        }
        target.playSound(target.getLocation(), BukkitUtils.getBlockBellSound(), 1.0f, 1.0f);
        who.playSound(who.getLocation(), BukkitUtils.getBlockBellSound(), 1.0f, 1.0f);
        message = settings.getBoolean("rainbowPrivateChatMessage") ? (who.hasPermission("trustessentials.privatechat.rainbow") ? ChatColor.translateAlternateColorCodes((char)'&', (String)message) : message) : message;
        String formatTarget = settings.getString("privateChatFormatTarget", new String[0]);
        formatTarget = settings.replace(formatTarget, "%prefixSender%", VaultAPI.getPrefix(target), "%sender%", ChatColor.translateAlternateColorCodes((char)'&', (String)who.getDisplayName()), "%prefixTarget%", VaultAPI.getPrefix(target), "%target%", ChatColor.translateAlternateColorCodes((char)'&', (String)target.getDisplayName()), "%message%", message);
        String formatSender = settings.getString("privateChatFormatSender", new String[0]);
        formatSender = settings.replace(formatSender, "%prefixSender%", VaultAPI.getPrefix(target), "%sender%", ChatColor.translateAlternateColorCodes((char)'&', (String)who.getDisplayName()), "%prefixTarget%", VaultAPI.getPrefix(target), "%target%", ChatColor.translateAlternateColorCodes((char)'&', (String)target.getDisplayName()), "%message%", message);
        target.spigot().sendMessage(this.getClickMessage(formatTarget, new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText((String)"\u00a77\u041d\u0430\u0436\u043c\u0438\u0442\u0435, \u0447\u0442\u043e\u0431\u044b \u043e\u0442\u0432\u0435\u0442\u0438\u0442\u044c")), new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/msg " + who.getName() + " ")));
        who.spigot().sendMessage(this.getClickMessage(formatSender, new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText((String)"\u00a77\u041d\u0430\u0436\u043c\u0438\u0442\u0435, \u0447\u0442\u043e\u0431\u044b \u0434\u043e\u043f\u043e\u043b\u043d\u0438\u0442\u044c \u0441\u043e\u043e\u0431\u0449\u0435\u043d\u0438\u0435")), new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/msg " + target.getName() + " ")));
        String socialSpyFormat = settings.getString("socialSpyFormat", new String[0]);
        socialSpyFormat = settings.replace(socialSpyFormat, "%prefixSender%", VaultAPI.getPrefix(who), "%sender%", ChatColor.translateAlternateColorCodes((char)'&', (String)who.getDisplayName()), "%prefixTarget%", VaultAPI.getPrefix(target), "%target%", ChatColor.translateAlternateColorCodes((char)'&', (String)target.getDisplayName()), "%message%", message);
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            User user;
            if (!onlinePlayer.hasPermission("trustessentials.socialspy") || !(user = EssentialsApi.getUserManager().getUser(onlinePlayer)).isSocialSpy()) continue;
            onlinePlayer.sendMessage(socialSpyFormat);
        }
        User whoUser = EssentialsApi.getUserManager().getUser(who);
        User targetUser = EssentialsApi.getUserManager().getUser(target);
        whoUser.setReplyPlayer(targetUser.getUsername());
        targetUser.setReplyPlayer(whoUser.getUsername());
        if (settings.getBoolean("privateChatCooldown")) {
            Cooldowns.set(who.getName(), "PRIVATECHAT", settings.getInt("privateChatCooldownMillis"));
        }
    }
}
