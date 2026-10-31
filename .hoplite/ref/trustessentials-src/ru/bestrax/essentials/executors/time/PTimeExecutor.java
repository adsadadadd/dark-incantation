/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.lang.StringUtils
 *  org.bukkit.command.CommandSender
 *  org.bukkit.entity.Player
 */
package ru.bestrax.essentials.executors.time;

import org.apache.commons.lang.StringUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.bestrax.essentials.api.EssentialsApi;
import ru.bestrax.essentials.api.commands.TrustCommand;
import ru.bestrax.essentials.gui.PTimeGui;
import ru.bestrax.essentials.utils.BukkitUtils;

public class PTimeExecutor
extends TrustCommand {
    public PTimeExecutor() {
        super("ptime", "playertime");
        this.setPermission("trustessentials.ptime");
        this.setPlayersOnly(true);
        this.register();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player)sender;
        if (args.length == 0) {
            EssentialsApi.getGuiManager().getGui(PTimeGui.class, player).open();
            player.playSound(player.getLocation(), BukkitUtils.getBlockBellSound(), 1.0f, 1.0f);
        } else if (args[0].equalsIgnoreCase("reset")) {
            player.resetPlayerTime();
            this.sendMessage((CommandSender)player, "PTIME_RESET", new String[0]);
        } else {
            if (!StringUtils.isNumeric((String)args[0])) {
                this.sendMessage((CommandSender)player, "NUMBER_ONLY", "%replaced%", args[0]);
                return;
            }
            long time = Integer.parseInt(args[0]);
            this.sendMessage((CommandSender)player, "PTIME_CHANGE", "%ticks%", String.valueOf(time));
            player.setPlayerTime(time, false);
        }
    }
}
