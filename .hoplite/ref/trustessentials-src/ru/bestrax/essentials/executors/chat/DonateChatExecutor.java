/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.command.CommandSender
 */
package ru.bestrax.essentials.executors.chat;

import org.bukkit.command.CommandSender;
import ru.bestrax.essentials.executors.chat.ChatExecutor;

public class DonateChatExecutor
extends ChatExecutor {
    public DonateChatExecutor() {
        super("donatechat", "dc", "d");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        this.send(sender, "Donate", args);
    }
}
