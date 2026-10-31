/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.md_5.bungee.api.ChatColor
 *  net.md_5.bungee.api.chat.ClickEvent
 *  net.md_5.bungee.api.chat.ClickEvent$Action
 *  net.md_5.bungee.api.chat.ComponentBuilder
 *  net.md_5.bungee.api.chat.HoverEvent
 *  net.md_5.bungee.api.chat.HoverEvent$Action
 *  org.bukkit.Bukkit
 *  org.bukkit.ChatColor
 *  org.bukkit.command.CommandSender
 *  org.bukkit.entity.Player
 */
package ru.bestrax.essentials.executors.teleportations;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.bestrax.essentials.api.EssentialsApi;
import ru.bestrax.essentials.api.User;
import ru.bestrax.essentials.api.commands.TrustCommand;
import ru.bestrax.essentials.base.objects.TrustUser;
import ru.bestrax.essentials.utils.BukkitUtils;
import ru.bestrax.essentials.utils.NMSUtils;

public class TpaExecutor
extends TrustCommand {
    public TpaExecutor() {
        super("tpa", "call");
        this.setPermission("trustessentials.tpa");
        this.setPlayersOnly(true);
        this.register();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player)sender;
        if (args.length < 1) {
            this.sendMessages(sender, "TPA_USAGE", new String[0]);
        } else {
            String bot = EssentialsApi.getBotManager() == null ? null : EssentialsApi.getBotManager().getBot(args[0]);
            Player otherPlayer = Bukkit.getPlayer((String)args[0]);
            if (otherPlayer == null && bot == null) {
                this.sendMessage(sender, "PLAYER_NOT_FOUND", "%name%", args[0]);
            } else if (otherPlayer != null && otherPlayer.getName().equalsIgnoreCase(sender.getName())) {
                this.sendMessage(sender, "TPA_YOU", new String[0]);
            } else {
                if (otherPlayer == null) {
                    this.sendMessage(sender, "TPA_SEND", "%name%", bot);
                    player.playSound(player.getLocation(), BukkitUtils.getBlockBellSound(), 1.0f, 1.0f);
                    return;
                }
                User otherUser = EssentialsApi.getUserManager().getUser(otherPlayer);
                if (!otherUser.isTpToggle() && !player.hasPermission("trustessentials.tptoggle.bypass")) {
                    this.sendMessage(sender, "TPTOGGLE_BLOCK", "%name%", otherUser.getUsername());
                } else {
                    TrustUser craftUser = (TrustUser)otherUser;
                    if (!craftUser.addCallRequest(player)) {
                        this.sendMessage(sender, "TPA_ERROR", new String[0]);
                    } else {
                        for (String words : this.getStringList("TPA", new String[0])) {
                            if (words.startsWith("json:")) {
                                NMSUtils.sendMessage(otherPlayer, words.replace("json:", ""));
                                continue;
                            }
                            ComponentBuilder componentBuilder = new ComponentBuilder("");
                            for (String word : words.split(" ")) {
                                String delimiter = "";
                                if (!words.split(" ")[0].equals(word)) {
                                    delimiter = delimiter + " ";
                                }
                                if (word.equalsIgnoreCase("%tpaccept%")) {
                                    componentBuilder.append(delimiter + this.getString("TPACCEPT_HERE", new String[0])).event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tpaccept " + sender.getName())).event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("\u041d\u0430\u0436\u043c\u0438\u0442\u0435, \u0447\u0442\u043e-\u0431\u044b \u043f\u0440\u0438\u043d\u044f\u0442\u044c \u0437\u0430\u043f\u0440\u043e\u0441.").color(net.md_5.bungee.api.ChatColor.GRAY).create())).create();
                                    continue;
                                }
                                if (word.equalsIgnoreCase("%tpcancel%")) {
                                    componentBuilder.append(delimiter + this.getString("TPCANCEL_HERE", new String[0])).event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tpcancel " + sender.getName())).event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("\u041d\u0430\u0436\u043c\u0438\u0442\u0435, \u0447\u0442\u043e-\u0431\u044b \u043e\u0442\u043a\u043b\u043e\u043d\u0438\u0442\u044c \u0437\u0430\u043f\u0440\u043e\u0441.").color(net.md_5.bungee.api.ChatColor.GRAY).create())).create();
                                    continue;
                                }
                                componentBuilder.append(delimiter + ChatColor.translateAlternateColorCodes((char)'&', (String)word.replace("%name%", sender.getName()))).create();
                            }
                            otherPlayer.playSound(otherPlayer.getLocation(), BukkitUtils.getBlockBellSound(), 1.0f, 1.0f);
                            player.playSound(player.getLocation(), BukkitUtils.getBlockBellSound(), 1.0f, 1.0f);
                            otherPlayer.spigot().sendMessage(componentBuilder.create());
                        }
                        this.sendMessage(sender, "TPA_SEND", "%name%", otherPlayer.getDisplayName());
                    }
                }
            }
        }
    }
}
