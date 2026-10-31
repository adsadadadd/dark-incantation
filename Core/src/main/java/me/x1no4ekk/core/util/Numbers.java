package me.x1no4ekk.core.util;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * Number formatting: {@code 1 234 567.5}.
 */
public final class Numbers {

    private static final ThreadLocal<DecimalFormat> FORMAT = ThreadLocal.withInitial(() -> {
        DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance(Locale.ROOT);
        symbols.setGroupingSeparator(' ');
        symbols.setDecimalSeparator('.');
        return new DecimalFormat("#,##0.##", symbols);
    });

    private Numbers() {
    }

    public static String format(double value) {
        return FORMAT.get().format(value);
    }

    public static String format(long value) {
        return FORMAT.get().format(value);
    }
}
