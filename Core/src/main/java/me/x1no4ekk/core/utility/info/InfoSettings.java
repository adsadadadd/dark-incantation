package me.x1no4ekk.core.utility.info;

import me.x1no4ekk.core.config.ConfigUtil;
import me.x1no4ekk.core.utility.UtilityText;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.permissions.Permissible;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * {@code utility.near}, {@code utility.gps}, {@code utility.date}, {@code utility.list}, {@code utility.rules},
 * {@code utility.links}.
 */
public record InfoSettings(Near near, int gpsArriveDistance, Dates dates, Map<String, String> listGroups,
                           List<String> rules, List<String> links) {

    /**
     * @param radiusPermissions {@code core.near.radius.<key>} → maximum radius
     */
    public record Near(int defaultRadius, int maxRadius, Map<String, Integer> radiusPermissions, int maxEntries,
                       boolean hideInvisible) {

        public int allowedRadius(Permissible player) {
            int allowed = maxRadius;
            for (Map.Entry<String, Integer> entry : radiusPermissions.entrySet()) {
                if (entry.getValue() > allowed && player.hasPermission("core.near.radius." + entry.getKey())) {
                    allowed = entry.getValue();
                }
            }
            return allowed;
        }
    }

    public record Dates(ZoneId zone, DateTimeFormatter date, DateTimeFormatter time, DateTimeFormatter day) {
    }

    public static InfoSettings load(ConfigurationSection utility) {
        ConfigurationSection near = ConfigUtil.section(utility, "near");
        ConfigurationSection gps = ConfigUtil.section(utility, "gps");
        ConfigurationSection date = ConfigUtil.section(utility, "date");
        ConfigurationSection list = ConfigUtil.section(utility, "list");
        return new InfoSettings(
                loadNear(near),
                (int) Math.min(10_000, ConfigUtil.nonNegative(gps, "arrive-distance", 5)),
                loadDates(date),
                loadGroups(ConfigUtil.sectionOrEmpty(list, "groups")),
                List.copyOf(ConfigUtil.section(utility, "rules").getStringList("lines")),
                List.copyOf(ConfigUtil.section(utility, "links").getStringList("lines")));
    }

    private static Near loadNear(ConfigurationSection section) {
        int maxRadius = (int) Math.min(10_000, ConfigUtil.nonNegative(section, "max-radius", 200));
        int defaultRadius = (int) Math.min(maxRadius, ConfigUtil.nonNegative(section, "default-radius", 200));
        if (maxRadius < 1 || defaultRadius < 1) {
            throw new IllegalArgumentException(ConfigUtil.path(section, "max-radius") + "/default-radius must be at least 1");
        }
        Map<String, Integer> permissions = new LinkedHashMap<>();
        ConfigurationSection limits = ConfigUtil.sectionOrEmpty(section, "radius-permissions");
        for (String key : limits.getKeys(false)) {
            permissions.put(key.toLowerCase(Locale.ROOT), (int) Math.min(10_000, ConfigUtil.nonNegative(limits, key, 0)));
        }
        return new Near(defaultRadius, maxRadius, Map.copyOf(permissions),
                (int) Math.max(1, Math.min(100, ConfigUtil.nonNegative(section, "max-entries", 15))),
                section.getBoolean("hide-invisible", true));
    }

    private static Dates loadDates(ConfigurationSection section) {
        ZoneId zone = UtilityText.zone(section.getString("zone"), ConfigUtil.path(section, "zone"));
        return new Dates(zone,
                UtilityText.formatter(section.getString("date-format", "dd.MM.yyyy"), zone, ConfigUtil.path(section, "date-format")),
                UtilityText.formatter(section.getString("time-format", "HH:mm:ss"), zone, ConfigUtil.path(section, "time-format")),
                UtilityText.formatter("EEEE", zone, ConfigUtil.path(section, "zone")));
    }

    private static Map<String, String> loadGroups(ConfigurationSection section) {
        Map<String, String> groups = new LinkedHashMap<>();
        for (String key : section.getKeys(false)) {
            groups.put(key.toLowerCase(Locale.ROOT), ConfigUtil.requireText(section, key));
        }
        // LinkedHashMap keeps the configured display order; it is never modified after loading.
        return Collections.unmodifiableMap(groups);
    }
}
