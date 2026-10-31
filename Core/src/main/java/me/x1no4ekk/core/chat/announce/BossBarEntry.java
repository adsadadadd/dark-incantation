package me.x1no4ekk.core.chat.announce;

import me.x1no4ekk.core.config.ConfigUtil;
import net.kyori.adventure.bossbar.BossBar;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Locale;

/**
 * One rotating boss bar announcement.
 */
public record BossBarEntry(String title, int durationTicks, BossBar.Color color, BossBar.Overlay overlay, Progress progress) {

    public enum Progress {
        DECREASE, INCREASE, FULL;

        public float at(int elapsedTicks, int durationTicks) {
            float done = Math.clamp((float) elapsedTicks / durationTicks, 0f, 1f);
            return switch (this) {
                case DECREASE -> 1f - done;
                case INCREASE -> done;
                case FULL -> 1f;
            };
        }
    }

    static BossBarEntry load(ConfigurationSection s) {
        long seconds = ConfigUtil.nonNegative(s, "duration", 10);
        if (seconds < 1 || seconds > 3600) {
            throw new IllegalArgumentException(ConfigUtil.path(s, "duration") + " must be between 1 and 3600 seconds");
        }
        // Old style names (SOLID, SEGMENTED_6...) are accepted as well.
        String overlay = s.getString("overlay", "PROGRESS").toUpperCase(Locale.ROOT)
                .replace("SOLID", "PROGRESS")
                .replace("SEGMENTED_", "NOTCHED_");
        return new BossBarEntry(
                ConfigUtil.requireText(s, "title"),
                (int) seconds * 20,
                ConfigUtil.enumValue(BossBar.Color.class, s.getString("color", "WHITE"), ConfigUtil.path(s, "color")),
                ConfigUtil.enumValue(BossBar.Overlay.class, overlay, ConfigUtil.path(s, "overlay")),
                ConfigUtil.enumValue(Progress.class, s.getString("progress", "DECREASE"), ConfigUtil.path(s, "progress")));
    }
}
