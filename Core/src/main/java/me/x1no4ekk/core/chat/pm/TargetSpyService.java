package me.x1no4ekk.core.chat.pm;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Who watches whose commands. Main thread only.
 */
public final class TargetSpyService {

    private final Map<UUID, Set<UUID>> spiesByTarget = new HashMap<>();

    /**
     * @return {@code true} if the spy now watches the target, {@code false} if watching stopped
     */
    public boolean toggle(UUID spy, UUID target) {
        Set<UUID> spies = spiesByTarget.computeIfAbsent(target, key -> new HashSet<>());
        if (spies.remove(spy)) {
            if (spies.isEmpty()) {
                spiesByTarget.remove(target);
            }
            return false;
        }
        spies.add(spy);
        return true;
    }

    public Set<UUID> spiesOf(UUID target) {
        Set<UUID> spies = spiesByTarget.get(target);
        return spies == null ? Set.of() : Set.copyOf(spies);
    }

    public void unwatch(UUID spy, UUID target) {
        Set<UUID> spies = spiesByTarget.get(target);
        if (spies != null && spies.remove(spy) && spies.isEmpty()) {
            spiesByTarget.remove(target);
        }
    }

    /**
     * Stops every watch of the spy.
     *
     * @return the number of targets that were watched
     */
    public int stopAll(UUID spy) {
        int stopped = 0;
        Iterator<Set<UUID>> iterator = spiesByTarget.values().iterator();
        while (iterator.hasNext()) {
            Set<UUID> spies = iterator.next();
            if (spies.remove(spy)) {
                stopped++;
                if (spies.isEmpty()) {
                    iterator.remove();
                }
            }
        }
        return stopped;
    }

    /**
     * Removes a player who left both as a target and as a spy.
     *
     * @return spies that were watching the player
     */
    public Set<UUID> quit(UUID uuid) {
        Set<UUID> spies = spiesByTarget.remove(uuid);
        stopAll(uuid);
        return spies == null ? Set.of() : spies;
    }

    public void clear() {
        spiesByTarget.clear();
    }
}
