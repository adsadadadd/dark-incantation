/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.command.CommandSender
 *  org.bukkit.entity.Player
 */
package ru.bestrax.essentials.executors.home;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.bestrax.essentials.api.EssentialsApi;
import ru.bestrax.essentials.api.Home;
import ru.bestrax.essentials.api.User;
import ru.bestrax.essentials.api.commands.TrustCommand;
import ru.bestrax.essentials.utils.StringUtils;

public class PlayerHomeExecutor
extends TrustCommand {
    public PlayerHomeExecutor() {
        super("playerhome", "phome");
        this.setPermission("trustessentials.playerhome");
        this.setPlayersOnly(true);
        this.register();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length != 1 && args.length != 2) {
            this.sendMessages(sender, "PLAYERHOME_USAGE", new String[0]);
            return;
        }
        Player player = (Player)sender;
        Player target = Bukkit.getPlayer((String)args[0]);
        if (target == null || !target.isOnline()) {
            this.sendMessage(sender, "PLAYER_NOT_FOUND", "%name%", args[0]);
            return;
        }
        User user = EssentialsApi.getUserManager().getUser(target);
        if (user == null) {
            return;
        }
        if (args.length == 1) {
            String[] homes = user.getHomes().keySet().toArray(new String[0]);
            if (homes.length == 0) {
                this.sendMessage(sender, "PLAYERHOME_SIZE", "%name%", user.getUsername());
            } else {
                this.sendMessage(sender, "PLAYERHOMES", "%name%", user.getUsername(), "%homes%", StringUtils.buildString(homes, ", "));
            }
            return;
        }
        String homeName = args[1];
        if (!user.getHomes().containsKey(homeName)) {
            this.sendMessage(sender, "HOME_NOT_FOUND", "%name%", homeName);
            return;
        }
        Home home = user.getHomes().get(homeName);
        if (EssentialsApi.getUserManager().getUser(player).teleport(home.getLocation())) {
            this.sendMessage(sender, "PLAYERHOME_TO", "%name%", user.getUsername(), "%home%", home.getName());
        }
    }
}
