package me.x1no4ekk.core.combat;

import me.x1no4ekk.core.config.ConfigUtil;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * {@code combat} section of settings.yml.
 */
public record CombatSettings(
        boolean enabled,
        int durationSeconds,
        boolean disableFly,
        boolean disableGliding,
        boolean cancelTeleport,
        boolean whitelistMode,
        List<String[]> commands,
        boolean punishEnabled,
        boolean punishKill,
        boolean punishBroadcast,
        List<String> punishCommands,
        Set<PlayerQuitEvent.QuitReason> punishReasons,
        boolean actionBar,
        Set<String> disabledWorlds
) {

    public static CombatSettings load(ConfigurationSection s) {
        ConfigurationSection commandSection = ConfigUtil.sectionOrEmpty(s, "commands");
        String mode = commandSection.getString("mode", "blacklist").toLowerCase(Locale.ROOT);
        if (!mode.equals("blacklist") && !mode.equals("whitelist")) {
            throw new IllegalArgumentException(ConfigUtil.path(commandSection, "mode") + " must be 'blacklist' or 'whitelist'");
        }
        List<String[]> commands = new ArrayList<>();
        for (String raw : commandSection.getStringList("list")) {
            String normalized = raw.trim().toLowerCase(Locale.ROOT);
            if (normalized.startsWith("/")) {
                normalized = normalized.substring(1);
            }
            if (!normalized.isEmpty()) {
                commands.add(normalized.split("\\s+"));
            }
        }

        ConfigurationSection punish = ConfigUtil.sectionOrEmpty(s, "punish-on-quit");
        Set<PlayerQuitEvent.QuitReason> reasons = EnumSet.noneOf(PlayerQuitEvent.QuitReason.class);
        List<String> rawReasons = punish.getStringList("reasons");
        if (rawReasons.isEmpty()) {
            reasons.add(PlayerQuitEvent.QuitReason.DISCONNECTED);
            reasons.add(PlayerQuitEvent.QuitReason.TIMED_OUT);
        }
        for (String raw : rawReasons) {
            reasons.add(ConfigUtil.enumValue(PlayerQuitEvent.QuitReason.class, raw, ConfigUtil.path(punish, "reasons")));
        }

        Set<String> disabledWorlds = new HashSet<>();
        for (String world : s.getStringList("disabled-worlds")) {
            disabledWorlds.add(world.toLowerCase(Locale.ROOT));
        }

        return new CombatSettings(
                s.getBoolean("enabled", true),
                (int) Math.clamp(ConfigUtil.nonNegative(s, "duration-seconds", 20), 1, 3600),
                s.getBoolean("disable-fly", true),
                s.getBoolean("disable-gliding", false),
                s.getBoolean("cancel-teleport", true),
                mode.equals("whitelist"),
                List.copyOf(commands),
                punish.getBoolean("enabled", true),
                punish.getBoolean("kill", true),
                punish.getBoolean("broadcast", true),
                List.copyOf(punish.getStringList("commands")),
                Set.copyOf(reasons),
                s.getBoolean("action-bar", true),
                Set.copyOf(disabledWorlds)
        );
    }

    /**
     * Whether the command line (without the leading slash) is blocked during combat.
     */
    public boolean isBlocked(String commandLine) {
        String[] tokens = commandLine.trim().toLowerCase(Locale.ROOT).split("\\s+");
        if (tokens.length == 0 || tokens[0].isEmpty()) {
            return false;
        }
        int colon = tokens[0].indexOf(':');
        if (colon >= 0) {
            tokens[0] = tokens[0].substring(colon + 1); // core:spawn -> spawn
        }
        boolean listed = false;
        for (String[] entry : commands) {
            if (startsWith(tokens, entry)) {
                listed = true;
                break;
            }
        }
        return whitelistMode != listed;
    }

    private static boolean startsWith(String[] tokens, String[] prefix) {
        if (prefix.length > tokens.length) {
            return false;
        }
        for (int i = 0; i < prefix.length; i++) {
            if (!tokens[i].equals(prefix[i])) {
                return false;
            }
        }
        return true;
    }
}
