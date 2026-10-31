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
import ru.bestrax.essentials.gui.InvseeGui;

public class InvSeeExecutor
extends TrustCommand {
    public InvSeeExecutor() {
        super("invsee", new String[0]);
        this.setPermission("trustessentials.invsee");
        this.setPlayersOnly(true);
        this.register();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player)sender;
        if (args.length != 1) {
            this.sendMessages(sender, "INVSEE_USAGE", new String[0]);
            return;
        }
        Player target = Bukkit.getPlayer((String)args[0]);
        if (target == null || !target.isOnline()) {
            this.sendMessage(sender, "PLAYER_NOT_FOUND", "%name%", args[0]);
            return;
        }
        if (target.getName().equalsIgnoreCase(player.getName())) {
            this.sendMessage(sender, "INVSEE_YOU", new String[0]);
            return;
        }
        new InvseeGui(player, target).open();
    }
}
