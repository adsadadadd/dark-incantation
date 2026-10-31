/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.ChatColor
 *  org.bukkit.Material
 *  org.bukkit.command.CommandSender
 *  org.bukkit.entity.Player
 *  org.bukkit.inventory.ItemStack
 *  org.bukkit.inventory.meta.ItemMeta
 */
package ru.bestrax.essentials.executors;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import ru.bestrax.essentials.api.commands.TrustCommand;
import ru.bestrax.essentials.utils.StringUtils;

public class ItemRenameExecutor
extends TrustCommand {
    public ItemRenameExecutor() {
        super("itemrename", "rename", "name");
        this.setPermission("trustessentials.itemrename");
        this.setPlayersOnly(true);
        this.register();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length < 1) {
            this.sendMessages(sender, "ITEMRENAME_USAGE", new String[0]);
            return;
        }
        Player player = (Player)sender;
        ItemStack inHand = player.getInventory().getItemInMainHand();
        if (inHand == null || inHand.getType() == Material.AIR) {
            this.sendMessage(sender, "ITEMRENAME_FOUND", new String[0]);
            return;
        }
        ItemMeta itemMeta = inHand.getItemMeta();
        String preifx = player.hasPermission("trustessentials.itemrename.admin") ? "" : "&6&l\u041f\u0415\u0420\u0415\u0418\u041c\u0415\u041d\u041e\u0412\u0410\u041d: &f";
        itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes((char)'&', (String)(preifx + StringUtils.buildString(args, " "))));
        inHand.setItemMeta(itemMeta);
    }
}
