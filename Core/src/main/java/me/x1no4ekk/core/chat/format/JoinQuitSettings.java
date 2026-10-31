package me.x1no4ekk.core.chat.format;

import me.x1no4ekk.core.chat.ChatYaml;
import me.x1no4ekk.core.config.ConfigUtil;
import net.kyori.adventure.title.Title;
import org.bukkit.configuration.ConfigurationSection;

import java.time.Duration;

/**
 * {@code join-quit} section of chat.yml. Empty templates disable the corresponding message.
 */
public record JoinQuitSettings(
        String join,
        String quit,
        String firstJoin,
        String welcome,
        boolean title,
        String titleText,
        String subtitleText,
        Title.Times titleTimes
) {

    static JoinQuitSettings load(ConfigurationSection root) {
        ConfigurationSection s = ConfigUtil.sectionOrEmpty(root, "join-quit");
        ConfigurationSection title = ConfigUtil.sectionOrEmpty(s, "title");
        Title.Times times = Title.Times.times(
                ticks(title, "fade-in", 10),
                ticks(title, "stay", 60),
                ticks(title, "fade-out", 20));
        return new JoinQuitSettings(
                ChatYaml.text(s, "join"),
                ChatYaml.text(s, "quit"),
                ChatYaml.text(s, "first-join"),
                ChatYaml.text(s, "welcome"),
                title.getBoolean("enabled", false),
                ChatYaml.text(title, "title"),
                ChatYaml.text(title, "subtitle"),
                times);
    }

    private static Duration ticks(ConfigurationSection section, String key, long fallback) {
        return Duration.ofMillis(ConfigUtil.nonNegative(section, key, fallback) * 50L);
    }
}
