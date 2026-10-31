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

public class TpAll
extends TrustCommand {
    public TpAll() {
        super("tpall", new String[0]);
        this.setPermission("trustessentials.tpall");
        this.setPlayersOnly(true);
        this.register();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player)sender;
        SpectateManager spectateManager = EssentialsApi.getSpectateManager();
        if (spectateManager.isSpectation(player)) {
            this.sendMessage(sender, "SPECTATE_TPHERE", new String[0]);
            return;
        }
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            User user = EssentialsApi.getUserManager().getUser(onlinePlayer);
            user.teleport(player.getLocation());
        }
        this.sendMessage(sender, "TPALL", new String[0]);
    }
}
