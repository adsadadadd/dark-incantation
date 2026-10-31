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
 *  org.bukkit.event.EventHandler
 *  org.bukkit.event.EventPriority
 *  org.bukkit.event.Listener
 *  org.bukkit.event.block.BlockBreakEvent
 *  org.bukkit.event.block.BlockPlaceEvent
 *  org.bukkit.event.entity.PlayerDeathEvent
 *  org.bukkit.event.player.AsyncPlayerChatEvent
 *  org.bukkit.event.player.AsyncPlayerPreLoginEvent
 *  org.bukkit.event.player.AsyncPlayerPreLoginEvent$Result
 *  org.bukkit.event.player.PlayerJoinEvent
 *  org.bukkit.event.player.PlayerQuitEvent
 */
package ru.bestrax.essentials.listeners;

import java.util.Map;
import java.util.stream.Collectors;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import ru.bestrax.essentials.api.EssentialsApi;
import ru.bestrax.essentials.api.EssentialsStatistic;
import ru.bestrax.essentials.api.PlaceholderAPI;
import ru.bestrax.essentials.api.StatisticUser;
import ru.bestrax.essentials.api.User;
import ru.bestrax.essentials.api.VaultAPI;
import ru.bestrax.essentials.api.config.Config;
import ru.bestrax.essentials.api.cooldown.Cooldowns;
import ru.bestrax.essentials.api.events.UserChatMessageEvent;
import ru.bestrax.essentials.api.manager.StatisticManager;
import ru.bestrax.essentials.api.manager.UserManager;
import ru.bestrax.essentials.utils.AkyloffClanUtils;
import ru.bestrax.essentials.utils.BukkitUtils;
import ru.bestrax.essentials.utils.CheckUtils;
import ru.bestrax.essentials.utils.ClanUtils;
import ru.bestrax.essentials.utils.ComponentBuilder;
import ru.bestrax.essentials.utils.JampireClanUtils;
import ru.bestrax.essentials.utils.LocationUtils;
import ru.bestrax.essentials.utils.MessagesUtils;
import ru.bestrax.essentials.utils.TimeUtils;

