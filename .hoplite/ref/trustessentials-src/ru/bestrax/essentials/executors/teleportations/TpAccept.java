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
import ru.bestrax.essentials.api.manager.SpectateManager;
import ru.bestrax.essentials.base.objects.TrustUser;
import ru.bestrax.essentials.utils.LocationUtils;

public class TpAccept
extends TrustCommand {
    public TpAccept() {
        super("tpaccept", "tpyes", "tpallow");
        this.setPermission("trustessentials.tpaccept");
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
                this.accept((Player)sender, player);
            }
            return;
        }
        if (playerRequest.size() == 0) {
            this.sendMessage(sender, "TPACCEPT_ERROR", new String[0]);
            return;
        }
        if (args.length == 0) {
            this.sendMessages(sender, "TPACCEPT_USAGE", new String[0]);
            return;
        }
        String name = args[0];
        player = Bukkit.getPlayer((String)name);
        if (player == null || !player.isOnline()) {
            this.sendMessage(sender, "PLAYER_NOT_FOUND", "%name%", name);
            return;
        }
        if (!playerRequest.containsKey(name)) {
            this.sendMessage(sender, "TPACCEPT_ERROR_PLAYER", "%name%", name);
            return;
        }
        this.accept((Player)sender, player);
    }

    private void accept(Player sender, Player target) {
        SpectateManager spectateManager = EssentialsApi.getSpectateManager();
        if (spectateManager.isSpectation(sender)) {
            this.sendMessage((CommandSender)sender, "SPECTATE_TPA", new String[0]);
            return;
        }
        User userWho = EssentialsApi.getUserManager().getUser(target);
        TrustUser senderUser = (TrustUser)EssentialsApi.getUserManager().getUser(sender);
        this.sendMessage((CommandSender)sender, "TPACCEPT_SENDER", "%name%", target.getDisplayName());
        this.sendMessage((CommandSender)target, "TPACCEPT_WHO", "%name%", sender.getDisplayName());
        senderUser.getCallRequests().remove(target.getName());
        ((TrustUser)userWho).addWaitTeleport(LocationUtils.stringFromLocation(sender.getLocation(), true));
    }
}
