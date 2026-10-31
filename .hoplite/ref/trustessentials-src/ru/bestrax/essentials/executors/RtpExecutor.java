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
import ru.bestrax.essentials.gui.RtpGui;
import ru.bestrax.essentials.utils.BukkitUtils;

public class RtpExecutor
extends TrustCommand {
    public RtpExecutor() {
        super("rtp", "tpr", "randomtp", "randomteleport", "randomteleportation");
        this.setPermission("trustessentials.rtp");
        this.setPlayersOnly(true);
        this.register();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player)sender;
        RtpGui rtpGui = EssentialsApi.getGuiManager().getGui(RtpGui.class, player);
        if (rtpGui != null) {
            rtpGui.open();
            player.playSound(player.getLocation(), BukkitUtils.getBlockBellSound(), 1.0f, 1.0f);
        }
    }
}