public class UserListener
implements Listener {
    private final UserManager userManager = EssentialsApi.getUserManager();
    private final StatisticManager statisticManager = EssentialsApi.getStatisticManager();

    @EventHandler(priority=EventPriority.MONITOR)
    public void onLogin(AsyncPlayerPreLoginEvent e) {
        if (e.getLoginResult() == AsyncPlayerPreLoginEvent.Result.ALLOWED) {
            this.userManager.loadUser(e.getName());
            EssentialsApi.getStatisticManager().loadStatisticUser(e.getName());
        }
    }

    @EventHandler(priority=EventPriority.MONITOR)
    public void onAsyncQuit(PlayerQuitEvent event) {
        EssentialsApi.getStatisticManager().unloadStatisticUser(event.getPlayer().getName());
        EssentialsApi.getEnderChestManager().saveEnderChest(event.getPlayer(), this.userManager.getUser(event.getPlayer()).getEnderChest());
        this.userManager.removeUser(event.getPlayer().getName());
    }

    @EventHandler
    public void onBreakBlock(BlockBreakEvent e) {
        StatisticUser statisticUser = this.statisticManager.getUser((OfflinePlayer)e.getPlayer());
        statisticUser.setStatistic(EssentialsStatistic.BREAK_BLOCK, statisticUser.getStatistic(EssentialsStatistic.BREAK_BLOCK) + 1L);
    }

    @EventHandler
    public void onPlaceBlock(BlockPlaceEvent e) {
        StatisticUser statisticUser = this.statisticManager.getUser((OfflinePlayer)e.getPlayer());
        statisticUser.setStatistic(EssentialsStatistic.PLACE_BLOCK, statisticUser.getStatistic(EssentialsStatistic.PLACE_BLOCK) + 1L);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Cooldowns.set(e.getPlayer().getName(), "joinChatCooldown", EssentialsApi.getInstance().getSettings().getFile().getInt("joinChatCooldown"));
        if (EssentialsApi.getInstance().getSettings().getBoolean("joinTitle")) {
            e.getPlayer().sendTitle(EssentialsApi.getInstance().getLang().getString("JOIN_TITLE", new String[0]).split(";")[0], EssentialsApi.getInstance().getLang().getString("JOIN_TITLE", new String[0]).split(";")[1]);
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        StatisticUser deathUser;
        if (e.getEntity().getKiller() != null) {
            deathUser = this.statisticManager.getUser((OfflinePlayer)e.getEntity().getKiller());
            deathUser.setStatistic(EssentialsStatistic.KILLS, deathUser.getStatistic(EssentialsStatistic.KILLS) + 1L);
        }
        deathUser = this.statisticManager.getUser((OfflinePlayer)e.getEntity());
        deathUser.setStatistic(EssentialsStatistic.DEATH, deathUser.getStatistic(EssentialsStatistic.DEATH) + 1L);
    }

    @EventHandler(priority=EventPriority.LOWEST)
    public void onChatPlayer(AsyncPlayerChatEvent e) {
        Player player = e.getPlayer();
        Config lang = EssentialsApi.getInstance().getLang();
        Config settings = EssentialsApi.getInstance().getSettings();
        if (lang != null && settings != null) {
            String format;
            boolean global;
            long playOneTicks = EssentialsApi.getStatisticManager().getUser((OfflinePlayer)e.getPlayer()).getStatistic(EssentialsStatistic.PLAY_ONE_TICK);
            if (playOneTicks < (long)settings.getInt("chatUseMinimalTime") && !player.hasPermission("trustessentials.chat.newplayerscooldown.bypass")) {
                player.sendMessage(lang.getString("CHAT_MINIMAL_TIME", "%time%", TimeUtils.getTimeInSeconds(((long)settings.getInt("chatUseMinimalTime") - playOneTicks) / 20L)));
                e.setCancelled(true);
                return;
            }
            if (Cooldowns.has(e.getPlayer().getName(), "joinChatCooldown")) {
                player.sendMessage(lang.getString("JOIN_CHAT_COOLDOWN_MESSAGE", "%time%", TimeUtils.getTimeInSeconds(Cooldowns.get(e.getPlayer().getName(), "joinChatCooldown") / 1000)));
                e.setCancelled(true);
                return;
            }
            if (Bukkit.getPluginManager().isPluginEnabled("SunCheck") && CheckUtils.isCheck(player)) {
                e.setCancelled(true);
                return;
            }
            e.setMessage(e.getMessage().replaceAll("%", "%%"));
            if (Cooldowns.has(player.getName(), "ESSENTIALS_CHAT")) {
                player.sendMessage(lang.getString("COOLDOWN_CHAT", "%time%", TimeUtils.getTimeInSeconds(Cooldowns.get(player.getName(), "ESSENTIALS_CHAT") / 1000)));
                e.setCancelled(true);
                return;
            }
            if (e.getMessage().contains("%")) {
                e.setCancelled(true);
                return;
            }
            String message = settings.getBoolean("rainbowChatMessage") ? (player.hasPermission("trustessentials.chat.rainbow") ? ChatColor.translateAlternateColorCodes((char)'&', (String)e.getMessage()) : e.getMessage()) : e.getMessage();
            boolean bl = global = message.startsWith("!") && settings.getBoolean("globalChat");
            if (settings.getBoolean("globalChat") && !global) {
                e.getRecipients().removeIf(all -> {
                    double distance = LocationUtils.distance(all.getLocation(), player.getLocation());
                    return distance < 0.0 || distance > (double)settings.getInt("localChatRadius");
                });
            }
            e.getRecipients().removeIf(player1 -> {
                User user2 = EssentialsApi.getUserManager().getUser((Player)player1);
                return !user2.isChatToggle();
            });
            String string = format = global ? settings.getString("globalFormat", new String[0]) : settings.getString("localFormat", new String[0]);
            if (global) {
                e.setMessage(message.replaceFirst("!", "").trim());
                if (e.getMessage().isEmpty()) {
                    e.setCancelled(true);
                    return;
                }
            }
            if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
                format = PlaceholderAPI.replacePlaceHolder(player, format);
            }
            String additionalSuffix = "";
            String group = VaultAPI.getPrimaryGroup(player);
            if (settings.getFile().contains("chatAdditionalSuffix") && settings.getFile().contains("chatAdditionalSuffix." + group)) {
                additionalSuffix = settings.getString("chatAdditionalSuffix." + group, new String[0]);
            }
            UserChatMessageEvent event = new UserChatMessageEvent(EssentialsApi.getUserManager().getUser(player), e.getMessage());
            BukkitUtils.callEvent(event, EssentialsApi.getInstance());
            if (event.isCancelled()) {
                e.setCancelled(true);
                return;
            }
            String additional = format.contains("%additinalsuffix%") ? format.split("%additionalsuffix%")[0] : format.split("%message%")[0];
            String[] spaces = additional.split(" ");
            for (Map.Entry<Player, String> playerStringEntry : event.getCustomMessages().entrySet()) {
                playerStringEntry.setValue(playerStringEntry.getValue().replace("%resetColor%", spaces.length > 0 ? spaces[spaces.length - 1] + (format.contains("%additinalsuffix%") ? additionalSuffix : "") : (format.contains("%additinalsuffix%") ? additionalSuffix : "")));
            }
            e.setMessage(event.getMessage().replace("%resetColor%", spaces.length > 0 ? spaces[spaces.length - 1] + (format.contains("%additinalsuffix%") ? additionalSuffix : "") : (format.contains("%additinalsuffix%") ? additionalSuffix : "")));
            for (Map.Entry<Player, String> playerStringEntry : event.getCustomMessages().entrySet()) {
                playerStringEntry.setValue(settings.replace(format, "%message%", playerStringEntry.getValue(), "%clan%", Bukkit.getPluginManager().isPluginEnabled("JampireClans") ? JampireClanUtils.getClanDisplayName(player, false) : (Bukkit.getPluginManager().isPluginEnabled("Clans") ? ClanUtils.getClanDisplayName(player, false) : (Bukkit.getPluginManager().isPluginEnabled("AkyloffClans") ? AkyloffClanUtils.getClanDisplayName(player, false) : "")), "%clandecorate%", Bukkit.getPluginManager().isPluginEnabled("JampireClans") ? JampireClanUtils.getClanDisplayName(player, true) : (Bukkit.getPluginManager().isPluginEnabled("Clans") ? ClanUtils.getClanDisplayName(player, true) : (Bukkit.getPluginManager().isPluginEnabled("AkyloffClans") ? AkyloffClanUtils.getClanDisplayName(player, true) : "")), "%player%", ChatColor.translateAlternateColorCodes((char)'&', (String)player.getDisplayName()), "%prefix%", VaultAPI.getPrefix(player), "%suffix%", VaultAPI.getSuffix(player), "%additionalsuffix%", additionalSuffix).replaceAll("%", "%%"));
            }
            format = settings.format(settings.replace(format, "%message%", e.getMessage(), "%clan%", Bukkit.getPluginManager().isPluginEnabled("JampireClans") ? JampireClanUtils.getClanDisplayName(player, false) : (Bukkit.getPluginManager().isPluginEnabled("Clans") ? ClanUtils.getClanDisplayName(player, false) : (Bukkit.getPluginManager().isPluginEnabled("AkyloffClans") ? AkyloffClanUtils.getClanDisplayName(player, false) : "")), "%clandecorate%", Bukkit.getPluginManager().isPluginEnabled("JampireClans") ? JampireClanUtils.getClanDisplayName(player, true) : (Bukkit.getPluginManager().isPluginEnabled("Clans") ? ClanUtils.getClanDisplayName(player, true) : (Bukkit.getPluginManager().isPluginEnabled("AkyloffClans") ? AkyloffClanUtils.getClanDisplayName(player, true) : "")), "%player%", ChatColor.translateAlternateColorCodes((char)'&', (String)player.getDisplayName()), "%prefix%", VaultAPI.getPrefix(player), "%suffix%", VaultAPI.getSuffix(player), "%additionalsuffix%", additionalSuffix).replaceAll("%", "%%"));
            if (settings.getBoolean("globalLocalChat")) {
                e.setFormat(format);
            }
            if (!global && e.getRecipients().stream().filter(x -> !x.getName().equalsIgnoreCase(e.getPlayer().getName())).filter(x -> {
                User user = EssentialsApi.getUserManager().getUser((Player)x);
                return !user.isVanished() && !EssentialsApi.getSpectateManager().isSpectation(user.getPlayer());
            }).collect(Collectors.toList()).size() == 0 && !e.isCancelled()) {
                MessagesUtils.sendMessages((CommandSender)e.getPlayer(), EssentialsApi.getInstance().getLang().getStringList("NO_ONE_HEARD_YOU", new String[0]));
            }
            if (!e.isCancelled() && e.getRecipients().size() > 0 && settings.getBoolean("chatMessageEvents")) {
                e.setCancelled(true);
                new ComponentBuilder("");
                for (Player recipient : e.getRecipients()) {
                    if (event.getCustomMessages().containsKey(recipient)) {
                        recipient.spigot().sendMessage(this.getBaseFromText(player, settings.format(event.getCustomMessages().get(recipient)), settings));
                        continue;
                    }
                    recipient.spigot().sendMessage(this.getBaseFromText(player, format, settings));
                }
                Bukkit.getLogger().info(format);
            }
            if (!player.hasPermission("trustessentials.chat.cooldown.bypass")) {
                Cooldowns.set(player.getName(), "ESSENTIALS_CHAT", settings.getInt("cooldownChat"));
            }
        }
    }

    private BaseComponent[] getBaseFromText(Player player, String format, Config settings) {
        BaseComponent[] result;
        BaseComponent[] var8 = result = TextComponent.fromLegacyText((String)format);
        int var7 = result.length;
        for (int var6 = 0; var6 < var7; ++var6) {
            BaseComponent baseComponent = var8[var6];
            if (!baseComponent.toLegacyText().contains(ChatColor.translateAlternateColorCodes((char)'&', (String)player.getDisplayName()))) continue;
            if (settings.getBoolean("hoverChatEvent")) {
                StringBuilder hover = new StringBuilder();
                for (int i = 0; i < settings.getStringList("hoverChatEventText", new String[0]).size(); ++i) {
                    if (i != 0) {
                        hover.append("\n");
                    }
                    String hoverChatEventText = settings.getStringList("hoverChatEventText", new String[0]).get(i);
                    hover.append(Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI") ? PlaceholderAPI.replacePlaceHolder(player, hoverChatEventText.replace("%name%", player.getName())) : hoverChatEventText.replace("%name%", player.getName()));
                }
                baseComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.valueOf((String)settings.getString("hoverChatEventAction", new String[0])), TextComponent.fromLegacyText((String)hover.toString())));
            }
            if (!settings.getBoolean("clickChatEvent")) continue;
            baseComponent.setClickEvent(new ClickEvent(ClickEvent.Action.valueOf((String)settings.getString("clickChatEventAction", new String[0])), settings.getString("clickChatEventText", new String[0]).replace("%name%", player.getName())));
        }
        return result;
    }
}
