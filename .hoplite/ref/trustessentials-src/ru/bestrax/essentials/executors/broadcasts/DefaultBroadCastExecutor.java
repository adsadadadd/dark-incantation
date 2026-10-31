/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.command.CommandSender
 */
package ru.bestrax.essentials.executors.broadcasts;

import org.bukkit.command.CommandSender;
import ru.bestrax.essentials.executors.broadcasts.BroadCastExecutor;

public class DefaultBroadCastExecutor
extends BroadCastExecutor {
    public DefaultBroadCastExecutor() {
        super("broadcast", "bc", "say");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        this.send(sender, "BROADCAST", args);
    }
}
