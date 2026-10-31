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

public class BurnExecutor
extends TrustCommand {
    public BurnExecutor() {
        super("burn", new String[0]);
        this.setPermission("trustessentials.burn");
        this.register();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length != 1) {
            this.sendMessages(sender, "BURN_USAGE", new String[0]);
            return;
        }
        Player target = Bukkit.getPlayer((String)args[0]);
        if (target == null || !target.isOnline()) {
            this.sendMessage(sender, "PLAYER_NOT_FOUND", "%name%", args[0]);
            return;
        }
        target.setFireTicks(200);
        this.sendMessage(sender, "BURN", "%name%", target.getDisplayName());
    }
}
