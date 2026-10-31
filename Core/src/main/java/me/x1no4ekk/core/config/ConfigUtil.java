package me.x1no4ekk.core.config;

import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Validation helpers for module configuration. Every failure throws {@link IllegalArgumentException}
 * with the full YAML path, so startup can hard-fail and {@code /core reload} can keep the previous values.
 */
public final class ConfigUtil {

    private ConfigUtil() {
    }

    /**
     * @throws IllegalArgumentException if the section is missing
     */
    public static ConfigurationSection section(ConfigurationSection parent, String path) {
        ConfigurationSection section = parent.getConfigurationSection(path);
        if (section == null) {
            throw new IllegalArgumentException("Missing section '" + path(parent, path) + "'");
        }
        return section;
    }

    /**
     * @return the section, or an empty one if it is missing
     */
    public static ConfigurationSection sectionOrEmpty(ConfigurationSection parent, String path) {
        ConfigurationSection section = parent.getConfigurationSection(path);
        return section != null ? section : new MemoryConfiguration();
    }

    public static String requireText(ConfigurationSection section, String key) {
        String value = section.getString(key);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("'" + path(section, key) + "' must not be empty");
        }
        return value;
    }

    /**
     * Full dotted path of {@code key} inside {@code parent}, for error messages.
     */
    public static String path(ConfigurationSection parent, String key) {
        String base = parent.getCurrentPath();
        return base == null || base.isEmpty() ? key : base + "." + key;
    }

    /**
     * Parses an item material (e.g. {@code DIAMOND_SWORD} or {@code minecraft:diamond_sword}).
     */
    public static Material material(String raw, String path) {
        Material material = raw == null ? null : Material.matchMaterial(raw.trim());
        if (material == null || material.isAir() || !material.isItem()) {
            throw new IllegalArgumentException(path + ": unknown item material '" + raw + "'");
        }
        return material;
    }

    public static Material material(ConfigurationSection section, String key, Material fallback) {
        String raw = section.getString(key);
        if (raw == null || raw.isBlank()) {
            return fallback;
        }
        return material(raw, path(section, key));
    }

    /**
     * Reads inventory slots. Accepts integers and ranges such as {@code "10-16"}; duplicates are removed.
     *
     * @param rows menu height used to validate the slots
     */
    public static List<Integer> slots(ConfigurationSection section, String key, int rows) {
        Set<Integer> result = new LinkedHashSet<>();
        int size = rows * 9;
        for (Object raw : section.getList(key, List.of())) {
            String value = String.valueOf(raw).trim();
            int dash = value.indexOf('-', 1);
            try {
                if (dash > 0) {
                    int from = Integer.parseInt(value.substring(0, dash).trim());
                    int to = Integer.parseInt(value.substring(dash + 1).trim());
                    for (int slot = Math.min(from, to); slot <= Math.max(from, to); slot++) {
                        result.add(checkSlot(slot, size, section, key));
                    }
                } else {
                    result.add(checkSlot(Integer.parseInt(value), size, section, key));
                }
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(path(section, key) + ": '" + value + "' is not a slot or slot range");
            }
        }
        return List.copyOf(new ArrayList<>(result));
    }

    private static int checkSlot(int slot, int size, ConfigurationSection section, String key) {
        if (slot < 0 || slot >= size) {
            throw new IllegalArgumentException(path(section, key) + ": slot " + slot + " is outside the menu (0-" + (size - 1) + ")");
        }
        return slot;
    }

    public static int rows(ConfigurationSection section, String key, int fallback) {
        int rows = section.getInt(key, fallback);
        if (rows < 1 || rows > 6) {
            throw new IllegalArgumentException(path(section, key) + " must be between 1 and 6");
        }
        return rows;
    }

    /**
     * Parses {@code #RRGGBB} or a named colour ({@code red}, {@code gold}...).
     */
    public static TextColor color(String raw, String path) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException(path + " must not be empty");
        }
        TextColor color = raw.startsWith("#") ? TextColor.fromHexString(raw)
                : net.kyori.adventure.text.format.NamedTextColor.NAMES.value(raw.toLowerCase(Locale.ROOT));
        if (color == null) {
            throw new IllegalArgumentException(path + ": invalid colour '" + raw + "'");
        }
        return color;
    }

    /**
     * Parses an enum constant case-insensitively.
     */
    public static <E extends Enum<E>> E enumValue(Class<E> type, String raw, String path) {
        if (raw != null) {
            for (E constant : type.getEnumConstants()) {
                if (constant.name().equalsIgnoreCase(raw.trim())) {
                    return constant;
                }
            }
        }
        throw new IllegalArgumentException(path + ": unknown value '" + raw + "'");
    }

    public static long nonNegative(ConfigurationSection section, String key, long fallback) {
        long value = section.getLong(key, fallback);
        if (value < 0) {
            throw new IllegalArgumentException(path(section, key) + " must not be negative");
        }
        return value;
    }

    public static double nonNegativeDouble(ConfigurationSection section, String key, double fallback) {
        double value = section.getDouble(key, fallback);
        if (value < 0 || Double.isNaN(value) || Double.isInfinite(value)) {
            throw new IllegalArgumentException(path(section, key) + " must be a non-negative number");
        }
        return value;
    }
}
