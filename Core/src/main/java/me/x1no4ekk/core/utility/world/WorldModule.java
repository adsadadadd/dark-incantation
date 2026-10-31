package me.x1no4ekk.core.utility.world;

import me.x1no4ekk.core.CorePlugin;
import me.x1no4ekk.core.config.ConfigFiles;
import me.x1no4ekk.core.config.ConfigUtil;
import me.x1no4ekk.core.module.CoreModule;
import me.x1no4ekk.core.util.Text;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

/**
 * World time and weather ({@code /day}, {@code /night}, {@code /time}, {@code /sun}, {@code /rain}, {@code /storm},
 * {@code /weather}) and personal time/weather ({@code /ptime}, {@code /pweather}).
 */
public final class WorldModule implements CoreModule {

    private final CorePlugin plugin;
    /** World name → end of the shared cooldown (epoch millis). Bounded by the number of worlds; main thread only. */
    private final Map<String, Long> timeCooldowns = new HashMap<>();
    private final Map<String, Long> weatherCooldowns = new HashMap<>();
    private volatile WorldSettings settings;

    public WorldModule(CorePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public String id() {
        return "utility-world";
    }

    @Override
    public Runnable loadConfig(ConfigFiles files) throws Exception {
        WorldSettings loaded = WorldSettings.load(ConfigUtil.section(files.settings(), "utility"));
        return () -> this.settings = loaded;
    }

    @Override
    public void enable() {
        plugin.registerCommand("day", new TimeCommand(plugin, this, "day"));
        plugin.registerCommand("night", new TimeCommand(plugin, this, "night"));
        plugin.registerCommand("time", new TimeCommand(plugin, this, null));
        plugin.registerCommand("sun", new WeatherCommand(plugin, this, WeatherCommand.Kind.CLEAR, "core.sun"));
        plugin.registerCommand("rain", new WeatherCommand(plugin, this, WeatherCommand.Kind.RAIN, "core.rain"));
        plugin.registerCommand("storm", new WeatherCommand(plugin, this, WeatherCommand.Kind.THUNDER, "core.storm"));
        plugin.registerCommand("weather", new WeatherCommand(plugin, this, null, "core.weather"));
        plugin.registerCommand("ptime", new PTimeCommand(plugin, this));
        plugin.registerCommand("pweather", new PWeatherCommand(plugin, this));
    }

    @Override
    public void disable() {
        timeCooldowns.clear();
        weatherCooldowns.clear();
    }

    WorldSettings settings() {
        return settings;
    }

    /**
     * @return milliseconds left before the world's time/weather may be changed again, or 0
     */
    long cooldownLeft(String world, boolean weather) {
        Map<String, Long> cooldowns = weather ? weatherCooldowns : timeCooldowns;
        Long until = cooldowns.get(world);
        if (until == null) {
            return 0L;
        }
        long left = until - System.currentTimeMillis();
        if (left <= 0) {
            cooldowns.remove(world);
            return 0L;
        }
        return left;
    }

    void startCooldown(String world, boolean weather) {
        long cooldown = weather ? settings.weather().cooldownMillis() : settings.time().cooldownMillis();
        if (cooldown > 0) {
            (weather ? weatherCooldowns : timeCooldowns).put(world, System.currentTimeMillis() + cooldown);
        }
    }

    // ---------------------------------------------------------------- personal time & weather

    void setPlayerTime(Player player, WorldSettings.TimeOption option) {
        setPlayerTime(player, option.ticks(), Text.parse(option.label()));
    }

    void setPlayerTime(Player player, long ticks, Component name) {
        player.setPlayerTime(ticks, false);
        plugin.messages().send(player, "utility.ptime.changed", Placeholder.component("time", name));
    }

    void resetPlayerTime(Player player) {
        player.resetPlayerTime();
        plugin.messages().send(player, "utility.ptime.reset");
    }

    void setPlayerWeather(Player player, WorldSettings.WeatherOption option) {
        player.setPlayerWeather(option.type());
        plugin.messages().send(player, "utility.pweather.changed",
                Placeholder.component("weather", Text.parse(option.label())));
    }

    void resetPlayerWeather(Player player) {
        player.resetPlayerWeather();
        plugin.messages().send(player, "utility.pweather.reset");
    }
}
