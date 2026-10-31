package me.x1no4ekk.core.utility.moderation;

import me.x1no4ekk.core.config.ConfigUtil;
import me.x1no4ekk.core.utility.UtilityText;
import org.bukkit.configuration.ConfigurationSection;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * {@code utility.vanish}, {@code utility.spectate}, {@code utility.nick}, {@code utility.whois}.
 */
public record ModerationSettings(Vanish vanish, Spectate spectate, Nick nick, DateTimeFormatter whoisDates) {

    public record Vanish(int actionBarIntervalSeconds, boolean fakeMessages, boolean hideFromServerList) {
    }

    public record Spectate(double maxDistance, boolean attachCamera) {
    }

    public record Nick(int minLength, int maxLength, Pattern pattern, List<String> forbidden, String displayFormat,
                       boolean tabList, boolean checkRegisteredNames) {
    }

    public static ModerationSettings load(ConfigurationSection utility) {
        ConfigurationSection vanish = ConfigUtil.section(utility, "vanish");
        ConfigurationSection spectate = ConfigUtil.section(utility, "spectate");
        ConfigurationSection whois = ConfigUtil.section(utility, "whois");
        ConfigurationSection date = ConfigUtil.section(utility, "date");
        ZoneId zone = UtilityText.zone(date.getString("zone"), ConfigUtil.path(date, "zone"));
        return new ModerationSettings(
                new Vanish(
                        (int) Math.min(60, ConfigUtil.nonNegative(vanish, "action-bar-interval-seconds", 2)),
                        vanish.getBoolean("fake-messages", false),
                        vanish.getBoolean("hide-from-server-list", true)),
                new Spectate(
                        ConfigUtil.nonNegativeDouble(spectate, "max-distance", 10),
                        spectate.getBoolean("attach-camera", false)),
                loadNick(ConfigUtil.section(utility, "nick")),
                UtilityText.formatter(whois.getString("date-format", "dd.MM.yyyy HH:mm"), zone,
                        ConfigUtil.path(whois, "date-format")));
    }

    private static Nick loadNick(ConfigurationSection section) {
        int min = (int) ConfigUtil.nonNegative(section, "min-length", 3);
        int max = (int) ConfigUtil.nonNegative(section, "max-length", 16);
        if (min < 1 || max < min || max > 64) {
            throw new IllegalArgumentException(ConfigUtil.path(section, "min-length") + "/max-length: expected 1 <= min <= max <= 64");
        }
        String rawPattern = ConfigUtil.requireText(section, "pattern");
        Pattern pattern;
        try {
            pattern = Pattern.compile(rawPattern);
        } catch (PatternSyntaxException e) {
            throw new IllegalArgumentException(ConfigUtil.path(section, "pattern") + ": invalid regular expression: " + e.getDescription());
        }
        List<String> forbidden = new ArrayList<>();
        for (String word : section.getStringList("forbidden")) {
            if (!word.isBlank()) {
                forbidden.add(word.trim().toLowerCase(Locale.ROOT));
            }
        }
        String format = ConfigUtil.requireText(section, "display-format");
        if (!format.contains("<nickname>")) {
            throw new IllegalArgumentException(ConfigUtil.path(section, "display-format") + " must contain <nickname>");
        }
        return new Nick(min, max, pattern, List.copyOf(forbidden), format,
                section.getBoolean("tab-list", true),
                section.getBoolean("check-registered-names", true));
    }
}
