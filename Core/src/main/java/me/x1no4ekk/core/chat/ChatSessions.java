package me.x1no4ekk.core.chat;

import org.bukkit.Statistic;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Join time and playtime baseline of online players for the join cooldown and the minimal playtime.
 * The statistic is sampled on the main thread at join, so the async chat thread never reads it.
 */
public final class ChatSessions {

    private static final long MILLIS_PER_TICK = 50L;

    /**
     * @param joinedAt join time, or 0 for players that were online before the module started (no join cooldown)
     */
    private record Session(long joinedAt, long playedTicks, long sampledAt) {
    }

    private final Map<UUID, Session> sessions = new ConcurrentHashMap<>();

    /**
     * Main thread only.
     */
    public void start(Player player, boolean freshJoin) {
        long now = System.currentTimeMillis();
        sessions.put(player.getUniqueId(), new Session(freshJoin ? now : 0L, player.getStatistic(Statistic.PLAY_ONE_MINUTE), now));
    }

    public void end(UUID uuid) {
        sessions.remove(uuid);
    }

    public void clear() {
        sessions.clear();
    }

    /**
     * @return milliseconds until the player may chat after joining, or 0
     */
    public long joinCooldownLeft(UUID uuid, long cooldownMillis) {
        Session session = sessions.get(uuid);
        if (session == null || cooldownMillis <= 0L || session.joinedAt() == 0L) {
            return 0L;
        }
        return Math.max(0L, session.joinedAt() + cooldownMillis - System.currentTimeMillis());
    }

    /**
     * @return milliseconds of playtime still missing, or 0
     */
    public long playtimeLeft(UUID uuid, long requiredTicks) {
        Session session = sessions.get(uuid);
        if (session == null || requiredTicks <= 0L) {
            return 0L;
        }
        long played = session.playedTicks() + (System.currentTimeMillis() - session.sampledAt()) / MILLIS_PER_TICK;
        return Math.max(0L, (requiredTicks - played) * MILLIS_PER_TICK);
    }
}
