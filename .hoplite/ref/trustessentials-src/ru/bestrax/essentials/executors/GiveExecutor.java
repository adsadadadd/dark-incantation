/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.lang.math.NumberUtils
 *  org.bukkit.Bukkit
 *  org.bukkit.Material
 *  org.bukkit.command.CommandSender
 *  org.bukkit.entity.Player
 *  org.bukkit.inventory.ItemStack
 *  org.bukkit.plugin.Plugin
 */
package ru.bestrax.essentials.executors;

import java.util.Locale;
import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import ru.bestrax.essentials.api.EssentialsApi;
import ru.bestrax.essentials.api.MetaItemStack;
import ru.bestrax.essentials.api.commands.TrustCommand;
import ru.bestrax.essentials.utils.ItemBuilder;

public class GiveExecutor
extends TrustCommand {
    public GiveExecutor() {
        super("give", new String[0]);
        this.setPermission("trustessentials.give");
        this.register();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length < 2) {
            this.sendMessages(sender, "GIVE_USAGE", new String[0]);
            return;
        }
        ItemStack stack = null;
        try {
            stack = ItemBuilder.builder(args[1]).build();
        }
        catch (Exception e) {
            this.sendMessage(sender, "GIVE_MATERIAL", new String[0]);
            return;
        }
        String itemname = stack.getType().toString().toLowerCase(Locale.ENGLISH).replace("_", "");
        Player giveTo = Bukkit.getPlayer((String)args[0]);
        try {
            if (args.length > 3 && NumberUtils.isNumber((String)args[2]) && NumberUtils.isNumber((String)args[3])) {
                stack.setAmount(Integer.parseInt(args[2]));
                stack.setDurability(Short.parseShort(args[3]));
            } else if (args.length > 2 && Integer.parseInt(args[2]) > 0) {
                stack.setAmount(Integer.parseInt(args[2]));
            } else {
                stack.setAmount(64);
            }
        }
        catch (NumberFormatException e) {
            this.sendMessage(sender, "GIVE_NUMBER", new String[0]);
            return;
        }
        MetaItemStack metaStack = new MetaItemStack(stack);
        if (args.length > 3) {
            int metaStart;
            boolean allowUnsafe = true;
            if (allowUnsafe) {
                allowUnsafe = false;
            }
            int n = metaStart = NumberUtils.isNumber((String)args[3]) ? 4 : 3;
            if (args.length > metaStart) {
                try {
                    metaStack.parseStringMeta(sender, allowUnsafe, args, metaStart, (Plugin)EssentialsApi.getInstance());
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
            stack = metaStack.getItemStack();
        }
        if (stack.getType() == Material.AIR) {
            this.sendMessage(sender, "GIVE_SPAWN", new String[0]);
            return;
        }
        String itemName = stack.getType().toString().toLowerCase(Locale.ENGLISH).replace('_', ' ');
        this.sendMessage(sender, "GIVE_GIVE", "%amount%", String.valueOf(stack.getAmount()), "%itemName%", itemName);
        giveTo.getInventory().addItem(new ItemStack[]{stack});
        giveTo.updateInventory();
    }
}
