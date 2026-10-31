/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.command.CommandSender
 *  org.bukkit.entity.Player
 *  org.bukkit.inventory.ItemStack
 */
package ru.bestrax.essentials.executors;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ru.bestrax.essentials.api.commands.TrustCommand;
import ru.bestrax.essentials.utils.HeadUtils;

public class HeadExecutor
extends TrustCommand {
    public HeadExecutor() {
        super("head", "skull");
        this.setPermission("trustessentials.head");
        this.setPlayersOnly(true);
        this.register();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player)sender;
        String targetstr = args.length == 0 ? player.getName() : args[0];
        Player target = Bukkit.getPlayer((String)targetstr);
        if (target == null || !target.isOnline()) {
            this.sendMessage(sender, "PLAYER_NOT_FOUND", "%name%", targetstr);
            return;
        }
        if (target.getName().equals(player.getName())) {
            this.sendMessage(sender, "HEAD", new String[0]);
        } else {
            this.sendMessage(sender, "HEAD_TARGET", "%name%", target.getName());
        }
        player.getInventory().addItem(new ItemStack[]{HeadUtils.getHead(target.getName())});
    }
}
