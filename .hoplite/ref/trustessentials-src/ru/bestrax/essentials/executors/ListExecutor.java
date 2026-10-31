/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.command.CommandSender
 *  org.bukkit.entity.Player
 */
package ru.bestrax.essentials.executors;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.bestrax.essentials.api.EssentialsApi;
import ru.bestrax.essentials.api.commands.TrustCommand;

public class ListExecutor
extends TrustCommand {
    public ListExecutor() {
        super("list", "online", "players");
        this.setPermission("trustessentials.list");
        this.register();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        List<Object> botManager = EssentialsApi.getBotManager() == null ? new ArrayList() : EssentialsApi.getBotManager().getUsedAccounts();
        int admins = 0;
        int defaults = 0 + botManager.size();
        int donates = 0;
        int all = 0 + botManager.size();
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (onlinePlayer.hasPermission("trustessentials.admin")) {
                ++admins;
            } else if (onlinePlayer.hasPermission("trustessentials.donate")) {
                ++donates;
            } else {
                ++defaults;
            }
            ++all;
        }
        this.sendMessages(sender, "LIST", "%admins%", String.valueOf(admins), "%defaults%", String.valueOf(defaults), "%donates%", String.valueOf(donates), "%all%", String.valueOf(all));
    }
}
