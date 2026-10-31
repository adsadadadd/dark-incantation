package me.x1no4ekk.core.chat.format;

import me.x1no4ekk.core.chat.ChatYaml;
import me.x1no4ekk.core.config.ConfigUtil;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.configuration.ConfigurationSection;

/**
 * Hover text and click action of the sender name in chat.
 */
public record NameInteraction(boolean hover, String hoverText, ClickAction click, String clickValue) {

    public enum ClickAction {
        NONE, SUGGEST_COMMAND, RUN_COMMAND, COPY_TO_CLIPBOARD, OPEN_URL;

        public ClickEvent create(String value) {
            return switch (this) {
                case SUGGEST_COMMAND -> ClickEvent.suggestCommand(value);
                case RUN_COMMAND -> ClickEvent.runCommand(value);
                case COPY_TO_CLIPBOARD -> ClickEvent.copyToClipboard(value);
                case OPEN_URL -> ClickEvent.openUrl(value);
                case NONE -> null;
            };
        }
    }

    static NameInteraction load(ConfigurationSection chat) {
        ConfigurationSection hover = ConfigUtil.sectionOrEmpty(chat, "name-hover");
        ConfigurationSection click = ConfigUtil.sectionOrEmpty(chat, "name-click");
        ClickAction action = click.getBoolean("enabled", true)
                ? ConfigUtil.enumValue(ClickAction.class, click.getString("action", "SUGGEST_COMMAND"), ConfigUtil.path(click, "action"))
                : ClickAction.NONE;
        String hoverText = ChatYaml.text(hover, "lines");
        return new NameInteraction(hover.getBoolean("enabled", true) && !hoverText.isEmpty(), hoverText,
                action, click.getString("value", "/msg <name> "));
    }
}
