/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.command.CommandSender
 *  org.bukkit.entity.Player
 */
package ru.bestrax.essentials.executors.weather;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.bestrax.essentials.api.EssentialsApi;
import ru.bestrax.essentials.api.commands.TrustCommand;
import ru.bestrax.essentials.gui.PWeatherGui;
import ru.bestrax.essentials.utils.BukkitUtils;

public class PWeatherExecutor
extends TrustCommand {
    public PWeatherExecutor() {
        super("pweather", "playerweather");
        this.setPlayersOnly(true);
        this.setPermission("trustessentials.pweather");
        this.register();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player)sender;
        if (args.length != 0 && args[0].equalsIgnoreCase("reset")) {
            this.sendMessage((CommandSender)player, "PWEATHER_RESET", new String[0]);
            player.resetPlayerWeather();
        } else {
            EssentialsApi.getGuiManager().getGui(PWeatherGui.class, player).open();
            player.playSound(player.getLocation(), BukkitUtils.getBlockBellSound(), 1.0f, 1.0f);
        }
    }
}
