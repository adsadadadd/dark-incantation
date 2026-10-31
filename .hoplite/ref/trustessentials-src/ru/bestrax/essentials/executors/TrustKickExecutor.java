/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.ChatColor
 *  org.bukkit.command.CommandSender
 *  org.bukkit.entity.Player
 */
package ru.bestrax.essentials.executors;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.bestrax.essentials.api.commands.TrustCommand;
import ru.bestrax.essentials.utils.StringUtils;

public class TrustKickExecutor
extends TrustCommand {
    public TrustKickExecutor() {
        super("trustkick", new String[0]);
        this.setPermission("trustessentials.kick");
        this.register();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length < 2) {
            this.sendMessage(sender, "KICK_USAGE", new String[0]);
            return;
        }
        Player player = Bukkit.getPlayer((String)args[0]);
        String reason = StringUtils.buildString(args, " ", 1);
        player.kickPlayer(ChatColor.translateAlternateColorCodes((char)'&', (String)reason));
    }
}
