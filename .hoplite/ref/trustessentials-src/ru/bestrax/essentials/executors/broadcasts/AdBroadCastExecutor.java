/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.command.CommandSender
 */
package ru.bestrax.essentials.executors.broadcasts;

import org.bukkit.command.CommandSender;
import ru.bestrax.essentials.executors.broadcasts.BroadCastExecutor;

public class AdBroadCastExecutor
extends BroadCastExecutor {
    public AdBroadCastExecutor() {
        super("ad", "advertising");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        this.send(sender, "AD", args);
    }
}
