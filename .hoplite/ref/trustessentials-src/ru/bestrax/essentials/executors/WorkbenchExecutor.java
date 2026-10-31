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

public class WorkbenchExecutor
extends TrustCommand {
    public WorkbenchExecutor() {
        super("workbench", "wb", "craft");
        this.setPlayersOnly(true);
        this.setPermission("trustessentials.workbench");
        this.register();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player)sender;
        player.openWorkbench(null, true);
    }
}
