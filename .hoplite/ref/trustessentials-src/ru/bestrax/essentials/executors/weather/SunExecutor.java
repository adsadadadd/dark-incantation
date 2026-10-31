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

public class SunExecutor
extends TrustCommand {
    private EssentialsPlugin plugin;

    public SunExecutor(EssentialsPlugin plugin) {
        super("sun", new String[0]);
        this.setPermission("trustessentials.sun");
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
        WeatherExecutor.setWeather(settings.getStringList("weatherWorld", new String[0]), this.getStringList("WEATHER_SUN_BROADCAST", "%name%", sender.getName()), 0, false, false);
        this.sendMessage(sender, "WEATHER_SUN", new String[0]);
        if (settings.getBoolean("weatherCooldown")) {
            Cooldowns.set("essentials", "weather", settings.getInt("weatherCooldownMillis"));
        }
    }
}
