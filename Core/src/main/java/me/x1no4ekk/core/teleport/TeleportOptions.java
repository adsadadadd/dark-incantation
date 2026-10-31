package me.x1no4ekk.core.teleport;

import org.bukkit.event.player.PlayerTeleportEvent;

/**
 * How a teleport through {@link TeleportService} behaves.
 *
 * @param warmup      wait {@code teleport.warmup-seconds} standing still first (skipped with {@code core.teleport.bypass-warmup})
 * @param checkCombat refuse while the player is in combat
 * @param recordBack  store the origin as the {@code /back} location
 * @param cause       cause passed to {@code teleportAsync}
 */
public record TeleportOptions(boolean warmup, boolean checkCombat, boolean recordBack, PlayerTeleportEvent.TeleportCause cause) {

    /**
     * Player teleports (/spawn, /warp, /tpa, /rtp, guild home): warmup, combat check, /back.
     */
    public static final TeleportOptions DEFAULT = new TeleportOptions(true, true, true, PlayerTeleportEvent.TeleportCause.COMMAND);

    /**
     * Staff teleports (/tp, /tphere, /tppos, /tpall): instant, no combat check, /back.
     */
    public static final TeleportOptions INSTANT = new TeleportOptions(false, false, true, PlayerTeleportEvent.TeleportCause.COMMAND);

    public TeleportOptions withWarmup(boolean warmup) {
        return new TeleportOptions(warmup, checkCombat, recordBack, cause);
    }

    public TeleportOptions withCheckCombat(boolean checkCombat) {
        return new TeleportOptions(warmup, checkCombat, recordBack, cause);
    }

    public TeleportOptions withRecordBack(boolean recordBack) {
        return new TeleportOptions(warmup, checkCombat, recordBack, cause);
    }

    public TeleportOptions withCause(PlayerTeleportEvent.TeleportCause cause) {
        return new TeleportOptions(warmup, checkCombat, recordBack, cause);
    }
}
