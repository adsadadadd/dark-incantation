/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.command.CommandSender
 *  org.bukkit.entity.Player
 */
package ru.bestrax.essentials.executors.teleportations;

import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.bestrax.essentials.api.EssentialsApi;
import ru.bestrax.essentials.api.User;
import ru.bestrax.essentials.api.commands.TrustCommand;
import ru.bestrax.essentials.base.objects.TrustUser;

public class TpCancelExecutor
extends TrustCommand {
    public TpCancelExecutor() {
        super("tpcancel", "tpdeny", "tpno", "tpacancel");
        this.setPermission("trustessentials.tpcancel");
        this.setPlayersOnly(true);
        this.register();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        User user = EssentialsApi.getUserManager().getUser(sender.getName());
        if (user == null) {
            return;
        }
        Map<String, Long> playerRequest = user.getCallRequests();
        Player player = null;
        for (String name : playerRequest.keySet()) {
            player = Bukkit.getPlayer((String)name);
            if (player != null && player.isOnline()) continue;
            playerRequest.remove(name);
        }
        if (playerRequest.size() == 1) {
            if (player != null) {
                this.deny((Player)sender, player);
            }
            return;
        }
        if (playerRequest.size() == 0) {
            this.sendMessage(sender, "TPCANCEL_ERROR", new String[0]);
            return;
        }
        if (args.length == 0) {
            this.sendMessages(sender, "TPCANCEL_USAGE", new String[0]);
            return;
        }
        String name = args[0];
        player = Bukkit.getPlayer((String)name);
        if (player == null || !player.isOnline()) {
            this.sendMessage(sender, "PLAYER_NOT_FOUND", "%name%", name);
            return;
        }
        if (!playerRequest.containsKey(name)) {
            this.sendMessage(sender, "TPCANCEL_ERROR_PLAYER", "%name%", name);
            return;
        }
        this.deny((Player)sender, player);
    }

    public void deny(Player sender, Player target) {
        User user = EssentialsApi.getUserManager().getUser(sender);
        if (user != null) {
            ((TrustUser)user).getCallRequests().remove(target.getName());
            this.sendMessage((CommandSender)sender, "TPCANCEL_SENDER", "%name%", target.getDisplayName());
            this.sendMessage((CommandSender)target, "TPCANCEL_WHO", "%name%", sender.getDisplayName());
        }
    }
}
