package me.x1no4ekk.core.chat.pm;

import me.x1no4ekk.core.chat.ChatYaml;
import me.x1no4ekk.core.config.ConfigUtil;
import me.x1no4ekk.core.config.SoundEffect;
import org.bukkit.configuration.ConfigurationSection;

/**
 * {@code private-messages} section of chat.yml.
 */
public record PrivateMessageSettings(
        String senderFormat,
        String receiverFormat,
        String socialSpyFormat,
        String targetSpyFormat,
        String senderHover,
        String receiverHover,
        boolean clickToReply,
        boolean legacyColors,
        long cooldownMillis,
        boolean logToConsole,
        SoundEffect sound,
        int ignoreLimit
) {

    public static PrivateMessageSettings load(ConfigurationSection root) {
        ConfigurationSection s = ConfigUtil.section(root, "private-messages");
        ConfigurationSection formats = ConfigUtil.section(s, "formats");
        ConfigurationSection hover = ConfigUtil.sectionOrEmpty(s, "hover");
        ConfigurationSection chat = ConfigUtil.sectionOrEmpty(root, "chat");
        return new PrivateMessageSettings(
                ConfigUtil.requireText(formats, "sender"),
                ConfigUtil.requireText(formats, "receiver"),
                ConfigUtil.requireText(formats, "social-spy"),
                ConfigUtil.requireText(formats, "target-spy"),
                ChatYaml.text(hover, "sender"),
                ChatYaml.text(hover, "receiver"),
                s.getBoolean("click-to-reply", true),
                chat.getBoolean("legacy-colors", true),
                ChatYaml.secondsToMillis(s, "cooldown", 3),
                s.getBoolean("log-to-console", true),
                SoundEffect.load(s, "sound"),
                (int) Math.min(Integer.MAX_VALUE, ConfigUtil.nonNegative(s, "ignore-limit", 100)));
    }
}
