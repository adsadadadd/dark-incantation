package me.x1no4ekk.core.util;

import java.util.concurrent.TimeUnit;

/**
 * Human-readable durations in Russian: {@code 1 д. 2 ч. 3 мин. 4 сек.}
 */
public final class Durations {

    private Durations() {
    }

    public static String format(long millis) {
        long totalSeconds = Math.max(0L, (millis + 999L) / 1000L);
        if (totalSeconds == 0L) {
            return "0 сек.";
        }
        long days = totalSeconds / 86_400L;
        long hours = totalSeconds % 86_400L / 3_600L;
        long minutes = totalSeconds % 3_600L / 60L;
        long seconds = totalSeconds % 60L;
        StringBuilder out = new StringBuilder();
        append(out, days, "д.");
        append(out, hours, "ч.");
        append(out, minutes, "мин.");
        append(out, seconds, "сек.");
        return out.toString();
    }

    public static String formatSeconds(long seconds) {
        return format(TimeUnit.SECONDS.toMillis(seconds));
    }

    private static void append(StringBuilder out, long value, String unit) {
        if (value > 0) {
            if (!out.isEmpty()) {
                out.append(' ');
            }
            out.append(value).append(' ').append(unit);
        }
    }
}
