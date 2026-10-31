package me.x1no4ekk.core.hook;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

/**
 * Optional PlaceholderAPI support. PlaceholderAPI classes are referenced only from the nested class,
 * which is loaded only when the plugin is present.
 */
public final class PlaceholderHook {

    private static volatile boolean enabled;

    private PlaceholderHook() {
    }

    static void init() {
        enabled = Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI");
    }

    public static boolean isEnabled() {
        return enabled;
    }

    /**
     * Replaces {@code %placeholders%}. Returns the input unchanged without PlaceholderAPI.
     * The result may contain legacy colour codes; see {@link me.x1no4ekk.core.util.Text#legacyToMiniMessage}.
     */
    public static String apply(OfflinePlayer player, String text) {
        if (!enabled || text == null || text.indexOf('%') < 0) {
            return text;
        }
        return Bridge.apply(player, text);
    }

    private static final class Bridge {
        private static String apply(OfflinePlayer player, String text) {
            return me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, text);
        }
    }
}
