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
import ru.bestrax.essentials.api.commands.TrustCommand;
import ru.bestrax.essentials.api.manager.SpectateManager;
import ru.bestrax.essentials.utils.MessagesUtils;

public class SpectateExecutor
extends TrustCommand {
    public SpectateExecutor() {
        super("spectate", "spec", "spectation");
        this.setPermission("trustessentials.spectation");
        this.setPlayersOnly(true);
        this.register();
    }

    public static void executeCmd(CommandSender sender, String[] args) {
        if (args.length != 1) {
            MessagesUtils.sendMessages(sender, EssentialsApi.getInstance().getLang().getStringList("SPECTATE_USAGE", new String[0]));
            return;
        }
        Player player = (Player)sender;
        Player target = Bukkit.getPlayer((String)args[0]);
        SpectateManager spectateManager = EssentialsApi.getSpectateManager();
        if (target == null || !target.isOnline()) {
            sender.sendMessage(EssentialsApi.getInstance().getLang().getString("PLAYER_NOT_FOUND", "%name%", args[0]));
            return;
        }
        if (target.getName().equals(player.getName())) {
            sender.sendMessage(EssentialsApi.getInstance().getLang().getString("SPECTATE_YOU", new String[0]));
            return;
        }
        if (spectateManager.isSpectation(player)) {
            spectateManager.stop(player);
            return;
        }
        spectateManager.start(player, target);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        SpectateExecutor.executeCmd(sender, args);
    }
}
