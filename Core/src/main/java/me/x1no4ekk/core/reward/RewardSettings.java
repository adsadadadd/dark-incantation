package me.x1no4ekk.core.reward;

import me.x1no4ekk.core.config.ConfigUtil;
import me.x1no4ekk.core.config.SoundEffect;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.event.entity.CreatureSpawnEvent;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Parsed killermoney.yml.
 */
public record RewardSettings(boolean enabled, Set<String> disabledWorlds, PlayerReward player, boolean mobsEnabled,
                             Set<CreatureSpawnEvent.SpawnReason> ignoredSpawnReasons, Map<EntityType, MobReward> mobs) {

    /**
     * Money moved from the victim to the killer.
     *
     * @param percent share of the victim's balance
     * @param min     minimum amount (still limited by the victim's balance)
     * @param max     maximum amount; {@code 0} — unlimited
     */
    public record PlayerReward(boolean enabled, double percent, double min, double max, SoundEffect killerSound,
                               SoundEffect victimSound, List<String> commands) {
    }

    /**
     * Reward for killing a mob.
     *
     * @param message   chat message override; {@code null} — {@code reward.mob.message}, empty — none
     * @param actionBar action bar override; {@code null} — {@code reward.mob.actionbar}, empty — none
     */
    public record MobReward(double min, double max, boolean whole, String message, String actionBar,
                            SoundEffect sound, List<String> commands) {

        /**
         * Random amount in {@code [min, max]}: whole numbers if both bounds are whole, otherwise cents.
         */
        double roll() {
            if (max <= min) {
                return min;
            }
            ThreadLocalRandom random = ThreadLocalRandom.current();
            if (whole) {
                return random.nextLong((long) min, (long) max + 1L);
            }
            return Math.floor(random.nextDouble(min, max) * 100D) / 100D;
        }
    }

    public static RewardSettings load(ConfigurationSection root) {
        Set<String> disabledWorlds = new HashSet<>();
        for (String world : root.getStringList("disabled-worlds")) {
            disabledWorlds.add(world.toLowerCase(Locale.ROOT));
        }

        ConfigurationSection playerSection = ConfigUtil.sectionOrEmpty(root, "player");
        double percent = ConfigUtil.nonNegativeDouble(playerSection, "percent", 10D);
        if (percent > 100D) {
            throw new IllegalArgumentException(ConfigUtil.path(playerSection, "percent") + " must be between 0 and 100");
        }
        double min = ConfigUtil.nonNegativeDouble(playerSection, "min", 0D);
        double max = ConfigUtil.nonNegativeDouble(playerSection, "max", 0D);
        if (max > 0 && max < min) {
            throw new IllegalArgumentException(ConfigUtil.path(playerSection, "max") + " must not be less than min");
        }
        PlayerReward player = new PlayerReward(
                playerSection.getBoolean("enabled", true),
                percent,
                min,
                max,
                SoundEffect.load(playerSection, "killer-sound"),
                SoundEffect.load(playerSection, "victim-sound"),
                List.copyOf(playerSection.getStringList("commands")));

        ConfigurationSection mobsSection = ConfigUtil.sectionOrEmpty(root, "mobs");
        Set<CreatureSpawnEvent.SpawnReason> ignored = EnumSet.noneOf(CreatureSpawnEvent.SpawnReason.class);
        for (String raw : mobsSection.getStringList("ignored-spawn-reasons")) {
            ignored.add(ConfigUtil.enumValue(CreatureSpawnEvent.SpawnReason.class, raw,
                    ConfigUtil.path(mobsSection, "ignored-spawn-reasons")));
        }
        SoundEffect defaultSound = SoundEffect.load(mobsSection, "sound");
        Map<EntityType, MobReward> mobs = new EnumMap<>(EntityType.class);
        ConfigurationSection types = ConfigUtil.sectionOrEmpty(mobsSection, "types");
        for (String key : types.getKeys(false)) {
            ConfigurationSection section = types.getConfigurationSection(key);
            if (section == null) {
                throw new IllegalArgumentException("'" + ConfigUtil.path(types, key) + "' must be a section");
            }
            EntityType type = entityType(key, ConfigUtil.path(types, key));
            mobs.put(type, loadMob(section, defaultSound));
        }

        return new RewardSettings(
                root.getBoolean("enabled", true),
                Set.copyOf(disabledWorlds),
                player,
                mobsSection.getBoolean("enabled", true),
                Set.copyOf(ignored),
                Map.copyOf(mobs));
    }

    private static EntityType entityType(String raw, String path) {
        String name = raw.trim();
        int colon = name.indexOf(':');
        if (colon >= 0) {
            name = name.substring(colon + 1); // minecraft:zombie -> zombie
        }
        EntityType type = ConfigUtil.enumValue(EntityType.class, name, path);
        if (type == EntityType.PLAYER || !type.isAlive()) {
            throw new IllegalArgumentException(path + ": '" + raw + "' is not a mob");
        }
        return type;
    }

    private static MobReward loadMob(ConfigurationSection s, SoundEffect defaultSound) {
        String raw = String.valueOf(s.get("reward", "0")).trim();
        String path = ConfigUtil.path(s, "reward");
        double min;
        double max;
        try {
            int dash = raw.indexOf('-', 1);
            if (dash > 0) {
                min = Double.parseDouble(raw.substring(0, dash).trim().replace(',', '.'));
                max = Double.parseDouble(raw.substring(dash + 1).trim().replace(',', '.'));
            } else {
                min = Double.parseDouble(raw.replace(',', '.'));
                max = min;
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(path + ": '" + raw + "' must be a number or a range like '5-50'");
        }
        if (min < 0 || max < min || Double.isInfinite(max) || Double.isNaN(min) || Double.isNaN(max)) {
            throw new IllegalArgumentException(path + ": '" + raw + "' must be a non-negative range with min <= max");
        }
        return new MobReward(
                min,
                max,
                min == Math.rint(min) && max == Math.rint(max),
                s.isSet("message") ? s.getString("message", "") : null,
                s.isSet("actionbar") ? s.getString("actionbar", "") : null,
                s.isConfigurationSection("sound") ? SoundEffect.load(s, "sound") : defaultSound,
                List.copyOf(s.getStringList("commands")));
    }

    boolean isDisabledWorld(String world) {
        return disabledWorlds.contains(world.toLowerCase(Locale.ROOT));
    }
}
