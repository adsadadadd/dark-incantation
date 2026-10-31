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
import ru.bestrax.essentials.api.EssentialsApi;
import ru.bestrax.essentials.api.User;
import ru.bestrax.essentials.api.commands.TrustCommand;

public class EnderChestExecutor
extends TrustCommand {
    public EnderChestExecutor() {
        super("enderchest", "ec");
        this.setPlayersOnly(true);
        this.setPermission("trustessentials.enderchest");
        this.register();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player target;
        Player player = (Player)sender;
        Player player2 = target = args.length >= 1 ? Bukkit.getPlayer((String)args[0]) : player;
        if (target == null || !target.isOnline()) {
            this.sendMessage(sender, "PLAYER_NOT_FOUND", "%name%", args[0]);
            return;
        }
        if (!target.getName().equalsIgnoreCase(sender.getName()) && !sender.hasPermission("trustessentials.enderchest.other")) {
            this.sendMessage(sender, "NO_PERMISSION", "%permission%", "trustessentials.enderchest.other");
            return;
        }
        User user = EssentialsApi.getUserManager().getUser(target);
        EssentialsApi.getEnderChestManager().openEnderChest(player, user.getEnderChest());
    }
}
