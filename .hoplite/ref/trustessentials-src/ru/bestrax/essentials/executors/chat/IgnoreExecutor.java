/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.command.CommandSender
 *  org.bukkit.entity.Player
 */
package ru.bestrax.essentials.executors.chat;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.bestrax.essentials.api.EssentialsApi;
import ru.bestrax.essentials.api.User;
import ru.bestrax.essentials.api.commands.TrustCommand;

public class IgnoreExecutor
extends TrustCommand {
    public IgnoreExecutor() {
        super("ignore", "unignore", "ignores");
        this.setPermission("trustessentials.ignore");
        this.setPlayersOnly(true);
        this.register();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player)sender;
        if (args.length != 1) {
            this.sendMessages(sender, "IGNORE_USAGE", new String[0]);
            return;
        }
        User user = EssentialsApi.getUserManager().getUser(player);
        if (user == null) {
            return;
        }
        String targetName = args[0];
        Player target = Bukkit.getPlayer((String)targetName);
        if (target == null || !target.isOnline()) {
            this.sendMessage(sender, "PLAYER_NOT_FOUND", "%name%", targetName);
            return;
        }
        if (user.getIgnores().contains(target.getName())) {
            user.getIgnores().remove(target.getName());
        } else {
            user.getIgnores().add(target.getName());
        }
        if (user.getIgnores().contains(target.getName())) {
            this.sendMessage(sender, "IGNORE_ADD", "%name%", target.getName());
        } else {
            this.sendMessage(sender, "IGNORE_REMOVE", "%name%", target.getName());
        }
    }
}
