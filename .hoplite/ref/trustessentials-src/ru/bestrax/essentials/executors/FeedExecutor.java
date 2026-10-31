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

public class FeedExecutor
extends TrustCommand {
    public FeedExecutor() {
        super("feed", "food");
        this.setPermission("trustessentials.feed");
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
        if (!target.equalsIgnoreCase(sender.getName()) && !sender.hasPermission("trustessentials.feed.other")) {
            this.sendMessage(sender, "NO_PERMISSION", "%permission%", "trustessentials.feed.other");
            return;
        }
        player.setFoodLevel(20);
        this.sendMessage(sender, "FEED", "%name%", player.getDisplayName());
    }
}
