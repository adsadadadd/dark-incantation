package me.x1no4ekk.core.warp;

/**
 * Aggregated grades of a warp.
 */
public record WarpRating(long sum, int count) {

    public static final WarpRating NONE = new WarpRating(0L, 0);

    public double average() {
        return count == 0 ? 0D : (double) sum / count;
    }

    /**
     * Applies a new grade, replacing {@code previous} (0 if the player had not graded before).
     */
    public WarpRating apply(int previous, int grade) {
        return previous <= 0 ? new WarpRating(sum + grade, count + 1) : new WarpRating(sum - previous + grade, count);
    }
}
