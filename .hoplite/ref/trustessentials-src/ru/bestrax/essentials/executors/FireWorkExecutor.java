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
import ru.bestrax.essentials.utils.BukkitUtils;

public class FireWorkExecutor
extends TrustCommand {
    public FireWorkExecutor() {
        super("firework", new String[0]);
        this.setPermission("trustessentials.firework");
        this.setPlayersOnly(true);
        this.register();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player)sender;
        BukkitUtils.spawnFireworks(player.getLocation(), 3);
        this.sendMessage((CommandSender)player, "FIREWORK", new String[0]);
    }
}
