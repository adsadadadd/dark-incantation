/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.command.CommandSender
 */
package ru.bestrax.essentials.executors.weather;

import org.bukkit.command.CommandSender;
import ru.bestrax.essentials.EssentialsPlugin;
import ru.bestrax.essentials.api.commands.TrustCommand;
import ru.bestrax.essentials.api.config.Config;
import ru.bestrax.essentials.api.cooldown.Cooldowns;
import ru.bestrax.essentials.executors.weather.WeatherExecutor;
import ru.bestrax.essentials.utils.TimeUtils;

public class StormExecutor
extends TrustCommand {
    private EssentialsPlugin plugin;

    public StormExecutor(EssentialsPlugin plugin) {
        super("storm", new String[0]);
        this.setPermission("trustessentials.storm");
        this.register();
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Config settings = this.plugin.getSettings();
        if (settings.getBoolean("weatherCooldown") && Cooldowns.has("essentials", "weather") && !sender.hasPermission("trustessentials.weather.cooldown.bypass")) {
            this.sendMessage(sender, "WEATHER_COOLDOWN", "%time%", TimeUtils.getTimeInSeconds(Cooldowns.get("essentials", "weather") / 1000));
            return;
        }
        WeatherExecutor.setWeather(settings.getStringList("weatherWorld", new String[0]), this.getStringList("WEATHER_STORM_BROADCAST", "%name%", sender.getName()), 24000, true, true);
        this.sendMessage(sender, "WEATHER_STORM", new String[0]);
        if (settings.getBoolean("weatherCooldown")) {
            Cooldowns.set("essentials", "time", settings.getInt("weatherCooldownMillis"));
        }
    }
}
