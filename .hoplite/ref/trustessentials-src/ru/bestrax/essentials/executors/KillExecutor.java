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
import ru.bestrax.essentials.api.commands.TrustCommand;
import ru.bestrax.essentials.utils.BukkitUtils;

public class KillExecutor
extends TrustCommand {
    public KillExecutor() {
        super("kill", new String[0]);
        this.setPermission("trustessentials.kill");
        this.register();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length != 1) {
            this.sendMessages(sender, "KILL_USAGE", new String[0]);
            return;
        }
        String target = args[0];
        Player player = Bukkit.getPlayer((String)target);
        if (player == null || !player.isOnline()) {
            this.sendMessage(sender, "PLAYER_NOT_FOUND", "%name%", target);
            return;
        }
        do {
            player.setHealth(0.0);
        } while (player.getInventory().getItemInMainHand().getType() == BukkitUtils.getTotem() || player.getInventory().getItemInOffHand().getType() == BukkitUtils.getTotem());
        this.sendMessage(sender, "KILL", "%name%", player.getName());
    }
}
