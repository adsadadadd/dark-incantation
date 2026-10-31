package me.x1no4ekk.core.top;

import me.x1no4ekk.core.config.ConfigUtil;
import org.bukkit.configuration.ConfigurationSection;

import java.util.regex.Pattern;

/**
 * {@code tops} section of settings.yml.
 *
 * @param balanceTable  table of the Economy plugin (identifiers are validated: they cannot be bound as parameters)
 */
public record TopSettings(int refreshMinutes, int size, int pageSize, boolean balanceEnabled, String balanceTable,
                          String uuidColumn, String nameColumn, String balanceColumn) {

    private static final Pattern IDENTIFIER = Pattern.compile("[A-Za-z0-9_]{1,64}");

    public static TopSettings load(ConfigurationSection s) {
        int refresh = (int) ConfigUtil.nonNegative(s, "refresh-minutes", 5);
        if (refresh < 1) {
            throw new IllegalArgumentException(ConfigUtil.path(s, "refresh-minutes") + " must be at least 1");
        }
        int size = (int) ConfigUtil.nonNegative(s, "size", 100);
        if (size < 1 || size > 1000) {
            throw new IllegalArgumentException(ConfigUtil.path(s, "size") + " must be between 1 and 1000");
        }
        int pageSize = (int) ConfigUtil.nonNegative(s, "page-size", 10);
        if (pageSize < 1 || pageSize > 50) {
            throw new IllegalArgumentException(ConfigUtil.path(s, "page-size") + " must be between 1 and 50");
        }
        ConfigurationSection balance = ConfigUtil.sectionOrEmpty(s, "baltop");
        return new TopSettings(refresh, size, pageSize, balance.getBoolean("enabled", true),
                identifier(balance, "table", "economy_accounts"),
                identifier(balance, "uuid-column", "uuid"),
                identifier(balance, "name-column", "name"),
                identifier(balance, "balance-column", "balance"));
    }

    private static String identifier(ConfigurationSection section, String key, String fallback) {
        String value = section.getString(key, fallback);
        if (value == null || !IDENTIFIER.matcher(value).matches()) {
            throw new IllegalArgumentException(ConfigUtil.path(section, key) + ": '" + value
                    + "' is not a valid table/column name (letters, digits, _)");
        }
        return value;
    }
}
