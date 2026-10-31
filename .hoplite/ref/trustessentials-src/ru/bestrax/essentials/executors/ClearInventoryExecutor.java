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
import ru.bestrax.essentials.api.EssentialsApi;
import ru.bestrax.essentials.api.User;
import ru.bestrax.essentials.api.commands.TrustCommand;

public class ClearInventoryExecutor
extends TrustCommand {
    public ClearInventoryExecutor() {
        super("clearinventory", "ci", "clear-inventory", "clear");
        this.setPermission("trustessentials.clearinventory");
        this.register();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        String target = args.length == 0 ? sender.getName() : args[0];
        Player player = Bukkit.getPlayer((String)target);
        if (player == null || !player.isOnline()) {
            this.sendMessage(sender, "PLAYER_NOT_FOUND", "%name%", target);
            return;
        }
        if (!target.equalsIgnoreCase(sender.getName()) && !sender.hasPermission("trustessentials.clearinventory.other")) {
            this.sendMessage(sender, "NO_PERMISSION", "%permission%", "trustessentials.clearinventory.other");
            return;
        }
        User user = EssentialsApi.getUserManager().getUser(player);
        user.clearInventory(true);
        if (sender.getName().equalsIgnoreCase(user.getUsername())) {
            return;
        }
        this.sendMessage(sender, "CLEAR_INVENTORY_TARGET", "%name%", player.getDisplayName());
    }
}
