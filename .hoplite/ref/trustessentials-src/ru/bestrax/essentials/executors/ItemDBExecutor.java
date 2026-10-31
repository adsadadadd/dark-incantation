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
import ru.bestrax.essentials.api.EssentialsApi;
import ru.bestrax.essentials.api.commands.TrustCommand;

public class ItemDBExecutor
extends TrustCommand {
    public ItemDBExecutor() {
        super("itemdb", "dura", "db");
        this.setPermission("trustessentials.itemdb");
        this.setPlayersOnly(true);
        this.register();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player)sender;
        ItemStack inHand = player.getInventory().getItemInMainHand();
        if (inHand == null || inHand.getType() == Material.AIR) {
            this.sendMessage(sender, "ITEMDB_FOUND", new String[0]);
            return;
        }
        double version = Double.parseDouble(EssentialsApi.getInstance().getFormattedVersion());
        if (version >= 1.13) {
            this.sendMessage(sender, "ITEMDB", "%type%", inHand.getType().toString(), "%id%", "\u00a7c\u0430\u0439\u0434\u0438 \u043d\u0435\u0434\u043e\u0441\u0442\u0443\u043f\u0435\u043d");
        } else {
            this.sendMessage(sender, "ITEMDB", "%type%", inHand.getType().toString(), "%id%", String.valueOf(inHand.getType().getId()));
        }
    }
}
