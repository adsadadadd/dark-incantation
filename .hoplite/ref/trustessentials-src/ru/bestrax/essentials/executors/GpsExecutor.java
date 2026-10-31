/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.Location
 *  org.bukkit.World
 *  org.bukkit.command.CommandSender
 *  org.bukkit.entity.Player
 */
package ru.bestrax.essentials.executors;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.bestrax.essentials.api.EssentialsApi;
import ru.bestrax.essentials.api.User;
import ru.bestrax.essentials.api.commands.TrustCommand;
import ru.bestrax.essentials.utils.StringUtils;

public class GpsExecutor
extends TrustCommand {
    public GpsExecutor() {
        super("gps", "radar");
        this.setPermission("trustessentials.gps");
        this.setPlayersOnly(true);
        this.register();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        World world;
        Player player = (Player)sender;
        User user = EssentialsApi.getUserManager().getUser(player);
        if (user.getGpsLocation() != null) {
            user.setGpsLocation(null);
            this.sendMessage((CommandSender)player, "GPS_DISABLE", new String[0]);
            return;
        }
        if (args.length != 3 && args.length != 4) {
            this.sendMessages((CommandSender)player, "GPS_USAGE", new String[0]);
            return;
        }
        if (!StringUtils.isNumber(args[0])) {
            this.sendMessage((CommandSender)player, "NUMBER_ONLY", "%replaced%", args[0]);
            return;
        }
        if (!StringUtils.isNumber(args[1])) {
            this.sendMessage((CommandSender)player, "NUMBER_ONLY", "%replaced%", args[1]);
            return;
        }
        if (!StringUtils.isNumber(args[2])) {
            this.sendMessage((CommandSender)player, "NUMBER_ONLY", "%replaced%", args[2]);
            return;
        }
        int x = Integer.parseInt(args[0]);
        int y = Integer.parseInt(args[1]);
        int z = Integer.parseInt(args[2]);
        World world2 = world = args.length == 4 ? Bukkit.getWorld((String)args[3]) : player.getWorld();
        if (world == null) {
            this.sendMessage((CommandSender)player, "GPS_WORLD_NOT_FOUND", new String[0]);
            return;
        }
        user.setGpsLocation(new Location(world, (double)x, (double)y, (double)z));
        this.sendMessage((CommandSender)player, "GPS_ENABLE", new String[0]);
    }
}
