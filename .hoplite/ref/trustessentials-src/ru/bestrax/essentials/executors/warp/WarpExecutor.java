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
import ru.bestrax.essentials.api.Warp;
import ru.bestrax.essentials.api.commands.TrustCommand;
import ru.bestrax.essentials.gui.WarpGui;
import ru.bestrax.essentials.utils.BukkitUtils;

public class WarpExecutor
extends TrustCommand {
    public WarpExecutor() {
        super("warp", "warps");
        this.setPermission("trustessentials.warp");
        this.setPlayersOnly(true);
        this.register();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player)sender;
        if (args.length == 0) {
            WarpGui warpGui = EssentialsApi.getGuiManager().getGui(WarpGui.class, player);
            if (warpGui != null) {
                warpGui.open();
                player.playSound(player.getLocation(), BukkitUtils.getBlockBellSound(), 1.0f, 1.0f);
            }
        } else {
            String name = args[0];
            Warp warp = EssentialsApi.getWarpManager().getWarp(name);
            if (warp == null) {
                this.sendMessage((CommandSender)player, "WARP_NOT_FOUND", "%name%", name);
            } else {
                warp.teleport(player);
            }
        }
    }
}
