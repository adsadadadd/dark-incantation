/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.command.CommandSender
 */
package ru.bestrax.essentials.executors;

import org.bukkit.command.CommandSender;
import ru.bestrax.essentials.api.commands.TrustCommand;

public class VkExecutor
extends TrustCommand {
    public VkExecutor() {
        super("vk", new String[0]);
        this.register();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        this.sendMessages(sender, "VK", new String[0]);
    }
}
