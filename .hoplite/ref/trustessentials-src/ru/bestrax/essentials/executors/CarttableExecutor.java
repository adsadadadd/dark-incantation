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
import ru.bestrax.essentials.api.EssentialsApi;
import ru.bestrax.essentials.api.commands.TrustCommand;

public class CarttableExecutor
extends TrustCommand {
    public CarttableExecutor() {
        super("carttable", new String[0]);
        this.setPermission("trustessentials.carttable");
        this.setPlayersOnly(true);
        this.register();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (EssentialsApi.getProviderManager().getContainerProvider() == null) {
            throw new UnsupportedOperationException("Use paper");
        }
        EssentialsApi.getProviderManager().getContainerProvider().openCartographyTable((Player)sender);
    }
}
