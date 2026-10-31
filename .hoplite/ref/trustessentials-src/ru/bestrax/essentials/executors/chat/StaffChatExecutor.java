/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.command.CommandSender
 */
package ru.bestrax.essentials.executors.chat;

import org.bukkit.command.CommandSender;
import ru.bestrax.essentials.executors.chat.ChatExecutor;

public class StaffChatExecutor
extends ChatExecutor {
    public StaffChatExecutor() {
        super("staffchat", "sc");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        this.send(sender, "Staff", args);
    }
}
