package me.x1no4ekk.core.chat;

import me.x1no4ekk.core.config.ConfigUtil;
import org.bukkit.configuration.ConfigurationSection;

import java.util.concurrent.TimeUnit;

/**
 * Reading helpers for chat.yml.
 */
public final class ChatYaml {

    public static final String FILE = "chat.yml";

    private ChatYaml() {
    }

    /**
     * A MiniMessage string or a list of lines joined with {@code <newline>}; empty if missing.
     */
    public static String text(ConfigurationSection section, String key) {
        if (section.isList(key)) {
            return String.join("<newline>", section.getStringList(key));
        }
        String value = section.getString(key);
        return value == null ? "" : value;
    }

    /**
     * Non-negative number of seconds, returned in milliseconds.
     */
    public static long secondsToMillis(ConfigurationSection section, String key, long fallbackSeconds) {
        return TimeUnit.SECONDS.toMillis(ConfigUtil.nonNegative(section, key, fallbackSeconds));
    }
}
