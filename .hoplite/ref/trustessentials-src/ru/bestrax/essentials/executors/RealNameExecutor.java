/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.command.CommandSender
 *  org.bukkit.entity.Player
 */
package ru.bestrax.essentials.executors;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.bestrax.essentials.api.commands.TrustCommand;

public class RealNameExecutor
extends TrustCommand {
    public RealNameExecutor() {
        super("realname", "rn");
        this.setPermission("trustessentials.realname");
        this.register();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length != 1) {
            this.sendMessages(sender, "REALNAME_USAGE", new String[0]);
            return;
        }
        String name = null;
        String changedName = this.parseName(args[0]);
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (!onlinePlayer.getDisplayName().startsWith(changedName)) continue;
            name = onlinePlayer.getName();
            break;
        }
        if (name == null) {
            this.sendMessage(sender, "PLAYER_NOT_FOUND", "%name%", changedName);
            return;
        }
        this.sendMessage(sender, "REALNAME", "%name%", name);
    }

    private String parseName(String name) {
        if (!name.startsWith("~")) {
            return "~" + name;
        }
        return name;
    }
}
