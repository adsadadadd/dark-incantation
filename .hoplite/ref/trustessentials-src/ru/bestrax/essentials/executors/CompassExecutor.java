/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.command.CommandSender
 *  org.bukkit.entity.Player
 */
package ru.bestrax.essentials.executors;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.bestrax.essentials.api.commands.TrustCommand;

public class CompassExecutor
extends TrustCommand {
    public CompassExecutor() {
        super("compass", new String[0]);
        this.setPermission("trustessentials.compass");
        this.setPlayersOnly(true);
        this.register();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player)sender;
        int bearing = (int)(player.getLocation().getYaw() + 180.0f + 360.0f) % 360;
        String dir = bearing < 23 ? "N" : (bearing < 68 ? "NE" : (bearing < 113 ? "E" : (bearing < 158 ? "SE" : (bearing < 203 ? "S" : (bearing < 248 ? "SW" : (bearing < 293 ? "W" : (bearing < 338 ? "NW" : "N")))))));
        this.sendMessage((CommandSender)player, "COMPASS", "%compass%", dir);
    }
}
