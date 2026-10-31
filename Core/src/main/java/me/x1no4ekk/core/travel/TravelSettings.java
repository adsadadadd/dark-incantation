package me.x1no4ekk.core.travel;

import me.x1no4ekk.core.config.ConfigUtil;
import me.x1no4ekk.core.config.SoundEffect;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * {@code travel} section of settings.yml.
 */
public record TravelSettings(
        long tpaTimeoutMillis,
        long tpaCooldownMillis,
        SoundEffect tpaSound,
        long backPickupBlockMillis,
        boolean topWarmup,
        boolean topCheckCombat,
        boolean spawnNewPlayers,
        boolean spawnOnJoin,
        boolean respawnAtSpawn,
        boolean voidRescue,
        Set<String> voidWorlds,
        int voidLevel
) {

    public static TravelSettings load(ConfigurationSection s) {
        ConfigurationSection tpa = ConfigUtil.sectionOrEmpty(s, "tpa");
        ConfigurationSection back = ConfigUtil.sectionOrEmpty(s, "back");
        ConfigurationSection top = ConfigUtil.sectionOrEmpty(s, "top");
        ConfigurationSection spawn = ConfigUtil.sectionOrEmpty(s, "spawn");
        ConfigurationSection rescue = ConfigUtil.sectionOrEmpty(spawn, "void-rescue");

        long timeout = ConfigUtil.nonNegative(tpa, "request-timeout-seconds", 60);
        if (timeout < 5) {
            throw new IllegalArgumentException(ConfigUtil.path(tpa, "request-timeout-seconds") + " must be at least 5");
        }
        return new TravelSettings(
                timeout * 1000L,
                ConfigUtil.nonNegative(tpa, "cooldown-seconds", 10) * 1000L,
                SoundEffect.load(tpa, "request-sound"),
                ConfigUtil.nonNegative(back, "pickup-block-seconds", 60) * 1000L,
                top.getBoolean("warmup", false),
                top.getBoolean("check-combat", true),
                spawn.getBoolean("teleport-new-players", true),
                spawn.getBoolean("teleport-on-join", false),
                spawn.getBoolean("respawn-at-spawn", true),
                rescue.getBoolean("enabled", true),
                rescue.getStringList("worlds").stream()
                        .map(world -> world.toLowerCase(Locale.ROOT))
                        .collect(Collectors.toUnmodifiableSet()),
                rescue.getInt("y-level", 10)
        );
    }
}
