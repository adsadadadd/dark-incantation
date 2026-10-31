/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.lang.StringUtils
 *  org.bukkit.Material
 *  org.bukkit.command.CommandSender
 *  org.bukkit.enchantments.Enchantment
 *  org.bukkit.entity.Player
 *  org.bukkit.inventory.ItemStack
 */
package ru.bestrax.essentials.executors;

import java.util.Locale;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ru.bestrax.essentials.api.commands.TrustCommand;

public class EnchantExecutor
extends TrustCommand {
    public EnchantExecutor() {
        super("enchant", new String[0]);
        this.setPermission("trustessentials.enchant");
        this.setPlayersOnly(true);
        this.register();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player)sender;
        if (args.length != 2) {
            this.sendMessages(sender, "ENCHANT_USAGE", new String[0]);
            return;
        }
        if (!StringUtils.isNumeric((String)args[1])) {
            this.sendMessage(sender, "NUMBER_ONLY", "%replaced%", args[1]);
            return;
        }
        int level = Integer.parseInt(args[1]);
        Enchantment enchantment = Enchantment.getByName((String)args[0].toUpperCase(Locale.ROOT));
        if (enchantment == null) {
            this.sendMessage(sender, "ENCHANT_FOUND", "%name%", args[0]);
            return;
        }
        ItemStack itemStack = player.getInventory().getItemInMainHand();
        if (itemStack == null || itemStack.getType() == Material.AIR) {
            this.sendMessage(sender, "ENCHANT_HAND_FOUND", new String[0]);
            return;
        }
        itemStack.addUnsafeEnchantment(enchantment, level);
        this.sendMessage(sender, "ENCHANT", "%item%", itemStack.getType().toString(), "%level%", args[1], "%ench%", enchantment.getName());
    }
}
