/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.command.CommandSender
 */
package ru.bestrax.essentials.executors.broadcasts;

import org.bukkit.command.CommandSender;
import ru.bestrax.essentials.executors.broadcasts.BroadCastExecutor;

public class StreamBroadCastExecutor
extends BroadCastExecutor {
    public StreamBroadCastExecutor() {
        super("stream", "\u0441\u0442\u0440\u0438\u043c");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length > 1 || args.length == 1 && !args[0].contains("youtu.be") && !args[0].contains("youtube.com")) {
            this.sendMessage(sender, "UNCORRECT_LINK", new String[0]);
            return;
        }
        this.send(sender, "STREAM", args);
    }
}
