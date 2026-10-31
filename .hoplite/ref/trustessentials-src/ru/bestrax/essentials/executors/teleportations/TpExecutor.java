/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.command.CommandSender
 *  org.bukkit.entity.Player
 */
package ru.bestrax.essentials.executors.teleportations;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.bestrax.essentials.api.EssentialsApi;
import ru.bestrax.essentials.api.User;
import ru.bestrax.essentials.api.commands.TrustCommand;

public class TpExecutor
extends TrustCommand {
    public TpExecutor() {
        super("tp", new String[0]);
        this.setPermission("trustessentials.tp");
        this.setPlayersOnly(true);
        this.register();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length != 1) {
            this.sendMessages(sender, "TP_USAGE", new String[0]);
            return;
        }
        Player player = (Player)sender;
        Player target = Bukkit.getPlayer((String)args[0]);
        if (target == null || !target.isOnline()) {
            this.sendMessage(sender, "PLAYER_NOT_FOUND", "%name%", args[0]);
            return;
        }
        User user = EssentialsApi.getUserManager().getUser(player);
        User targetUser = EssentialsApi.getUserManager().getUser(target);
        if (!targetUser.isTpToggle() && !player.hasPermission("trustessentials.tptoggle.bypass")) {
            this.sendMessage((CommandSender)player, "TPTOGGLE_BLOCK", "%name%", target.getName());
            return;
        }
        if (user.teleport(target.getLocation())) {
            this.sendMessage((CommandSender)player, "TP", "%name%", target.getName());
        }
    }
}
