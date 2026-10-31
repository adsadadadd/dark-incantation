/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.command.CommandSender
 *  org.bukkit.entity.Player
 */
package ru.bestrax.essentials.executors.home;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.bestrax.essentials.api.EssentialsApi;
import ru.bestrax.essentials.api.User;
import ru.bestrax.essentials.api.VaultAPI;
import ru.bestrax.essentials.api.commands.TrustCommand;
import ru.bestrax.essentials.api.manager.SpectateManager;

public class CreateHomeExecutor
extends TrustCommand {
    public CreateHomeExecutor() {
        super("createhome", "sethome");
        this.setPermission("trustessentials.createhome");
        this.setPlayersOnly(true);
        this.register();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player)sender;
        User user = EssentialsApi.getUserManager().getUser(player);
        if (user != null) {
            if (args.length < 1) {
                this.sendMessages(sender, "SETHOME_USAGE", new String[0]);
            } else {
                int homes = user.getHomes().size();
                String group = VaultAPI.getPrimaryGroup(player);
                int limit = EssentialsApi.getHomeManager().getHomeLimit(group);
                if (homes == limit) {
                    this.sendMessage(sender, "HOME_LIMIT", "%limit%", String.valueOf(limit));
                } else {
                    SpectateManager spectateManager = EssentialsApi.getSpectateManager();
                    if (spectateManager.isSpectation(player)) {
                        this.sendMessage(sender, "SPECTATE_HOME", new String[0]);
                        return;
                    }
                    String nameHome = args[0].toLowerCase();
                    if (user.getHomes().containsKey(nameHome)) {
                        this.sendMessage(sender, "HOME_ERROR", "%name%", nameHome);
                    } else {
                        this.sendMessage(sender, "HOME_CREATE", "%name%", nameHome);
                        user.addHome(nameHome, player.getLocation());
                    }
                }
            }
        }
    }
}
