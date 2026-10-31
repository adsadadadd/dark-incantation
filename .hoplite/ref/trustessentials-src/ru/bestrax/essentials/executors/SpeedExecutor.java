/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.lang.StringUtils
 *  org.bukkit.Bukkit
 *  org.bukkit.command.CommandSender
 *  org.bukkit.entity.Player
 */
package ru.bestrax.essentials.executors;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.bestrax.essentials.api.commands.TrustCommand;

public class SpeedExecutor
extends TrustCommand {
    public SpeedExecutor() {
        super("speed", new String[0]);
        this.setPermission("trustessentials.speed");
        this.register();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length != 1 && args.length != 2) {
            this.sendMessages(sender, "SPEED_USAGE", new String[0]);
            return;
        }
        String target = args.length == 1 ? sender.getName() : args[0];
        String level = args.length == 1 ? args[0] : args[1];
        Player player = Bukkit.getPlayer((String)target);
        if (player == null || !player.isOnline()) {
            this.sendMessage(sender, "PLAYER_NOT_FOUND", "%name%", target);
            return;
        }
        if (!target.equalsIgnoreCase(sender.getName()) && !sender.hasPermission("trustessentials.speed.other")) {
            this.sendMessage(sender, "NO_PERMISSION", "%permission%", "trustessentials.speed.other");
            return;
        }
        if (!StringUtils.isNumeric((String)level)) {
            this.sendMessage(sender, "NUMBER_ONLY", "%replaced%", level);
            return;
        }
        int iLevel = Integer.parseInt(level);
        if (iLevel > 10 || iLevel <= 0) {
            iLevel = 10;
        }
        player.setWalkSpeed(0.2f + (float)(Integer.parseInt(String.valueOf(iLevel)) - 1) * 0.8f / 9.0f);
        this.sendMessages((CommandSender)player, "SPEED", "%level%", String.valueOf(iLevel));
    }
}
