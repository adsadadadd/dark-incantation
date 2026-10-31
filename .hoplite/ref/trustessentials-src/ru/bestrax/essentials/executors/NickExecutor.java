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
package ru.bestrax.essentials.executors;

import java.util.Arrays;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.bestrax.essentials.api.EssentialsApi;
import ru.bestrax.essentials.api.User;
import ru.bestrax.essentials.api.commands.TrustCommand;

public class NickExecutor
extends TrustCommand {
    public NickExecutor() {
        super("nick", "nickname");
        this.setPermission("trustessentials.nickname");
        this.register();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length != 1 && args.length != 2) {
            this.sendMessages(sender, "NICKNAME_USAGE", new String[0]);
            return;
        }
        String target = args.length == 1 ? sender.getName() : args[0];
        String changedName = this.parseName(sender, args.length == 1 ? args[0] : args[1]);
        Player player = Bukkit.getPlayer((String)target);
        if (player == null || !player.isOnline()) {
            this.sendMessage(sender, "PLAYER_NOT_FOUND", "%name%", target);
            return;
        }
        if (!target.equalsIgnoreCase(sender.getName()) && !sender.hasPermission("trustessentials.nickname.other")) {
            this.sendMessage(sender, "NO_PERMISSION", "%permission%", "trustessentials.nickname.other");
            return;
        }
        if (changedName == null) {
            this.sendMessage(sender, "NICKNAME_ERROR", new String[0]);
            return;
        }
        User user = EssentialsApi.getUserManager().getUser(player);
        user.setFakeName(changedName.replaceFirst("~", "").equalsIgnoreCase("reset") || changedName.replaceFirst("~", "").equalsIgnoreCase("clear") ? null : changedName, true);
        if (sender.getName().equalsIgnoreCase(user.getUsername())) {
            return;
        }
        this.sendMessage(sender, "NICKNAME_TARGET", "%name%", player.getDisplayName());
    }

    private boolean isAbcd(String text) {
        List<Character> english = Arrays.asList(Character.valueOf('a'), Character.valueOf('b'), Character.valueOf('c'), Character.valueOf('c'), Character.valueOf('d'), Character.valueOf('e'), Character.valueOf('f'), Character.valueOf('g'), Character.valueOf('h'), Character.valueOf('i'), Character.valueOf('j'), Character.valueOf('k'), Character.valueOf('l'), Character.valueOf('m'), Character.valueOf('n'), Character.valueOf('o'), Character.valueOf('p'), Character.valueOf('q'), Character.valueOf('r'), Character.valueOf('s'), Character.valueOf('t'), Character.valueOf('u'), Character.valueOf('v'), Character.valueOf('w'), Character.valueOf('x'), Character.valueOf('y'), Character.valueOf('z'));
        for (char c : text.toCharArray()) {
            if (english.contains(Character.valueOf(c))) continue;
            return false;
        }
        return true;
    }

    private String parseName(CommandSender sender, String name) {
        String newName = ChatColor.translateAlternateColorCodes((char)'&', (String)name.replace('.', 'd').replace('_', 'd').replace("%", "d").replace("&k", "&n").replace("$", "d"));
        String noColorName = ChatColor.stripColor((String)newName.replace('&', '\u00a7'));
        if (!sender.hasPermission("trustessentials.nickname.admin")) {
            newName = "~" + name;
            noColorName = "~" + name;
        }
        if (noColorName.length() > EssentialsApi.getInstance().getSettings().getInt("lengthFakeName") || noColorName.length() <= 0) {
            return null;
        }
        if (!this.isAbcd(noColorName) && !sender.hasPermission("trustessentials.nickname.abcd")) {
            return null;
        }
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (!onlinePlayer.getDisplayName().equalsIgnoreCase(newName) && !onlinePlayer.getDisplayName().equalsIgnoreCase(noColorName)) continue;
            return null;
        }
        for (OfflinePlayer offlinePlayer : Bukkit.getOfflinePlayers()) {
            if (offlinePlayer.getName() == null || !offlinePlayer.getName().equalsIgnoreCase(noColorName)) continue;
            return null;
        }
        return sender.hasPermission("trustessentials.nickname.colors") ? newName : noColorName;
    }
}
