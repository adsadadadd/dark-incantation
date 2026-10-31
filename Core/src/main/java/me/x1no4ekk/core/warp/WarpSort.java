package me.x1no4ekk.core.warp;

import java.util.Comparator;
import java.util.function.Function;

/**
 * Orderings of the players' warps menu.
 */
public enum WarpSort {
    RATING, VISITS, NEWEST;

    public WarpSort next() {
        WarpSort[] values = values();
        return values[(ordinal() + 1) % values.length];
    }

    /**
     * @param ratings rating lookup by warp key (snapshot)
     */
    public Comparator<Warp> comparator(Function<String, WarpRating> ratings) {
        Comparator<Warp> byName = Comparator.comparing(Warp::name, String.CASE_INSENSITIVE_ORDER);
        return switch (this) {
            case RATING -> Comparator.<Warp>comparingDouble(warp -> ratings.apply(warp.key()).average()).reversed()
                    .thenComparing(Comparator.<Warp>comparingInt(warp -> ratings.apply(warp.key()).count()).reversed())
                    .thenComparing(byName);
            case VISITS -> Comparator.comparingInt(Warp::visits).reversed().thenComparing(byName);
            case NEWEST -> Comparator.comparingLong(Warp::createdAt).reversed().thenComparing(byName);
        };
    }
}
