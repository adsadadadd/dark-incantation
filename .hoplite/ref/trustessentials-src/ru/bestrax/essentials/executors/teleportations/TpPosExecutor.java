/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.lang.StringUtils
 *  org.bukkit.Bukkit
 *  org.bukkit.Location
 *  org.bukkit.World
 *  org.bukkit.command.CommandSender
 *  org.bukkit.entity.Player
 */
package ru.bestrax.essentials.executors.teleportations;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.bestrax.essentials.api.EssentialsApi;
import ru.bestrax.essentials.api.User;
import ru.bestrax.essentials.api.commands.TrustCommand;

public class TpPosExecutor
extends TrustCommand {
    public TpPosExecutor() {
        super("tppos", "tpcord", "pos");
        this.setPermission("trustessentials.tppos");
        this.setPlayersOnly(true);
        this.register();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player target;
        Player player = (Player)sender;
        if (args.length != 3 && args.length != 4 && args.length != 5) {
            this.sendMessages(sender, "TPPOS_USAGE", new String[0]);
            return;
        }
        Player player2 = target = args.length == 5 ? Bukkit.getPlayer((String)args[0]) : player;
        if (target == null || !target.isOnline()) {
            this.sendMessage((CommandSender)player, "PLAYER_NOT_FOUND", "%name%", args[0]);
            return;
        }
        if (!target.getName().equalsIgnoreCase(player.getName()) && !player.hasPermission("trustessentials.tppos.other")) {
            this.sendMessage((CommandSender)player, "NO_PERMISSION", "%permission%", "trustessentials.tppos.other");
            return;
        }
        String xCord = args[args.length == 5 ? 1 : 0];
        String yCord = args[args.length == 5 ? 2 : 1];
        String zCord = args[args.length == 5 ? 3 : 2];
        if (!StringUtils.isNumeric((String)xCord)) {
            this.sendMessage((CommandSender)player, "NUMBER_ONLY", "%replaced%", xCord);
        } else if (!StringUtils.isNumeric((String)yCord)) {
            this.sendMessage((CommandSender)player, "NUMBER_ONLY", "%replaced%", yCord);
        } else if (!StringUtils.isNumeric((String)zCord)) {
            this.sendMessage((CommandSender)player, "NUMBER_ONLY", "%replaced%", zCord);
        }
        String world = args.length == 3 ? player.getWorld().getName() : (args.length == 5 ? args[4] : args[3]);
        World bukkitWorld = Bukkit.getWorld((String)world);
        if (bukkitWorld == null) {
            this.sendMessage((CommandSender)player, "TPPOS_WORLD", "%name%", world);
            return;
        }
        User user = EssentialsApi.getUserManager().getUser(target);
        user.teleport(new Location(bukkitWorld, (double)Integer.parseInt(xCord), (double)Integer.parseInt(yCord), (double)Integer.parseInt(zCord)));
        if (target.getName().equalsIgnoreCase(player.getName())) {
            this.sendMessage(sender, "TPPOS", "%world%", world, "%x%", xCord, "%y%", yCord, "%z%", zCord);
        } else {
            this.sendMessage(sender, "TPPOS_TARGET", "%name%", target.getName(), "%world%", world, "%x%", xCord, "%y%", yCord, "%z%", zCord);
        }
    }
}
