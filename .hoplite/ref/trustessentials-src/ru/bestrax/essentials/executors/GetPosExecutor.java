/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.Location
 *  org.bukkit.command.CommandSender
 *  org.bukkit.entity.Player
 */
package ru.bestrax.essentials.executors;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.bestrax.essentials.api.commands.TrustCommand;

public class GetPosExecutor
extends TrustCommand {
    public GetPosExecutor() {
        super("getpos", "getcord", "curpos");
        this.setPermission("trustessentials.getpos");
        this.register();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        String target = args.length == 0 ? sender.getName() : args[0];
        Player player = Bukkit.getPlayer((String)target);
        if (player == null || !player.isOnline()) {
            this.sendMessage(sender, "PLAYER_NOT_FOUND", "%name%", target);
            return;
        }
        if (!target.equalsIgnoreCase(sender.getName()) && !sender.hasPermission("trustessentials.getpos.other")) {
            this.sendMessage(sender, "NO_PERMISSION", "%permission%", "trustessentials.getpos.other");
            return;
        }
        Location location = player.getLocation();
        if (target.equalsIgnoreCase(sender.getName())) {
            this.sendMessage(sender, "GETPOS", "%world%", location.getWorld().getName(), "%x%", String.valueOf(location.getBlockX()), "%y%", String.valueOf(location.getBlockY()), "%z%", String.valueOf(location.getBlockZ()));
        } else {
            this.sendMessage(sender, "GETPOS_TARGET", "%name%", player.getDisplayName(), "%world%", location.getWorld().getName(), "%x%", String.valueOf(location.getBlockX()), "%y%", String.valueOf(location.getBlockY()), "%z%", String.valueOf(location.getBlockZ()));
        }
    }
}
