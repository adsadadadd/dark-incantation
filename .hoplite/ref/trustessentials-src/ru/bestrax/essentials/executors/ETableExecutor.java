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
import ru.bestrax.essentials.api.provider.ContainerProvider;

public class ETableExecutor
extends TrustCommand {
    public ETableExecutor() {
        super("etable", new String[0]);
        this.setPermission("trustessentials.etable");
        this.setPlayersOnly(true);
        this.register();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        ContainerProvider containerProvider = EssentialsApi.getProviderManager().getContainerProvider();
        Player player = (Player)sender;
        if (containerProvider == null) {
            player.openEnchanting(null, true);
        } else {
            containerProvider.openEnchanting(player);
        }
    }
}
