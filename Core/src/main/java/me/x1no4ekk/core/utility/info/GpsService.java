package me.x1no4ekk.core.utility.info;

import me.x1no4ekk.core.CorePlugin;
import me.x1no4ekk.core.util.Locations;
import me.x1no4ekk.core.util.Numbers;
import me.x1no4ekk.core.utility.UtilityText;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * {@code /gps} navigation: points the compass at a position or a player and shows direction and distance
 * in the action bar. Main thread only.
 */
final class GpsService {

    /**
     * @param tracked         followed player, or {@code null} for fixed coordinates
     * @param previousCompass serialized compass target to restore when navigation ends
     */
    record Target(String world, double x, double z, UUID tracked, String label, String previousCompass) {
    }

    private final CorePlugin plugin;
    private final InfoModule module;
    private final Map<UUID, Target> targets = new HashMap<>();

    GpsService(CorePlugin plugin, InfoModule module) {
        this.plugin = plugin;
        this.module = module;
    }

    boolean isActive(UUID owner) {
        return targets.containsKey(owner);
    }

    void start(Player owner, String world, double x, double z, UUID tracked, String label) {
        UUID id = owner.getUniqueId();
        Target previous = targets.get(id);
        String compass = previous != null ? previous.previousCompass() : Locations.serialize(owner.getCompassTarget());
        Target target = new Target(world, x, z, tracked, label, compass);
        targets.put(id, target);
        update(owner, target);
    }

    /**
     * @param messageKey message for the owner, or {@code null}
     * @return {@code false} if navigation was not active
     */
    boolean stop(Player owner, String messageKey) {
        Target target = targets.remove(owner.getUniqueId());
        if (target == null) {
            return false;
        }
        restoreCompass(owner, target);
        if (messageKey != null) {
            plugin.messages().send(owner, messageKey);
        }
        return true;
    }

    void handleQuit(Player player) {
        stop(player, null);
        UUID leaving = player.getUniqueId();
        List<UUID> followers = new ArrayList<>();
        targets.forEach((owner, target) -> {
            if (leaving.equals(target.tracked())) {
                followers.add(owner);
            }
        });
        for (UUID followerId : followers) {
            Player follower = Bukkit.getPlayer(followerId);
            if (follower != null) {
                stop(follower, "utility.gps.target-lost");
            }
        }
    }

    void tick() {
        for (Map.Entry<UUID, Target> entry : List.copyOf(targets.entrySet())) {
            Player owner = Bukkit.getPlayer(entry.getKey());
            if (owner == null) {
                targets.remove(entry.getKey());
            } else {
                update(owner, entry.getValue());
            }
        }
    }

    /**
     * Restores every compass (module disable).
     */
    void clearAll() {
        for (Map.Entry<UUID, Target> entry : List.copyOf(targets.entrySet())) {
            Player owner = Bukkit.getPlayer(entry.getKey());
            if (owner != null) {
                restoreCompass(owner, entry.getValue());
            }
        }
        targets.clear();
    }

    private void update(Player owner, Target target) {
        String world;
        double x;
        double z;
        if (target.tracked() != null) {
            Player tracked = Bukkit.getPlayer(target.tracked());
            if (tracked == null || !owner.canSee(tracked)) {
                stop(owner, "utility.gps.target-lost");
                return;
            }
            Location location = tracked.getLocation();
            world = location.getWorld().getName();
            x = location.getX();
            z = location.getZ();
        } else {
            world = target.world();
            x = target.x();
            z = target.z();
        }
        Location from = owner.getLocation();
        if (!from.getWorld().getName().equals(world)) {
            plugin.messages().sendActionBar(owner, "utility.gps.other-world",
                    Placeholder.unparsed("world", world), Placeholder.unparsed("target", target.label()));
            return;
        }
        double dx = x - from.getX();
        double dz = z - from.getZ();
        double distance = Math.sqrt(dx * dx + dz * dz);
        int arrive = module.settings().gpsArriveDistance();
        if (target.tracked() == null && arrive > 0 && distance <= arrive) {
            stop(owner, "utility.gps.arrived");
            return;
        }
        owner.setCompassTarget(new Location(from.getWorld(), x, from.getY(), z));
        plugin.messages().sendActionBar(owner, "utility.gps.action-bar",
                Placeholder.unparsed("direction", UtilityText.arrow(from, x, z)),
                Placeholder.unparsed("distance", Numbers.format(Math.round(distance))),
                Placeholder.unparsed("target", target.label()));
    }

    private static void restoreCompass(Player owner, Target target) {
        Location previous = Locations.deserialize(target.previousCompass());
        owner.setCompassTarget(previous != null ? previous : owner.getWorld().getSpawnLocation());
    }
}
