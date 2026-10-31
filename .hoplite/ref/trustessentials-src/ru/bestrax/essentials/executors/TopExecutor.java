/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Location
 *  org.bukkit.command.CommandSender
 *  org.bukkit.entity.Player
 */
package ru.bestrax.essentials.executors;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.bestrax.essentials.api.EssentialsApi;
import ru.bestrax.essentials.api.User;
import ru.bestrax.essentials.api.commands.TrustCommand;

public class TopExecutor
extends TrustCommand {
    public TopExecutor() {
        super("top", new String[0]);
        this.setPlayersOnly(true);
        this.setPermission("trustessentials.top");
        this.register();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player)sender;
        User user = EssentialsApi.getUserManager().getUser(player);
        Location newLocation = player.getLocation().clone();
        newLocation.setY((double)player.getWorld().getHighestBlockYAt(player.getLocation()));
        if (user.teleport(newLocation)) {
            this.sendMessage((CommandSender)player, "TOP", new String[0]);
        }
    }
}
