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

public class HatExecutor
extends TrustCommand {
    public HatExecutor() {
        super("hat", new String[0]);
        this.setPermission("trustessentials.hat");
        this.setPlayersOnly(true);
        this.register();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player)sender;
        ItemStack curHelmet = player.getInventory().getHelmet();
        if (curHelmet != null && curHelmet.getType() != Material.AIR) {
            this.sendMessage(sender, "HAT_ALREADY", "%helmet%", curHelmet.getType().toString());
            return;
        }
        ItemStack inHand = player.getInventory().getItemInMainHand();
        if (inHand == null || inHand.getType() == Material.AIR) {
            this.sendMessage(sender, "HAT_FOUND", new String[0]);
            return;
        }
        this.sendMessage(sender, "HAT", "%name%", inHand.getType().toString());
        player.getInventory().setHelmet(inHand.clone());
        inHand.setAmount(0);
    }
}
