package me.x1no4ekk.core.gameplay;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * UUID → expiry map for short cooldowns. Main thread only; expired entries are purged by the module ticker.
 */
public final class Cooldowns {

    private final Map<UUID, Long> until = new HashMap<>();

    /**
     * @return milliseconds left, 0 if the cooldown is over
     */
    public long remaining(UUID uuid) {
        Long end = until.get(uuid);
        if (end == null) {
            return 0L;
        }
        long left = end - System.currentTimeMillis();
        if (left <= 0) {
            until.remove(uuid);
            return 0L;
        }
        return left;
    }

    public void start(UUID uuid, long millis) {
        if (millis > 0) {
            until.put(uuid, System.currentTimeMillis() + millis);
        }
    }

    public void remove(UUID uuid) {
        until.remove(uuid);
    }

    public void purge() {
        long now = System.currentTimeMillis();
        until.values().removeIf(end -> end <= now);
    }

    public void clear() {
        until.clear();
    }
}
