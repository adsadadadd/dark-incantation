package me.x1no4ekk.core.travel;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Temporary item-pickup ban after /back (old {@code backBlock}/{@code backTime}). Main thread only.
 */
final class PickupGuard {

    private static final long NOTICE_INTERVAL_MILLIS = 1000L;

    private final Map<UUID, Long> blockedUntil = new HashMap<>();
    private final Map<UUID, Long> lastNotice = new HashMap<>();

    void block(UUID uuid, long millis) {
        blockedUntil.put(uuid, System.currentTimeMillis() + millis);
    }

    /**
     * @return remaining ban in milliseconds, or 0
     */
    long remaining(UUID uuid) {
        Long until = blockedUntil.get(uuid);
        if (until == null) {
            return 0L;
        }
        long left = until - System.currentTimeMillis();
        if (left <= 0) {
            blockedUntil.remove(uuid);
            lastNotice.remove(uuid);
            return 0L;
        }
        return left;
    }

    /**
     * Throttles the "pickup blocked" message to one per second.
     */
    boolean shouldNotify(UUID uuid) {
        long now = System.currentTimeMillis();
        Long last = lastNotice.get(uuid);
        if (last != null && now - last < NOTICE_INTERVAL_MILLIS) {
            return false;
        }
        lastNotice.put(uuid, now);
        return true;
    }

    void remove(UUID uuid) {
        blockedUntil.remove(uuid);
        lastNotice.remove(uuid);
    }

    void purge() {
        long now = System.currentTimeMillis();
        blockedUntil.values().removeIf(until -> until <= now);
        lastNotice.keySet().retainAll(blockedUntil.keySet());
    }

    void clear() {
        blockedUntil.clear();
        lastNotice.clear();
    }
}
