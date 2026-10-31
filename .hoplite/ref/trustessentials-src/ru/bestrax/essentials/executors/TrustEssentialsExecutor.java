/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.command.CommandSender
 */
package ru.bestrax.essentials.executors;

import org.bukkit.command.CommandSender;
import ru.bestrax.essentials.EssentialsPlugin;
import ru.bestrax.essentials.api.commands.TrustCommand;

public class TrustEssentialsExecutor
extends TrustCommand {
    private EssentialsPlugin plugin;

    public TrustEssentialsExecutor(EssentialsPlugin plugin) {
        super("trustessentials", "essentials");
        this.plugin = plugin;
        this.setPermission("trustessentials.admin");
        this.register();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length != 1) {
            this.sendMessages(sender, "TRUSTESSENTIALS_USAGE", new String[0]);
            return;
        }
        if (args[0].equalsIgnoreCase("reload")) {
            this.plugin.reloadAllConfig();
            this.sendMessage(sender, "TRUSTESSENTIALS_RELOAD", new String[0]);
        } else {
            this.sendMessages(sender, "TRUSTESSENTIALS_USAGE", new String[0]);
        }
    }
}
