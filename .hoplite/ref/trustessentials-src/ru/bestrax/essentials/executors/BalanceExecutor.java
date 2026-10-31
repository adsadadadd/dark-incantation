/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.command.CommandSender
 *  org.bukkit.entity.Player
 *  org.bukkit.event.EventHandler
 *  org.bukkit.event.Listener
 *  org.bukkit.event.player.PlayerCommandPreprocessEvent
 */
package ru.bestrax.essentials.executors;

import java.util.ArrayList;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import ru.bestrax.essentials.api.PlaceholderAPI;
import ru.bestrax.essentials.api.commands.TrustCommand;

public class BalanceExecutor
extends TrustCommand
implements Listener {
    public BalanceExecutor() {
        super("balance", "bal", "money");
        this.setPermission("trustessentials.balance");
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
        if (!target.equalsIgnoreCase(sender.getName()) && !sender.hasPermission("trustessentials.balance.other")) {
            this.sendMessage(sender, "NO_PERMISSION", "%permission%", "trustessentials.balance.other");
            return;
        }
        ArrayList message = new ArrayList();
        this.getStringList("BALANCE", new String[0]).forEach(x -> message.add(PlaceholderAPI.replacePlaceHolder(player, x)));
        message.forEach(arg_0 -> ((CommandSender)sender).sendMessage(arg_0));
    }

    @EventHandler
    public void onPreCommandEvent(PlayerCommandPreprocessEvent e) {
        Player player = e.getPlayer();
        String command = e.getMessage().replaceFirst("/", "").split(" ")[0].toLowerCase();
        if (command.equalsIgnoreCase("bal") || command.equalsIgnoreCase("money")) {
            e.setCancelled(true);
            ArrayList<String> listArgs = new ArrayList<String>();
            for (int i = 0; i < e.getMessage().split(" ").length; ++i) {
                if (i == 0) continue;
                listArgs.add(e.getMessage().split(" ")[i]);
            }
            this.execute((CommandSender)player, listArgs.toArray(new String[0]));
        }
    }
}
