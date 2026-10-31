/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Material
 *  org.bukkit.command.CommandSender
 *  org.bukkit.entity.Player
 *  org.bukkit.inventory.ItemStack
 */
package ru.bestrax.essentials.executors;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ru.bestrax.essentials.api.commands.TrustCommand;

public class MoreExecutor
extends TrustCommand {
    public MoreExecutor() {
        super("more", "dupe");
        this.setPermission("trustessentials.dupe");
        this.setPlayersOnly(true);
        this.register();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player)sender;
        ItemStack inHand = player.getInventory().getItemInMainHand();
        if (inHand == null || inHand.getType() == Material.AIR) {
            this.sendMessage(sender, "DUPE_FOUND", new String[0]);
            return;
        }
        inHand.setAmount(64);
        this.sendMessage((CommandSender)player, "DUPE", new String[0]);
    }
}
