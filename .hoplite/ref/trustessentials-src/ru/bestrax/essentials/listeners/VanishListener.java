/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.md_5.bungee.api.ChatMessageType
 *  net.md_5.bungee.api.chat.BaseComponent
 *  net.md_5.bungee.api.chat.TextComponent
 *  org.bukkit.Bukkit
 *  org.bukkit.entity.Player
 *  org.bukkit.event.EventHandler
 *  org.bukkit.event.EventPriority
 *  org.bukkit.event.Listener
 *  org.bukkit.event.player.PlayerJoinEvent
 *  org.bukkit.event.server.TabCompleteEvent
 *  org.bukkit.plugin.Plugin
 */
package ru.bestrax.essentials.listeners;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.stream.Collectors;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.server.TabCompleteEvent;
import org.bukkit.plugin.Plugin;
import ru.bestrax.essentials.EssentialsPlugin;
import ru.bestrax.essentials.api.EssentialsApi;
import ru.bestrax.essentials.api.events.UserVanishStatusChangeEvent;
import ru.bestrax.essentials.base.objects.TrustUser;

public class VanishListener
implements Listener,
Runnable {
    private final Set<UUID> vanished = new ConcurrentSkipListSet<UUID>();
    private EssentialsPlugin ess;

    public VanishListener(EssentialsPlugin plugin) {
        plugin.getServer().getScheduler().runTaskTimerAsynchronously((Plugin)plugin, (Runnable)this, 0L, (long)plugin.getSettings().getInt("vanishMessageCooldown"));
        this.ess = plugin;
    }

    @EventHandler(priority=EventPriority.MONITOR)
    public void onVanishStatusChange(UserVanishStatusChangeEvent event) {
        if (!event.isCancelled()) {
            UUID playerUUID = event.getUser().getPlayer().getUniqueId();
            if (event.getStatus()) {
                this.vanished.add(playerUUID);
            } else {
                this.vanished.remove(playerUUID);
            }
        }
    }

    @EventHandler(priority=EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        TrustUser trustUser = (TrustUser)EssentialsApi.getUserManager().getUser(event.getPlayer());
        if (trustUser != null) {
            for (UUID uuid : this.vanished) {
                Player player = Bukkit.getPlayer((UUID)uuid);
                if (player == null) {
                    this.vanished.remove(uuid);
                    continue;
                }
                if (event.getPlayer().hasPermission("trustessentials.vanish.see")) continue;
                event.getPlayer().hidePlayer(player);
            }
            if (trustUser.isVanished()) {
                this.vanished.add(event.getPlayer().getUniqueId());
            }
            if (trustUser.isVanished()) {
                for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                    if (onlinePlayer.hasPermission("trustessentials.vanish.see")) continue;
                    onlinePlayer.hidePlayer(event.getPlayer());
                }
            }
        }
    }

    @EventHandler(priority=EventPriority.HIGHEST)
    public void onTabComplete(TabCompleteEvent event) {
        if (!event.getSender().hasPermission("trustessentials.vanish.see") && event.getCompletions().size() <= Bukkit.getOnlinePlayers().size()) {
            List complete = event.getCompletions().stream().filter(s -> {
                Player player = Bukkit.getPlayer((String)s);
                if (player == null) {
                    return true;
                }
                return !this.vanished.contains(player.getUniqueId());
            }).collect(Collectors.toList());
            event.setCompletions(complete);
        }
    }

    @Override
    public void run() {
        for (UUID uuid : this.vanished) {
            Player player = Bukkit.getPlayer((UUID)uuid);
            if (player == null) {
                this.vanished.remove(uuid);
                continue;
            }
            if (EssentialsApi.getUserManager().getUser(player).getGpsLocation() != null) continue;
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, (BaseComponent)new TextComponent(this.ess.getLang().getString("VANISH_ACTIONBAR", new String[0])));
        }
    }
}
