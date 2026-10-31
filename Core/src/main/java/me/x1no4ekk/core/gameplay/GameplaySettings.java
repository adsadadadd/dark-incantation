package me.x1no4ekk.core.gameplay;

import me.x1no4ekk.core.config.ConfigUtil;
import org.bukkit.configuration.ConfigurationSection;

/**
 * {@code gameplay} section of settings.yml.
 */
public record GameplaySettings(
        Fly fly,
        FlyElytra flyElytra,
        God god,
        boolean speedResetOnQuit,
        boolean healRemoveNegativeEffects,
        Burn burn,
        int giveMaxAmount,
        int headMaxAmount,
        Repair repair,
        ItemRename itemRename,
        Firework firework
) {

    public record Fly(long othersCooldownMillis, boolean blockPickup, boolean safeLogin) {
    }

    public record FlyElytra(boolean enabled, int durabilityPerSecond, boolean disableInCombat) {
    }

    public record God(boolean keepFood, boolean preventMobTarget, boolean blockPvp, boolean blockPickup,
                      boolean healOnEnable) {
    }

    public record Burn(int defaultSeconds, int maxSeconds) {
    }

    public record Repair(double cost, double allCost) {
    }

    public record ItemRename(int maxLength, String prefix) {
    }

    public record Firework(int count, int defaultPower, int maxPower, long cooldownMillis, boolean harmless) {
    }

    public static GameplaySettings load(ConfigurationSection s) {
        ConfigurationSection fly = ConfigUtil.sectionOrEmpty(s, "fly");
        ConfigurationSection elytra = ConfigUtil.sectionOrEmpty(s, "fly-elytra");
        ConfigurationSection god = ConfigUtil.sectionOrEmpty(s, "god");
        ConfigurationSection speed = ConfigUtil.sectionOrEmpty(s, "speed");
        ConfigurationSection heal = ConfigUtil.sectionOrEmpty(s, "heal");
        ConfigurationSection burn = ConfigUtil.sectionOrEmpty(s, "burn");
        ConfigurationSection give = ConfigUtil.sectionOrEmpty(s, "give");
        ConfigurationSection head = ConfigUtil.sectionOrEmpty(s, "head");
        ConfigurationSection repair = ConfigUtil.sectionOrEmpty(s, "repair");
        ConfigurationSection rename = ConfigUtil.sectionOrEmpty(s, "itemrename");
        ConfigurationSection firework = ConfigUtil.sectionOrEmpty(s, "firework");

        int burnMax = range(burn, "max-seconds", 300, 1, 3600);
        int fireworkMax = range(firework, "max-power", 3, 0, 10);
        return new GameplaySettings(
                new Fly(
                        ConfigUtil.nonNegative(fly, "others-cooldown-seconds", 60) * 1000L,
                        fly.getBoolean("block-pickup", true),
                        fly.getBoolean("safe-login", true)),
                new FlyElytra(
                        elytra.getBoolean("enabled", true),
                        range(elytra, "durability-per-second", 1, 0, 100),
                        elytra.getBoolean("disable-in-combat", true)),
                new God(
                        god.getBoolean("keep-food", true),
                        god.getBoolean("prevent-mob-target", true),
                        god.getBoolean("block-pvp", true),
                        god.getBoolean("block-pickup", true),
                        god.getBoolean("heal-on-enable", true)),
                speed.getBoolean("reset-on-quit", true),
                heal.getBoolean("remove-negative-effects", true),
                new Burn(range(burn, "default-seconds", 10, 1, burnMax), burnMax),
                range(give, "max-amount", 2304, 1, 36 * 64 * 10),
                range(head, "max-amount", 64, 1, 64),
                new Repair(
                        ConfigUtil.nonNegativeDouble(repair, "cost", 100),
                        ConfigUtil.nonNegativeDouble(repair, "all-cost", 5000)),
                new ItemRename(
                        range(rename, "max-length", 50, 1, 256),
                        rename.getString("prefix", "")),
                new Firework(
                        range(firework, "count", 3, 1, 20),
                        range(firework, "default-power", 1, 0, fireworkMax),
                        fireworkMax,
                        ConfigUtil.nonNegative(firework, "cooldown-seconds", 5) * 1000L,
                        firework.getBoolean("harmless", true)));
    }

    private static int range(ConfigurationSection section, String key, int fallback, int min, int max) {
        int value = section.getInt(key, fallback);
        if (value < min || value > max) {
            throw new IllegalArgumentException(ConfigUtil.path(section, key) + " must be between " + min + " and " + max);
        }
        return value;
    }
}
