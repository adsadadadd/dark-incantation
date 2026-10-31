package me.x1no4ekk.core.teleport;

/**
 * Outcome of a teleport through {@link TeleportService}.
 * The service already told the player about every outcome except {@link #SUCCESS}.
 */
public enum TeleportResult {
    /** Teleported. The caller sends its own success message. */
    SUCCESS,
    /** Moved during the warmup. */
    CANCELLED_MOVE,
    /** Took damage during the warmup. */
    CANCELLED_DAMAGE,
    /** Entered combat during the warmup. */
    CANCELLED_COMBAT,
    /** Replaced by a newer teleport or cancelled by a plugin/shutdown. */
    CANCELLED,
    /** Refused because the player is in combat. */
    IN_COMBAT,
    /** The destination disappeared (world unloaded, target player left...). */
    NO_DESTINATION,
    /** Teleport was cancelled by another plugin or failed. */
    FAILED,
    /** The player left the server. */
    OFFLINE;

    public boolean isSuccess() {
        return this == SUCCESS;
    }
}
