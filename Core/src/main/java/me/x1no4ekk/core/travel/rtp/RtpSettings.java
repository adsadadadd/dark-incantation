package me.x1no4ekk.core.travel.rtp;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import me.x1no4ekk.core.config.ConfigUtil;
import me.x1no4ekk.core.config.MenuItem;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.block.Biome;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * {@code rtp} section of settings.yml.
 */
public record RtpSettings(
        int maxAttempts,
        Set<Material> unsafeBlocks,
        Set<NamespacedKey> blockedBiomes,
        GuiSettings gui,
        Map<String, RtpMode> modes
) {

    private static final Pattern MODE_ID = Pattern.compile("[a-z0-9_-]{1,32}");

    public enum ModeType {
        /** Random point in a ring around a centre. */
        RANDOM,
        /** Random point near a random online player. */
        NEAR_PLAYER
    }

    public enum Center {
        WORLD_SPAWN, BORDER_CENTER, CUSTOM
    }

    /**
     * @param world            world name; for {@link ModeType#NEAR_PLAYER} an empty name means any world
     * @param minRadius        minimum distance from the centre (or from the chosen player)
     * @param maxRadius        maximum distance from the centre (or from the chosen player)
     * @param playerMaxDistance NEAR_PLAYER: candidates farther than this from their world spawn are skipped (0 — no limit)
     * @param minPlaytimeTicks NEAR_PLAYER: minimal total play time of the chosen player
     * @param disabledWorlds   NEAR_PLAYER: lower-case worlds whose players are never chosen
     */
    public record RtpMode(String id, ModeType type, String permission, String world, Center center,
                          double centerX, double centerZ, int minRadius, int maxRadius, int playerMaxDistance,
                          long minPlaytimeTicks, Set<String> disabledWorlds, long cooldownMillis, MenuItem icon) {
    }

    /**
     * Texts for {@code <status>} in mode icons are MiniMessage; {@code <time>} is the remaining cooldown.
     */
    public record GuiSettings(String title, int rows, Material filler, MenuItem close,
                              String statusAvailable, String statusCooldown, String statusNoPermission) {
    }

    public static RtpSettings load(ConfigurationSection s) {
        int attempts = (int) ConfigUtil.nonNegative(s, "max-attempts", 10);
        if (attempts < 1 || attempts > 50) {
            throw new IllegalArgumentException(ConfigUtil.path(s, "max-attempts") + " must be between 1 and 50");
        }

        Set<Material> unsafe = EnumSet.noneOf(Material.class);
        for (String raw : s.getStringList("unsafe-blocks")) {
            Material material = Material.matchMaterial(raw.trim());
            if (material == null || !material.isBlock()) {
                throw new IllegalArgumentException(ConfigUtil.path(s, "unsafe-blocks") + ": unknown block '" + raw + "'");
            }
            unsafe.add(material);
        }

        Registry<Biome> biomes = RegistryAccess.registryAccess().getRegistry(RegistryKey.BIOME);
        Set<NamespacedKey> blocked = s.getStringList("blocked-biomes").stream().map(raw -> {
            NamespacedKey key = NamespacedKey.fromString(raw.trim().toLowerCase(Locale.ROOT));
            if (key == null || biomes.get(key) == null) {
                throw new IllegalArgumentException(ConfigUtil.path(s, "blocked-biomes") + ": unknown biome '" + raw + "'");
            }
            return key;
        }).collect(Collectors.toUnmodifiableSet());

        ConfigurationSection guiSection = ConfigUtil.section(s, "gui");
        int rows = ConfigUtil.rows(guiSection, "rows", 3);
        String fillerRaw = guiSection.getString("filler", "");
        GuiSettings gui = new GuiSettings(
                ConfigUtil.requireText(guiSection, "title"),
                rows,
                fillerRaw == null || fillerRaw.isBlank() ? null : ConfigUtil.material(fillerRaw, ConfigUtil.path(guiSection, "filler")),
                MenuItem.load(guiSection, "close", rows, true),
                guiSection.getString("status.available", ""),
                guiSection.getString("status.cooldown", ""),
                guiSection.getString("status.no-permission", "")
        );

        ConfigurationSection modesSection = ConfigUtil.section(s, "modes");
        Map<String, RtpMode> modes = new LinkedHashMap<>();
        for (String id : modesSection.getKeys(false)) {
            ConfigurationSection mode = ConfigUtil.section(modesSection, id);
            if (!mode.getBoolean("enabled", true)) {
                continue;
            }
            String normalized = id.toLowerCase(Locale.ROOT);
            if (!MODE_ID.matcher(normalized).matches()) {
                throw new IllegalArgumentException(ConfigUtil.path(modesSection, id) + ": mode id must match " + MODE_ID.pattern());
            }
            modes.put(normalized, loadMode(normalized, mode, rows));
        }
        if (modes.isEmpty()) {
            throw new IllegalArgumentException(ConfigUtil.path(s, "modes") + " must contain at least one enabled mode");
        }
        // Insertion order is kept: it is the order of modes in tab completion.
        return new RtpSettings(attempts, Set.copyOf(unsafe), blocked, gui, Collections.unmodifiableMap(modes));
    }

    private static RtpMode loadMode(String id, ConfigurationSection s, int rows) {
        ModeType type = ConfigUtil.enumValue(ModeType.class, s.getString("type", "RANDOM"), ConfigUtil.path(s, "type"));
        String world = s.getString("world", "");
        if (type == ModeType.RANDOM && (world == null || world.isBlank())) {
            throw new IllegalArgumentException("'" + ConfigUtil.path(s, "world") + "' must not be empty");
        }
        int min = (int) ConfigUtil.nonNegative(s, "min-radius", 0);
        int max = (int) ConfigUtil.nonNegative(s, "max-radius", 1000);
        if (max < 1 || min >= max) {
            throw new IllegalArgumentException(ConfigUtil.path(s, "max-radius") + " must be greater than min-radius");
        }
        List<String> disabled = s.getStringList("disabled-worlds");
        return new RtpMode(
                id,
                type,
                s.getString("permission", ""),
                world == null ? "" : world.trim(),
                ConfigUtil.enumValue(Center.class, s.getString("center", "WORLD_SPAWN"), ConfigUtil.path(s, "center")),
                s.getDouble("center-x", 0D),
                s.getDouble("center-z", 0D),
                min,
                max,
                (int) ConfigUtil.nonNegative(s, "player-max-distance", 0),
                ConfigUtil.nonNegative(s, "target-min-playtime-minutes", 0) * 60L * 20L,
                disabled.stream().map(name -> name.toLowerCase(Locale.ROOT)).collect(Collectors.toUnmodifiableSet()),
                ConfigUtil.nonNegative(s, "cooldown-seconds", 0) * 1000L,
                MenuItem.load(s, "icon", rows, true)
        );
    }
}
