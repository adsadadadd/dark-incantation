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
import ru.bestrax.essentials.api.manager.SpectateManager;

public class TpHereExecutor
extends TrustCommand {
    public TpHereExecutor() {
        super("tphere", "s");
        this.setPermission("trustessentials.tphere");
        this.setPlayersOnly(true);
        this.register();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player)sender;
        if (args.length != 1) {
            this.sendMessages(sender, "TPHERE_USAGE", new String[0]);
            return;
        }
        Player target = Bukkit.getPlayer((String)args[0]);
        if (target == null || !target.isOnline()) {
            this.sendMessage(sender, "PLAYER_NOT_FOUND", "%name%", args[0]);
            return;
        }
        SpectateManager spectateManager = EssentialsApi.getSpectateManager();
        if (spectateManager.isSpectation(player)) {
            this.sendMessage(sender, "SPECTATE_TPHERE", new String[0]);
            return;
        }
        User user = EssentialsApi.getUserManager().getUser(target);
        user.teleport(player.getLocation());
        this.sendMessage((CommandSender)player, "TPHERE", "%name%", user.getUsername());
    }
}
