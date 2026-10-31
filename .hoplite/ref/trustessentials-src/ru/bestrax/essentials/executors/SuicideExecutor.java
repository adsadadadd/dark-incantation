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

public class SuicideExecutor
extends TrustCommand {
    public SuicideExecutor() {
        super("suicide", new String[0]);
        this.setPermission("trustessentials.suicide");
        this.register();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player)sender;
        do {
            player.setHealth(0.0);
        } while (player.getInventory().getItemInMainHand().getType() == BukkitUtils.getTotem() || player.getInventory().getItemInOffHand().getType() == BukkitUtils.getTotem());
        this.sendMessage((CommandSender)player, "SUICIDE", new String[0]);
    }
}
