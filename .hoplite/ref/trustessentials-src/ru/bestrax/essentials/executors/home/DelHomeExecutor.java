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
import ru.bestrax.essentials.api.commands.TrustCommand;

public class DelHomeExecutor
extends TrustCommand {
    public DelHomeExecutor() {
        super("delhome", "removehome", "deletehome");
        this.setPermission("trustessentials.delhome");
        this.setPlayersOnly(true);
        this.register();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player)sender;
        User user = EssentialsApi.getUserManager().getUser(player);
        if (user != null) {
            if (args.length < 1) {
                this.sendMessages(sender, "DELHOME_USAGE", new String[0]);
            } else {
                String nameHome = args[0].toLowerCase();
                if (user.getHomes().containsKey(nameHome)) {
                    this.sendMessage(sender, "HOME_DELETE", "%name%", nameHome);
                    user.removeHome(nameHome);
                } else {
                    this.sendMessage(sender, "HOME_NOT_FOUND", "%name%", nameHome);
                }
            }
        }
    }
}
