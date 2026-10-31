package me.x1no4ekk.core.kit;

/**
 * Whether a player can claim a kit for free right now.
 */
public enum KitStatus {
    AVAILABLE,
    COOLDOWN,
    /** One-time kit that was already claimed. */
    USED,
    NO_PERMISSION,
    /** The kit cannot be claimed for free at all (it may still be for sale). */
    PURCHASE_ONLY;

    /**
     * Status with the time left for {@link #COOLDOWN}.
     */
    public record State(KitStatus status, long remainingMillis) {

        static final State AVAILABLE_STATE = new State(AVAILABLE, 0L);
    }
}
