package me.x1no4ekk.core.utility;

import me.x1no4ekk.core.config.Messages;
import me.x1no4ekk.core.util.Text;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Text and geometry helpers shared by the utility modules.
 */
public final class UtilityText {

    public static final Locale RUSSIAN = Locale.forLanguageTag("ru");

    /** Colours and decorations only: player-supplied styled text must not carry click/hover/insertion tags. */
    private static final MiniMessage STYLE_ONLY = MiniMessage.builder()
            .tags(TagResolver.resolver(
                    StandardTags.color(),
                    StandardTags.decorations(),
                    StandardTags.gradient(),
                    StandardTags.rainbow(),
                    StandardTags.reset()))
            .build();

    private static final String[] CARDINALS = {"n", "ne", "e", "se", "s", "sw", "w", "nw"};
    private static final String[] ARROWS = {"⬆", "⬈", "➡", "⬊", "⬇", "⬋", "⬅", "⬉"};

    private UtilityText() {
    }

    /**
     * Parses player-supplied MiniMessage that may only contain colours and decorations.
     */
    public static Component styled(String input) {
        return input == null || input.isEmpty() ? Component.empty() : STYLE_ONLY.deserialize(input);
    }

    /**
     * Visible text of {@link #styled(String)} input.
     */
    public static String plainStyled(String input) {
        return Text.plain(styled(input));
    }

    /**
     * Display name of a player, or {@code utility.console-name} for the console and other senders.
     */
    public static Component senderName(Messages messages, CommandSender sender) {
        return sender instanceof Player player ? player.displayName() : messages.get("utility.console-name");
    }

    /**
     * Compass bearing in degrees (0 = north, 90 = east) for a Minecraft yaw.
     */
    public static int bearing(float yaw) {
        return Math.floorMod(Math.round(yaw + 180.0F), 360);
    }

    /**
     * Localised cardinal direction ({@code utility.direction.*}) the given yaw faces.
     */
    public static Component cardinal(Messages messages, float yaw) {
        int index = Math.round(bearing(yaw) / 45.0F) % 8;
        return messages.get("utility.direction." + CARDINALS[index]);
    }

    /**
     * Arrow showing where the point lies relative to where {@code from} is looking.
     */
    public static String arrow(Location from, double targetX, double targetZ) {
        double dx = targetX - from.getX();
        double dz = targetZ - from.getZ();
        double targetYaw = Math.toDegrees(Math.atan2(-dx, dz));
        double relative = ((targetYaw - from.getYaw()) % 360.0 + 360.0) % 360.0;
        return ARROWS[(int) Math.round(relative / 45.0) % 8];
    }

    /**
     * In-game clock ({@code HH:mm}) for a world time in ticks; tick 0 is 06:00.
     */
    public static String clock(long ticks) {
        long dayTicks = Math.floorMod(ticks, 24_000L);
        long hours = (dayTicks / 1000L + 6L) % 24L;
        long minutes = dayTicks % 1000L * 60L / 1000L;
        return String.format(Locale.ROOT, "%02d:%02d", hours, minutes);
    }

    /**
     * Localised game mode name ({@code utility.gamemode.*}).
     */
    public static Component gameMode(Messages messages, GameMode mode) {
        return messages.get("utility.gamemode." + mode.name().toLowerCase(Locale.ROOT));
    }

    /**
     * Parses a Java time zone id for configuration.
     */
    public static ZoneId zone(String raw, String path) {
        try {
            return ZoneId.of(raw == null ? "" : raw.trim());
        } catch (RuntimeException e) {
            throw new IllegalArgumentException(path + ": unknown time zone '" + raw + "' (e.g. Europe/Moscow)");
        }
    }

    /**
     * Parses a {@link DateTimeFormatter} pattern for configuration, with Russian month/day names.
     */
    public static DateTimeFormatter formatter(String raw, ZoneId zone, String path) {
        try {
            return DateTimeFormatter.ofPattern(raw == null ? "" : raw, RUSSIAN).withZone(zone);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(path + ": invalid date pattern '" + raw + "'");
        }
    }
}
