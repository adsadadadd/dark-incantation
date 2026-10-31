/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Material
 *  org.bukkit.OfflinePlayer
 *  org.bukkit.command.CommandSender
 *  org.bukkit.entity.Player
 *  org.bukkit.inventory.ItemStack
 */
package ru.bestrax.essentials.executors;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ru.bestrax.essentials.api.EssentialsApi;
import ru.bestrax.essentials.api.User;
import ru.bestrax.essentials.api.commands.TrustCommand;
import ru.bestrax.essentials.api.config.Config;
import ru.bestrax.essentials.api.events.UserRepairItemEvent;
import ru.bestrax.essentials.api.manager.MoneyManager;
import ru.bestrax.essentials.utils.BukkitUtils;
import ru.bestrax.essentials.utils.StringUtils;

public class RepairExecutor
extends TrustCommand {
    public RepairExecutor() {
        super("repair", "fix");
        this.setPermission("trustessentials.repair");
        this.setPlayersOnly(true);
        this.register();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player)sender;
        if (args.length != 1 && args.length != 0) {
            this.sendMessages(sender, "REPAIR_USAGE", new String[0]);
            return;
        }
        User user = EssentialsApi.getUserManager().getUser(player);
        MoneyManager moneyManager = EssentialsApi.getMoneyManager();
        Config settings = EssentialsApi.getInstance().getSettings();
        if (args.length == 1 && args[0].equalsIgnoreCase("all")) {
            if (!player.hasPermission("trustessentials.repair.all")) {
                this.sendMessage((CommandSender)player, "NO_PERMISSION", "%permission%", "trustessentials.repair.all");
                return;
            }
            if (settings.getBoolean("repairBuy") && moneyManager.get((OfflinePlayer)player) < (double)settings.getInt("repairAllCost")) {
                this.sendMessage((CommandSender)player, "REPAIR_ALL_ENOUGH", new String[0]);
                return;
            }
            String[] repaired = this.repairAll(user).toArray(new String[0]);
            if (repaired.length > 0) {
                String message = StringUtils.buildString(repaired, ", ");
                this.sendMessage((CommandSender)player, "REPAIR", "%items%", message);
                moneyManager.withdraw((OfflinePlayer)player, (double)settings.getInt("repairAllCost"));
            } else {
                this.sendMessage((CommandSender)player, "REPAIR_NON", new String[0]);
            }
        } else if (args.length == 0) {
            if (settings.getBoolean("repairBuy") && moneyManager.get((OfflinePlayer)player) < (double)settings.getInt("repairCost")) {
                this.sendMessage((CommandSender)player, "REPAIR_ENOUGH", new String[0]);
                return;
            }
            try {
                String name = this.repairItem(user, player.getInventory().getItemInMainHand());
                this.sendMessage((CommandSender)player, "REPAIR", "%items%", name);
                moneyManager.withdraw((OfflinePlayer)player, (double)settings.getInt("repairCost"));
            }
            catch (Exception e) {
                this.sendMessage((CommandSender)player, "REPAIR_INVALID", new String[0]);
            }
        }
    }

    private String repairItem(User user, ItemStack item) {
        UserRepairItemEvent repairItemEvent = new UserRepairItemEvent(user, item);
        BukkitUtils.callEvent(repairItemEvent, EssentialsApi.getInstance());
        if (repairItemEvent.isCancelled()) {
            return "\u041e\u0448\u0438\u0431\u043a\u0430";
        }
        Material material = item.getType();
        if (material.isBlock() || material.getMaxDurability() < 1) {
            throw new UnsupportedOperationException("repairInvalidType");
        }
        item.setDurability((short)0);
        return item.getType().toString().toLowerCase(Locale.ENGLISH);
    }

    public List<String> repairAll(User user) {
        ArrayList<String> repaired = new ArrayList<String>();
        this.repairItems(user, user.getPlayer().getInventory().getContents(), repaired);
        this.repairItems(user, user.getPlayer().getInventory().getArmorContents(), repaired);
        user.getPlayer().updateInventory();
        return repaired;
    }

    private void repairItems(User user, ItemStack[] items, List<String> repaired) {
        for (ItemStack item : items) {
            if (item == null || item.getType().isBlock() || item.getDurability() == 0) continue;
            String itemName = item.getType().toString().toLowerCase(Locale.ENGLISH);
            try {
                this.repairItem(user, item);
            }
            catch (Exception e) {
                continue;
            }
            repaired.add(itemName.replace('_', ' '));
        }
    }
}
