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

public class GrindStoneExecutor
extends TrustCommand {
    public GrindStoneExecutor() {
        super("grindstone", new String[0]);
        this.setPermission("trustessentials.grindstone");
        this.setPlayersOnly(true);
        this.register();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (EssentialsApi.getProviderManager().getContainerProvider() == null) {
            throw new UnsupportedOperationException("Use paper");
        }
        EssentialsApi.getProviderManager().getContainerProvider().openGrindstone((Player)sender);
    }
}
