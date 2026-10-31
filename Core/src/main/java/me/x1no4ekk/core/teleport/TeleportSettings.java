package me.x1no4ekk.core.teleport;

import me.x1no4ekk.core.config.ConfigUtil;
import me.x1no4ekk.core.config.SoundEffect;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Color;
import org.bukkit.configuration.ConfigurationSection;

/**
 * {@code teleport} section of settings.yml.
 */
public record TeleportSettings(
        int warmupSeconds,
        boolean cancelOnMove,
        boolean cancelOnDamage,
        boolean bossbarEnabled,
        BossBar.Color bossbarColor,
        BossBar.Overlay bossbarOverlay,
        boolean particlesEnabled,
        Color particleColor,
        float particleSize,
        int particleCount,
        long protectionMillis,
        boolean backOnDeath,
        SoundEffect arrivalSound
) {

    public static TeleportSettings load(ConfigurationSection s) {
        ConfigurationSection bossbar = ConfigUtil.sectionOrEmpty(s, "bossbar");
        ConfigurationSection particles = ConfigUtil.sectionOrEmpty(s, "particles");
        TextColor color = ConfigUtil.color(particles.getString("color", "#00e676"), ConfigUtil.path(particles, "color"));
        return new TeleportSettings(
                (int) Math.min(60, ConfigUtil.nonNegative(s, "warmup-seconds", 4)),
                s.getBoolean("cancel-on-move", true),
                s.getBoolean("cancel-on-damage", true),
                bossbar.getBoolean("enabled", true),
                ConfigUtil.enumValue(BossBar.Color.class, bossbar.getString("color", "GREEN"), ConfigUtil.path(bossbar, "color")),
                ConfigUtil.enumValue(BossBar.Overlay.class, bossbar.getString("overlay", "PROGRESS"), ConfigUtil.path(bossbar, "overlay")),
                particles.getBoolean("enabled", true),
                Color.fromRGB(color.value()),
                (float) Math.clamp(particles.getDouble("size", 1.0), 0.1, 4.0),
                Math.clamp(particles.getInt("count", 6), 0, 50),
                Math.round(ConfigUtil.nonNegativeDouble(s, "protection-seconds", 3.5) * 1000D),
                s.getBoolean("back-on-death", true),
                SoundEffect.load(s, "arrival-sound")
        );
    }
}
