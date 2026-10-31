/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.command.CommandSender
 *  org.bukkit.entity.Player
 */
package ru.bestrax.essentials.executors;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.bestrax.essentials.api.commands.TrustCommand;
import ru.bestrax.essentials.utils.NMSUtils;

public class PingExecutor
extends TrustCommand {
    public PingExecutor() {
        super("ping", new String[0]);
        this.setPermission("trustessentials.ping");
        this.setPlayersOnly(true);
        this.register();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player)sender;
        int ping = NMSUtils.getPing(player);
        this.sendMessage((CommandSender)player, "PING", "%ping%", String.valueOf(ping));
    }
}
