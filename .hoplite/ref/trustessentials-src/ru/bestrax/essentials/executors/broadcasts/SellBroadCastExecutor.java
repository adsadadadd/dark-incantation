/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.command.CommandSender
 */
package ru.bestrax.essentials.executors.broadcasts;

import org.bukkit.command.CommandSender;
import ru.bestrax.essentials.executors.broadcasts.BroadCastExecutor;

public class SellBroadCastExecutor
extends BroadCastExecutor {
    public SellBroadCastExecutor() {
        super("sell", new String[0]);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        this.send(sender, "SELL", args);
    }
}
