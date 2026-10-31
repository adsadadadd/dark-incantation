/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.lang.StringUtils
 *  org.bukkit.Bukkit
 *  org.bukkit.World
 *  org.bukkit.command.CommandSender
 *  org.bukkit.entity.Player
 */
package ru.bestrax.essentials.executors.weather;

import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.bestrax.essentials.EssentialsPlugin;
import ru.bestrax.essentials.api.commands.TrustCommand;
import ru.bestrax.essentials.api.config.Config;
import ru.bestrax.essentials.api.cooldown.Cooldowns;
import ru.bestrax.essentials.utils.MessagesUtils;
import ru.bestrax.essentials.utils.TimeUtils;

public class WeatherExecutor
extends TrustCommand {
    private EssentialsPlugin plugin;

    public WeatherExecutor(EssentialsPlugin plugin) {
        super("weather", new String[0]);
        this.setPermission("trustessentials.weather");
        this.register();
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        block18: {
            block17: {
                Config settings = this.plugin.getSettings();
                if (args.length != 2) {
                    String time = sender instanceof Player ? WeatherExecutor.getWeather(((Player)sender).getWorld()) : "\u00a7c\u041d\u0438\u0447\u0435\u0433\u043e";
                    this.sendMessages(sender, "WEATHER", "%weather%", time);
                    return;
                }
                if (!args[0].equalsIgnoreCase("set")) break block17;
                String time = args[1];
                if (settings.getBoolean("weatherCooldown") && Cooldowns.has("essentials", "weather") && !sender.hasPermission("trustessentials.weather.cooldown.bypass")) {
                    this.sendMessage(sender, "WEATHER_COOLDOWN", "%time%", TimeUtils.getTimeInSeconds(Cooldowns.get("essentials", "weather") / 1000));
                    return;
                }
                switch (time) {
                    case "sun": {
                        WeatherExecutor.setWeather(settings.getStringList("weatherWorld", new String[0]), this.getStringList("WEATHER_SUN_BROADCAST", "%name%", sender.getName()), 0, false, false);
                        this.sendMessage(sender, "WEATHER_SUN", new String[0]);
                        if (settings.getBoolean("weatherCooldown")) {
                            Cooldowns.set("essentials", "time", settings.getInt("weather_cooldown_millis"));
                            break;
                        }
                        break block18;
                    }
                    case "rain": {
                        WeatherExecutor.setWeather(settings.getStringList("weatherWorld", new String[0]), this.getStringList("WEATHER_RAIN_BROADCAST", "%name%", sender.getName()), 24000, true, false);
                        this.sendMessage(sender, "WEATHER_RAIN", new String[0]);
                        if (settings.getBoolean("weatherCooldown")) {
                            Cooldowns.set("essentials", "time", settings.getInt("weather_cooldown_millis"));
                            break;
                        }
                        break block18;
                    }
                    case "storm": {
                        WeatherExecutor.setWeather(settings.getStringList("weatherWorld", new String[0]), this.getStringList("WEATHER_STORM_BROADCAST", "%name%", sender.getName()), 24000, true, true);
                        this.sendMessage(sender, "WEATHER_STORM", new String[0]);
                        if (settings.getBoolean("weatherCooldown")) {
                            Cooldowns.set("essentials", "time", settings.getInt("weather_cooldown_millis"));
                            break;
                        }
                        break block18;
                    }
                    default: {
                        String str = args[1];
                        if (!StringUtils.isNumeric((String)str)) {
                            this.sendMessage(sender, "NUMBER_ONLY", "%replaced%", str);
                            break;
                        }
                        WeatherExecutor.setWeather(settings.getStringList("weatherWorld", new String[0]), this.getStringList("WEATHER_CUSTOM_BROADCAST", "%name%", sender.getName(), "%custom%", str), Integer.parseInt(str), false, false);
                        this.sendMessage(sender, "WEATHER_CUSTOM", "%custom%", str);
                        if (settings.getBoolean("weatherCooldown")) {
                            Cooldowns.set("essentials", "time", settings.getInt("weather_cooldown_millis"));
                            break;
                        }
                        break block18;
                    }
                }
                break block18;
            }
            String time = sender instanceof Player ? WeatherExecutor.getWeather(((Player)sender).getWorld()) : "\u00a7c\u041d\u0438\u0447\u0435\u0433\u043e";
            this.sendMessages(sender, "WEATHER", "%weather%", time);
        }
    }

    public static void setWeather(List<String> worlds, List<String> broadcastMessage, int ticks, boolean rain, boolean storm) {
        MessagesUtils.sendMessagesAll(broadcastMessage);
        worlds.forEach(x -> Bukkit.getWorld((String)x).setTime((long)ticks));
        for (String world : worlds) {
            World worldBukkit = Bukkit.getWorld((String)world);
            worldBukkit.setWeatherDuration(0);
            worldBukkit.setStorm(rain);
            worldBukkit.setThundering(storm);
        }
    }

    public static String getWeather(World world) {
        String weather = world.getWeatherDuration() < 24000 ? "&e\u0414\u043e\u0436\u0434\u043b\u0438\u0432\u0430\u044f" : "&e\u0421\u043e\u043b\u043d\u0435\u0447\u043d\u0430\u044f";
        weather = world.isThundering() ? "&e\u0428\u0442\u043e\u0440\u043c\u043e\u0432\u0430\u044f" : weather;
        return weather;
    }
}
