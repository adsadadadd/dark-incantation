/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.command.CommandSender
 *  org.bukkit.entity.Player
 */
package ru.bestrax.essentials.executors.warp;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.bestrax.essentials.api.EssentialsApi;
import ru.bestrax.essentials.api.commands.TrustCommand;
import ru.bestrax.essentials.gui.SystemWarpGui;

public class ServerWarpExecutor
extends TrustCommand {
    public ServerWarpExecutor() {
        super("serverwarp", "systemwarp");
        this.setPermission("trustessentials.systemwarp");
        this.setPlayersOnly(true);
        this.register();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player)sender;
        SystemWarpGui systemWarpGui = EssentialsApi.getGuiManager().getGui(SystemWarpGui.class, player);
        systemWarpGui.open();
    }
}
