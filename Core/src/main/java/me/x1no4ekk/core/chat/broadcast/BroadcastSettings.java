package me.x1no4ekk.core.chat.broadcast;

import me.x1no4ekk.core.chat.ChatYaml;
import me.x1no4ekk.core.config.ConfigUtil;
import me.x1no4ekk.core.config.SoundEffect;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * {@code broadcasts} section of chat.yml.
 */
public record BroadcastSettings(Map<String, BroadcastType> types, String consoleName, boolean legacyColors) {

    /** Type ids; each has the command {@code <id>broadcast} (plain {@code broadcast} for the default one). */
    public static final List<String> TYPES = List.of("broadcast", "ad", "buy", "sell", "stream", "video");

    public static BroadcastSettings load(ConfigurationSection root) {
        ConfigurationSection s = ConfigUtil.section(root, "broadcasts");
        Map<String, BroadcastType> types = new LinkedHashMap<>();
        for (String id : TYPES) {
            types.put(id, loadType(id, ConfigUtil.section(s, id)));
        }
        ConfigurationSection chat = ConfigUtil.sectionOrEmpty(root, "chat");
        return new BroadcastSettings(Collections.unmodifiableMap(types), s.getString("console-name", "<red><bold>Сервер</bold></red>"),
                chat.getBoolean("legacy-colors", true));
    }

    private static BroadcastType loadType(String id, ConfigurationSection t) {
        String format = ChatYaml.text(t, "format");
        if (format.isBlank()) {
            throw new IllegalArgumentException("'" + ConfigUtil.path(t, "format") + "' must not be empty");
        }
        List<String> links = new ArrayList<>();
        for (String link : t.getStringList("allowed-links")) {
            String domain = link.strip().toLowerCase(Locale.ROOT);
            if (!domain.isEmpty()) {
                links.add(domain);
            }
        }
        boolean linkOnly = t.getBoolean("link-only", false);
        if (linkOnly && links.isEmpty()) {
            throw new IllegalArgumentException("'" + ConfigUtil.path(t, "allowed-links") + "' must not be empty when link-only is enabled");
        }
        return new BroadcastType(
                id,
                id.equals("broadcast") ? "broadcast" : id + "broadcast",
                id.equals("broadcast") ? "core.broadcast" : "core.broadcast." + id,
                format,
                ChatYaml.secondsToMillis(t, "cooldown", 0),
                ConfigUtil.nonNegativeDouble(t, "price", 0),
                linkOnly,
                List.copyOf(links),
                SoundEffect.load(t, "sound"));
    }
}
