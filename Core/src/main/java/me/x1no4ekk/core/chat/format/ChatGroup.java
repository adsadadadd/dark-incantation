package me.x1no4ekk.core.chat.format;

import me.x1no4ekk.core.util.Text;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Locale;

/**
 * Chat privileges of a group from chat.yml {@code chat.groups}. Legacy {@code &} codes are converted on load.
 *
 * @param prefix           MiniMessage prefix replacing the Vault prefix, or {@code null} to keep it
 * @param nameColor        opening style tags for the player name
 * @param messageColor     opening style tags for the message
 * @param additionalSuffix MiniMessage for {@code <additional_suffix>}
 */
public record ChatGroup(String name, String prefix, String nameColor, String messageColor, String additionalSuffix) {

    public static final ChatGroup NONE = new ChatGroup("", null, "", "", "");

    static ChatGroup load(String name, ConfigurationSection section) {
        String prefix = section.getString("prefix");
        return new ChatGroup(
                name.toLowerCase(Locale.ROOT),
                prefix == null || prefix.isEmpty() ? null : Text.legacyToMiniMessage(prefix),
                Text.legacyToMiniMessage(section.getString("name-color", "")),
                Text.legacyToMiniMessage(section.getString("message-color", "")),
                Text.legacyToMiniMessage(section.getString("additional-suffix", "")));
    }
}
