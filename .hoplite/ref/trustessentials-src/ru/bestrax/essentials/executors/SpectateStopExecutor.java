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
import ru.bestrax.essentials.api.manager.SpectateManager;

public class SpectateStopExecutor
extends TrustCommand {
    public SpectateStopExecutor() {
        super("specstop", "spectatestop", "specoff", "spectateoff");
        this.setPermission("trustessentials.specstop");
        this.setPlayersOnly(true);
        this.register();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player)sender;
        SpectateManager spectateManager = EssentialsApi.getSpectateManager();
        if (!spectateManager.isSpectation(player)) {
            this.sendMessage(sender, "SPECTATE_NOT", new String[0]);
            return;
        }
        spectateManager.stop(player);
    }
}
