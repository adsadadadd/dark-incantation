package me.x1no4ekk.core.chat.announce;

import me.x1no4ekk.core.config.ConfigUtil;
import me.x1no4ekk.core.config.SoundEffect;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;

/**
 * {@code auto-broadcast} and {@code boss-bars} sections of chat.yml.
 *
 * @param messages MiniMessage announcements; multi-line entries are joined with {@code <newline>}
 */
public record AnnouncerSettings(
        boolean autoEnabled,
        long intervalSeconds,
        boolean random,
        SoundEffect sound,
        List<String> messages,
        boolean barsEnabled,
        List<BossBarEntry> bars
) {

    public static AnnouncerSettings load(ConfigurationSection root) {
        ConfigurationSection auto = ConfigUtil.section(root, "auto-broadcast");
        boolean autoEnabled = auto.getBoolean("enabled", true);
        long interval = ConfigUtil.nonNegative(auto, "interval", 300);
        if (autoEnabled && interval < 1) {
            throw new IllegalArgumentException(ConfigUtil.path(auto, "interval") + " must be at least 1 second");
        }
        List<String> messages = new ArrayList<>();
        for (Object entry : auto.getList("messages", List.of())) {
            String text = entry instanceof List<?> lines ? joinLines(lines) : String.valueOf(entry);
            if (entry != null && !text.isBlank()) {
                messages.add(text);
            }
        }

        ConfigurationSection barSection = ConfigUtil.section(root, "boss-bars");
        List<BossBarEntry> bars = new ArrayList<>();
        ConfigurationSection entries = ConfigUtil.sectionOrEmpty(barSection, "entries");
        for (String key : entries.getKeys(false)) {
            bars.add(BossBarEntry.load(ConfigUtil.section(entries, key)));
        }

        return new AnnouncerSettings(
                autoEnabled,
                interval,
                auto.getBoolean("random", false),
                SoundEffect.load(auto, "sound"),
                List.copyOf(messages),
                barSection.getBoolean("enabled", true),
                List.copyOf(bars));
    }

    private static String joinLines(List<?> lines) {
        List<String> parts = new ArrayList<>(lines.size());
        for (Object line : lines) {
            parts.add(String.valueOf(line));
        }
        return String.join("<newline>", parts);
    }
}
