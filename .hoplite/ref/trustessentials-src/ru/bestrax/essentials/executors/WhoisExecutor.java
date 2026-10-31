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
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.bestrax.essentials.api.PlaceholderAPI;
import ru.bestrax.essentials.api.commands.TrustCommand;

public class WhoisExecutor
extends TrustCommand {
    public WhoisExecutor() {
        super("whois", new String[0]);
        this.setPermission("trustessentials.whois");
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
        if (!target.equalsIgnoreCase(sender.getName()) && !sender.hasPermission("trustessentials.whois.other")) {
            this.sendMessage(sender, "NO_PERMISSION", "%permission%", "trustessentials.whois.other");
            return;
        }
        ArrayList message = new ArrayList();
        this.getStringList("WHOIS", new String[0]).forEach(x -> message.add(PlaceholderAPI.replacePlaceHolder(player, x)));
        message.forEach(arg_0 -> ((CommandSender)sender).sendMessage(arg_0));
    }
}
