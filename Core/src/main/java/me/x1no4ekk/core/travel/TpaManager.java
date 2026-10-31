package me.x1no4ekk.core.travel;

import me.x1no4ekk.core.CorePlugin;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Pending /tpa and /tpahere requests. Main thread only; keyed by UUID.
 * The expiry ticker runs once per second and only while at least one request exists.
 */
public final class TpaManager {

    public enum Type {
        /** /tpa: the sender travels to the target. */
        TO_TARGET,
        /** /tpahere: the target travels to the sender. */
        TO_SENDER
    }

    public record Request(UUID sender, String senderName, UUID target, String targetName, Type type, long expiresAt) {
    }

    private final CorePlugin plugin;
    private final Supplier<TravelSettings> settings;
    /** target → (sender → request), in arrival order. */
    private final Map<UUID, LinkedHashMap<UUID, Request>> incoming = new HashMap<>();
    /** Sender → time when the next request is allowed; purged by {@link #purgeCooldowns()}. */
    private final Map<UUID, Long> cooldowns = new HashMap<>();
    private BukkitTask ticker;

    TpaManager(CorePlugin plugin, Supplier<TravelSettings> settings) {
        this.plugin = plugin;
        this.settings = settings;
    }

    public long cooldownLeft(UUID sender) {
        Long until = cooldowns.get(sender);
        return until == null ? 0L : Math.max(0L, until - System.currentTimeMillis());
    }

    public boolean hasRequest(UUID sender, UUID target) {
        Map<UUID, Request> map = incoming.get(target);
        return map != null && map.containsKey(sender);
    }

    /**
     * Stores a request, starts the sender's cooldown and the expiry ticker.
     */
    public Request add(Player sender, Player target, Type type) {
        TravelSettings config = settings.get();
        long now = System.currentTimeMillis();
        Request request = new Request(sender.getUniqueId(), sender.getName(), target.getUniqueId(), target.getName(),
                type, now + config.tpaTimeoutMillis());
        incoming.computeIfAbsent(target.getUniqueId(), key -> new LinkedHashMap<>()).put(sender.getUniqueId(), request);
        if (config.tpaCooldownMillis() > 0) {
            cooldowns.put(sender.getUniqueId(), now + config.tpaCooldownMillis());
        }
        startTicker();
        return request;
    }

    /**
     * Requests addressed to {@code target}, oldest first.
     */
    public List<Request> incoming(UUID target) {
        Map<UUID, Request> map = incoming.get(target);
        return map == null ? List.of() : List.copyOf(map.values());
    }

    /**
     * Requests sent by {@code sender}.
     */
    public List<Request> outgoing(UUID sender) {
        List<Request> result = new ArrayList<>();
        for (Map<UUID, Request> map : incoming.values()) {
            Request request = map.get(sender);
            if (request != null) {
                result.add(request);
            }
        }
        return result;
    }

    /**
     * @return the removed request, or {@code null}
     */
    public Request remove(UUID sender, UUID target) {
        Map<UUID, Request> map = incoming.get(target);
        if (map == null) {
            return null;
        }
        Request removed = map.remove(sender);
        if (map.isEmpty()) {
            incoming.remove(target);
        }
        return removed;
    }

    /**
     * Drops every request of a player who left and tells the other side.
     */
    void handleQuit(UUID uuid) {
        Map<UUID, Request> addressed = incoming.remove(uuid);
        if (addressed != null) {
            for (Request request : addressed.values()) {
                notify(request.sender(), "travel.tpa.cancelled-quit", request.targetName());
            }
        }
        for (Request request : outgoing(uuid)) {
            remove(request.sender(), request.target());
            notify(request.target(), "travel.tpa.cancelled-quit", request.senderName());
        }
    }

    void purgeCooldowns() {
        long now = System.currentTimeMillis();
        cooldowns.values().removeIf(until -> until <= now);
    }

    void clear() {
        incoming.clear();
        cooldowns.clear();
        stopTicker();
    }

    private void startTicker() {
        if (ticker == null || ticker.isCancelled()) {
            ticker = Bukkit.getScheduler().runTaskTimer(plugin, this::tick, 20L, 20L);
        }
    }

    private void stopTicker() {
        if (ticker != null) {
            ticker.cancel();
            ticker = null;
        }
    }

    private void tick() {
        long now = System.currentTimeMillis();
        Iterator<LinkedHashMap<UUID, Request>> targets = incoming.values().iterator();
        while (targets.hasNext()) {
            Map<UUID, Request> map = targets.next();
            Iterator<Request> requests = map.values().iterator();
            while (requests.hasNext()) {
                Request request = requests.next();
                if (request.expiresAt() <= now) {
                    requests.remove();
                    notify(request.sender(), "travel.tpa.expired-sender", request.targetName());
                    notify(request.target(), "travel.tpa.expired-target", request.senderName());
                }
            }
            if (map.isEmpty()) {
                targets.remove();
            }
        }
        if (incoming.isEmpty()) {
            stopTicker();
        }
    }

    private void notify(UUID uuid, String key, String otherName) {
        Player player = Bukkit.getPlayer(uuid);
        if (player != null) {
            plugin.messages().send(player, key, Placeholder.unparsed("player", otherName));
        }
    }
}
