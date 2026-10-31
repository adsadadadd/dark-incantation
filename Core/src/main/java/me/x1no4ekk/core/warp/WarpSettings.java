package me.x1no4ekk.core.warp;

import me.x1no4ekk.core.config.ConfigUtil;
import me.x1no4ekk.core.config.MenuItem;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

/**
 * warps.yml: limits, grading, formats, system warp names and every warp menu layout.
 */
public record WarpSettings(
        Pattern namePattern,
        int defaultLimit,
        Map<String, Integer> limits,
        Set<String> disabledWorlds,
        int descriptionMaxLength,
        boolean countOwnerVisits,
        long gradeCooldownMillis,
        boolean gradeBroadcast,
        int nearbyRadius,
        DateTimeFormatter dateFormat,
        String ratingFormat,
        String noRating,
        String noDescription,
        String serverOwner,
        String systemNameFormat,
        Map<String, String> systemDisplayNames,
        MainGui main,
        ListGui system,
        ListGui mine,
        PlayersGui players,
        GradeGui grade
) {

    /** Main /warp menu. */
    public record MainGui(String title, int rows, Material filler, MenuItem system, MenuItem mine, MenuItem players,
                          MenuItem close) {
    }

    /** Paginated warp list. {@code entry} supplies the texts and the default icon material. */
    public record ListGui(String title, int rows, Material filler, List<Integer> contentSlots, MenuItem entry,
                          MenuItem empty, MenuItem previous, MenuItem next, MenuItem back) {
    }

    /** Players' warps: a list plus the sort button. */
    public record PlayersGui(ListGui list, boolean ownerHead, MenuItem sort, Map<WarpSort, String> sortNames,
                             WarpSort defaultSort) {
    }

    /** Grade menu; {@code grades.get(0)} is grade 1. */
    public record GradeGui(String title, int rows, Material filler, MenuItem info, List<MenuItem> grades, MenuItem back) {
    }

    public static WarpSettings load(ConfigurationSection s) {
        String rawPattern = ConfigUtil.requireText(s, "name-pattern");
        Pattern pattern;
        try {
            pattern = Pattern.compile(rawPattern);
        } catch (PatternSyntaxException e) {
            throw new IllegalArgumentException(ConfigUtil.path(s, "name-pattern") + ": invalid regular expression: " + e.getDescription());
        }

        ConfigurationSection limitsSection = ConfigUtil.sectionOrEmpty(s, "limits");
        ConfigurationSection groups = ConfigUtil.sectionOrEmpty(limitsSection, "groups");
        Map<String, Integer> limits = new HashMap<>();
        for (String group : groups.getKeys(false)) {
            limits.put(group.toLowerCase(Locale.ROOT), (int) ConfigUtil.nonNegative(groups, group, 0));
        }

        int descriptionMax = (int) ConfigUtil.nonNegative(s, "description-max-length", 100);
        if (descriptionMax < 1 || descriptionMax > Warp.MAX_DESCRIPTION_LENGTH) {
            throw new IllegalArgumentException(ConfigUtil.path(s, "description-max-length") + " must be between 1 and "
                    + Warp.MAX_DESCRIPTION_LENGTH);
        }

        ConfigurationSection grading = ConfigUtil.sectionOrEmpty(s, "grading");
        ConfigurationSection format = ConfigUtil.sectionOrEmpty(s, "format");
        DateTimeFormatter date;
        try {
            date = DateTimeFormatter.ofPattern(format.getString("date", "dd.MM.yyyy")).withZone(ZoneId.systemDefault());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(ConfigUtil.path(format, "date") + ": invalid date pattern: " + e.getMessage());
        }

        ConfigurationSection systemWarps = ConfigUtil.sectionOrEmpty(s, "system-warps");
        ConfigurationSection names = ConfigUtil.sectionOrEmpty(systemWarps, "display-names");
        Map<String, String> displayNames = new HashMap<>();
        for (String name : names.getKeys(false)) {
            displayNames.put(Warp.key(name), ConfigUtil.requireText(names, name));
        }

        ConfigurationSection gui = ConfigUtil.section(s, "gui");
        return new WarpSettings(
                pattern,
                (int) ConfigUtil.nonNegative(limitsSection, "default", 0),
                Map.copyOf(limits),
                s.getStringList("disabled-worlds").stream().map(world -> world.toLowerCase(Locale.ROOT))
                        .collect(Collectors.toUnmodifiableSet()),
                descriptionMax,
                s.getBoolean("count-owner-visits", false),
                ConfigUtil.nonNegative(grading, "cooldown-seconds", 3600) * 1000L,
                grading.getBoolean("broadcast", true),
                (int) Math.min(10_000, ConfigUtil.nonNegative(s, "nearby-radius", 100)),
                date,
                format.getString("rating", "<rating> (<votes>)"),
                format.getString("no-rating", "-"),
                format.getString("no-description", "-"),
                format.getString("server-owner", "Сервер"),
                systemWarps.getString("name-format", "<name>"),
                Map.copyOf(displayNames),
                loadMain(ConfigUtil.section(gui, "main")),
                loadList(ConfigUtil.section(gui, "system")),
                loadList(ConfigUtil.section(gui, "mine")),
                loadPlayers(ConfigUtil.section(gui, "players")),
                loadGrade(ConfigUtil.section(gui, "grade"))
        );
    }

    private static Material filler(ConfigurationSection s) {
        String raw = s.getString("filler", "");
        return raw == null || raw.isBlank() ? null : ConfigUtil.material(raw, ConfigUtil.path(s, "filler"));
    }

    private static MainGui loadMain(ConfigurationSection s) {
        int rows = ConfigUtil.rows(s, "rows", 5);
        return new MainGui(ConfigUtil.requireText(s, "title"), rows, filler(s),
                MenuItem.load(s, "system", rows, true),
                MenuItem.load(s, "mine", rows, true),
                MenuItem.load(s, "players", rows, true),
                MenuItem.load(s, "close", rows, true));
    }

    private static ListGui loadList(ConfigurationSection s) {
        int rows = ConfigUtil.rows(s, "rows", 6);
        List<Integer> content = ConfigUtil.slots(s, "content-slots", rows);
        if (content.isEmpty()) {
            throw new IllegalArgumentException("'" + ConfigUtil.path(s, "content-slots") + "' must not be empty");
        }
        return new ListGui(ConfigUtil.requireText(s, "title"), rows, filler(s), content,
                MenuItem.load(s, "warp", rows, false),
                MenuItem.load(s, "empty", rows, true),
                MenuItem.load(s, "previous-page", rows, true),
                MenuItem.load(s, "next-page", rows, true),
                MenuItem.load(s, "back", rows, true));
    }

    private static PlayersGui loadPlayers(ConfigurationSection s) {
        ListGui list = loadList(s);
        ConfigurationSection namesSection = ConfigUtil.section(s, "sort-names");
        Map<WarpSort, String> sortNames = new EnumMap<>(WarpSort.class);
        for (WarpSort sort : WarpSort.values()) {
            sortNames.put(sort, ConfigUtil.requireText(namesSection, sort.name().toLowerCase(Locale.ROOT)));
        }
        return new PlayersGui(list, s.getBoolean("owner-head", true),
                MenuItem.load(s, "sort", list.rows(), true),
                Map.copyOf(sortNames),
                ConfigUtil.enumValue(WarpSort.class, s.getString("default-sort", "RATING"), ConfigUtil.path(s, "default-sort")));
    }

    private static GradeGui loadGrade(ConfigurationSection s) {
        int rows = ConfigUtil.rows(s, "rows", 6);
        ConfigurationSection gradesSection = ConfigUtil.section(s, "grades");
        List<MenuItem> grades = new ArrayList<>(5);
        for (int grade = 1; grade <= 5; grade++) {
            grades.add(MenuItem.load(gradesSection, String.valueOf(grade), rows, true));
        }
        return new GradeGui(ConfigUtil.requireText(s, "title"), rows, filler(s),
                MenuItem.load(s, "info", rows, true),
                List.copyOf(grades),
                MenuItem.load(s, "back", rows, true));
    }
}
