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

public class GodExecutor
extends TrustCommand {
    public GodExecutor() {
        super("god", new String[0]);
        this.setPermission("trustessentials.god");
        this.register();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        User user;
        String target = args.length == 0 ? sender.getName() : args[0];
        Player player = Bukkit.getPlayer((String)target);
        if (player == null || !player.isOnline()) {
            this.sendMessage(sender, "PLAYER_NOT_FOUND", "%name%", target);
            return;
        }
        if (!target.equalsIgnoreCase(sender.getName()) && !sender.hasPermission("trustessentials.god.other")) {
            this.sendMessage(sender, "NO_PERMISSION", "%permission%", "trustessentials.god.other");
            return;
        }
        user.setGod(!(user = EssentialsApi.getUserManager().getUser(player)).isGod(), true);
        if (sender.getName().equalsIgnoreCase(user.getUsername())) {
            return;
        }
        if (user.isGod()) {
            this.sendMessages(sender, "GOD_TARGET_ON", "%name%", player.getDisplayName());
        } else {
            this.sendMessages(sender, "GOD_TARGET_OFF", "%name%", player.getDisplayName());
        }
    }
}
