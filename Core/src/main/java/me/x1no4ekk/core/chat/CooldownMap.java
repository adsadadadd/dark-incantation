package me.x1no4ekk.core.chat;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Per-player cooldown expiry times. Thread-safe: the chat cooldown is checked on the async chat thread.
 */
public final class CooldownMap {

    private final Map<UUID, Long> expiries = new ConcurrentHashMap<>();

    /**
     * @return milliseconds left, or 0 if the cooldown is over
     */
    public long remaining(UUID uuid) {
        Long expiry = expiries.get(uuid);
        if (expiry == null) {
            return 0L;
        }
        long left = expiry - System.currentTimeMillis();
        if (left <= 0L) {
            expiries.remove(uuid, expiry);
            return 0L;
        }
        return left;
    }

    public void start(UUID uuid, long millis) {
        if (millis > 0L) {
            expiries.put(uuid, System.currentTimeMillis() + millis);
        }
    }

    public void remove(UUID uuid) {
        expiries.remove(uuid);
    }

    /**
     * Drops expired entries, which bounds the memory of maps that survive a quit.
     */
    public void purge() {
        long now = System.currentTimeMillis();
        expiries.values().removeIf(expiry -> expiry <= now);
    }

    public void clear() {
        expiries.clear();
    }
}
