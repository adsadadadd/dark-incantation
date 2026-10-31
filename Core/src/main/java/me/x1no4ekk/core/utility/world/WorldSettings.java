package me.x1no4ekk.core.utility.world;

import me.x1no4ekk.core.config.ConfigUtil;
import me.x1no4ekk.core.config.MenuItem;
import me.x1no4ekk.core.config.SoundEffect;
import org.bukkit.WeatherType;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.BiFunction;

/**
 * {@code utility.time}, {@code utility.weather}, {@code utility.ptime-menu}, {@code utility.pweather-menu}.
 */
public record WorldSettings(Control time, Control weather, int defaultWeatherSeconds,
                            PersonalMenu<TimeOption> ptimeMenu, PersonalMenu<WeatherOption> pweatherMenu) {

    /**
     * @param worlds lower-case world names; empty means every world
     */
    public record Control(Set<String> worlds, long cooldownMillis) {

        public boolean allows(World world) {
            return worlds.isEmpty() || worlds.contains(world.getName().toLowerCase(Locale.ROOT));
        }
    }

    public record TimeOption(String key, long ticks, String label, MenuItem item) {
    }

    public record WeatherOption(String key, WeatherType type, String label, MenuItem item) {
    }

    /**
     * Layout of {@code /ptime} or {@code /pweather}; {@code filler} and {@code close} are optional.
     */
    public record PersonalMenu<T>(String title, int rows, SoundEffect sound, boolean closeOnClick, MenuItem filler,
                                  List<T> options, MenuItem reset, MenuItem close) {
    }

    public static WorldSettings load(ConfigurationSection utility) {
        ConfigurationSection weather = ConfigUtil.section(utility, "weather");
        return new WorldSettings(
                control(ConfigUtil.section(utility, "time")),
                control(weather),
                (int) Math.min(1_000_000, ConfigUtil.nonNegative(weather, "default-duration-seconds", 0)),
                menu(ConfigUtil.section(utility, "ptime-menu"), 5, (option, rows) -> {
                    long ticks = ConfigUtil.nonNegative(option, "ticks", 0);
                    if (ticks >= 24_000) {
                        throw new IllegalArgumentException(ConfigUtil.path(option, "ticks") + " must be between 0 and 23999");
                    }
                    return new TimeOption(key(option), ticks, ConfigUtil.requireText(option, "label"),
                            MenuItem.load(option, rows, true));
                }),
                menu(ConfigUtil.section(utility, "pweather-menu"), 3, (option, rows) -> new WeatherOption(key(option),
                        ConfigUtil.enumValue(WeatherType.class, option.getString("type"), ConfigUtil.path(option, "type")),
                        ConfigUtil.requireText(option, "label"),
                        MenuItem.load(option, rows, true))));
    }

    private static Control control(ConfigurationSection section) {
        Set<String> worlds = new HashSet<>();
        for (String world : section.getStringList("worlds")) {
            worlds.add(world.trim().toLowerCase(Locale.ROOT));
        }
        long cooldownSeconds = ConfigUtil.nonNegative(section, "cooldown-seconds", 60);
        return new Control(Set.copyOf(worlds), cooldownSeconds * 1000L);
    }

    private static <T> PersonalMenu<T> menu(ConfigurationSection section, int defaultRows,
                                            BiFunction<ConfigurationSection, Integer, T> optionLoader) {
        int rows = ConfigUtil.rows(section, "rows", defaultRows);
        ConfigurationSection optionsSection = ConfigUtil.section(section, "options");
        List<T> options = new ArrayList<>();
        for (String key : optionsSection.getKeys(false)) {
            if (key.equalsIgnoreCase("reset")) {
                throw new IllegalArgumentException(ConfigUtil.path(optionsSection, key) + ": 'reset' is reserved");
            }
            options.add(optionLoader.apply(ConfigUtil.section(optionsSection, key), rows));
        }
        if (options.isEmpty()) {
            throw new IllegalArgumentException("'" + ConfigUtil.path(section, "options") + "' must not be empty");
        }
        return new PersonalMenu<>(
                ConfigUtil.requireText(section, "title"),
                rows,
                SoundEffect.load(section, "sound"),
                section.getBoolean("close-on-click", true),
                optional(section, "filler", rows),
                List.copyOf(options),
                MenuItem.load(section, "reset", rows, true),
                optional(section, "close", rows));
    }

    private static MenuItem optional(ConfigurationSection section, String key, int rows) {
        return section.isConfigurationSection(key) ? MenuItem.load(section, key, rows, true) : null;
    }

    private static String key(ConfigurationSection option) {
        return option.getName().toLowerCase(Locale.ROOT);
    }
}
