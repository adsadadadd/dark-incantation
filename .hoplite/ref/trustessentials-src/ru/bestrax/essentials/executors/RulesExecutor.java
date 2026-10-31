/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.command.CommandSender
 */
package ru.bestrax.essentials.executors;

import org.bukkit.command.CommandSender;
import ru.bestrax.essentials.api.commands.TrustCommand;

public class RulesExecutor
extends TrustCommand {
    public RulesExecutor() {
        super("rules", "rule");
        this.setPermission("trustessentials.rules");
        this.register();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        this.sendMessages(sender, "RULES", new String[0]);
    }
}
