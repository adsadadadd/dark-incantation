package me.x1no4ekk.core.chat.format;

import me.x1no4ekk.core.chat.ChatYaml;
import me.x1no4ekk.core.config.ConfigUtil;
import org.bukkit.configuration.ConfigurationSection;

/**
 * Donate or staff chat: format and cooldown.
 */
public record ChannelSettings(String format, long cooldownMillis) {

    static ChannelSettings load(ConfigurationSection parent, String key) {
        ConfigurationSection section = ConfigUtil.section(parent, key);
        return new ChannelSettings(ConfigUtil.requireText(section, "format"), ChatYaml.secondsToMillis(section, "cooldown", 0));
    }
}
