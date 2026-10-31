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
import ru.bestrax.essentials.api.config.Config;

public class BackExecutor
extends TrustCommand {
    public BackExecutor() {
        super("back", new String[0]);
        this.setPermission("trustessentials.back");
        this.setPlayersOnly(true);
        this.register();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Config settings = EssentialsApi.instance.getSettings();
        Player player = (Player)sender;
        User user = EssentialsApi.getUserManager().getUser(player);
        Location location = user.getLastLocation();
        if (location == null) {
            this.sendMessage((CommandSender)player, "BACK_FOUND", new String[0]);
            return;
        }
        player.teleport(location);
        if (settings.getBoolean("backBlock") && !player.hasPermission("trustessentials.back.cooldown.bypass")) {
            user.setBackTime(System.currentTimeMillis() + (long)settings.getInt("backTime"));
        }
        this.sendMessage((CommandSender)player, "BACK", new String[0]);
    }
}
