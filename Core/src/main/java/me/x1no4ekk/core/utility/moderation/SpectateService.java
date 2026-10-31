package me.x1no4ekk.core.utility.moderation;

import me.x1no4ekk.core.CorePlugin;
import me.x1no4ekk.core.util.Locations;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

/**
 * {@code /spectate} sessions: spectator mode near a target, then a return to the saved position and game mode.
 * Main thread only.
 */
final class SpectateService {

    /**
     * @param location  serialized position before spectating
     * @param returning the teleport back is in flight; the session ends when it completes
     */
    record Session(String location, GameMode gameMode, UUID target, boolean returning) {
    }

    private final CorePlugin plugin;
    private final ModerationModule module;
    private final Map<UUID, Session> sessions = new HashMap<>();

    SpectateService(CorePlugin plugin, ModerationModule module) {
        this.plugin = plugin;
        this.module = module;
    }

    /**
     * @return the spectated player, or {@code null} if not spectating
     */
    UUID targetOf(UUID spectator) {
        Session session = sessions.get(spectator);
        return session == null || session.returning() ? null : session.target();
    }

    boolean isReturning(UUID spectator) {
        Session session = sessions.get(spectator);
        return session != null && session.returning();
    }

    /**
     * Starts or retargets a session; the original position and game mode are kept when retargeting.
     */
    void start(Player spectator, Player target) {
        UUID id = spectator.getUniqueId();
        UUID targetId = target.getUniqueId();
        Session previous = sessions.get(id);
        Session session = previous != null
                ? new Session(previous.location(), previous.gameMode(), targetId, false)
                : new Session(Locations.serialize(spectator.getLocation()), spectator.getGameMode(), targetId, false);
        sessions.put(id, session);
        if (spectator.getSpectatorTarget() != null) {
            spectator.setSpectatorTarget(null);
        }
        spectator.setGameMode(GameMode.SPECTATOR);
        spectator.teleportAsync(target.getLocation(), TeleportCause.PLUGIN).thenAcceptAsync(success -> {
            if (!success || sessions.get(id) != session || !module.settings().spectate().attachCamera()) {
                return;
            }
            Player viewer = Bukkit.getPlayer(id);
            Player watched = Bukkit.getPlayer(targetId);
            if (viewer != null && watched != null && viewer.getGameMode() == GameMode.SPECTATOR
                    && viewer.getWorld().equals(watched.getWorld())) {
                viewer.setSpectatorTarget(watched);
            }
        }, plugin.mainThread());
    }

    /**
     * Ends a session with an asynchronous teleport back; the game mode is restored once the teleport completes.
     *
     * @return {@code false} if the player was not spectating
     */
    boolean stop(Player spectator, String messageKey) {
        UUID id = spectator.getUniqueId();
        Session session = sessions.get(id);
        if (session == null || session.returning()) {
            return false;
        }
        Session returning = new Session(session.location(), session.gameMode(), session.target(), true);
        sessions.put(id, returning);
        if (spectator.getSpectatorTarget() != null) {
            spectator.setSpectatorTarget(null);
        }
        spectator.teleportAsync(origin(session), TeleportCause.PLUGIN).whenCompleteAsync((success, error) -> {
            if (error != null) {
                plugin.getLogger().log(Level.WARNING, "Could not return spectator " + id, error);
            }
            if (sessions.get(id) != returning) {
                return; // already restored on quit/disable
            }
            sessions.remove(id);
            Player player = Bukkit.getPlayer(id);
            if (player != null) {
                restoreGameMode(player, session);
            }
        }, plugin.mainThread());
        plugin.messages().send(spectator, messageKey);
        return true;
    }

    /**
     * Restores the player immediately (quit, module disable): the position must be set before the player data
     * is saved, so this cannot wait for an asynchronous teleport.
     */
    void restoreNow(Player spectator) {
        Session session = sessions.remove(spectator.getUniqueId());
        if (session == null) {
            return;
        }
        if (spectator.getSpectatorTarget() != null) {
            spectator.setSpectatorTarget(null);
        }
        spectator.teleport(origin(session), TeleportCause.PLUGIN);
        restoreGameMode(spectator, session);
    }

    /**
     * Quit of a player: restores them if spectating and ends the sessions that watched them.
     */
    void handleQuit(Player player) {
        restoreNow(player);
        UUID quitting = player.getUniqueId();
        for (UUID spectatorId : spectatorsOf(quitting)) {
            Player spectator = Bukkit.getPlayer(spectatorId);
            if (spectator != null) {
                stop(spectator, "utility.spectate.target-left");
            }
        }
    }

    /**
     * Keeps spectators near their targets; ends sessions whose target left or whose spectator changed game mode.
     */
    void tick() {
        if (sessions.isEmpty()) {
            return;
        }
        double maxDistance = module.settings().spectate().maxDistance();
        for (Map.Entry<UUID, Session> entry : List.copyOf(sessions.entrySet())) {
            Session session = entry.getValue();
            if (session.returning()) {
                continue;
            }
            Player spectator = Bukkit.getPlayer(entry.getKey());
            if (spectator == null) {
                sessions.remove(entry.getKey());
                continue;
            }
            Player target = Bukkit.getPlayer(session.target());
            if (target == null) {
                stop(spectator, "utility.spectate.target-left");
                continue;
            }
            if (spectator.getGameMode() != GameMode.SPECTATOR) {
                stop(spectator, "utility.spectate.stopped");
                continue;
            }
            if (spectator.getSpectatorTarget() != null) {
                continue;
            }
            Location targetLocation = target.getLocation();
            Location spectatorLocation = spectator.getLocation();
            boolean otherWorld = !targetLocation.getWorld().equals(spectatorLocation.getWorld());
            if (otherWorld || (maxDistance > 0
                    && targetLocation.distanceSquared(spectatorLocation) > maxDistance * maxDistance)) {
                spectator.teleportAsync(targetLocation, TeleportCause.PLUGIN);
            }
        }
    }

    /**
     * Restores every spectator (module disable).
     */
    void restoreAll() {
        for (UUID id : List.copyOf(sessions.keySet())) {
            Player player = Bukkit.getPlayer(id);
            if (player != null) {
                restoreNow(player);
            }
        }
        sessions.clear();
    }

    private List<UUID> spectatorsOf(UUID target) {
        List<UUID> result = new ArrayList<>();
        for (Map.Entry<UUID, Session> entry : sessions.entrySet()) {
            if (!entry.getValue().returning() && entry.getValue().target().equals(target)) {
                result.add(entry.getKey());
            }
        }
        return result;
    }

    private static Location origin(Session session) {
        Location location = Locations.deserialize(session.location());
        return location != null ? location : Bukkit.getWorlds().getFirst().getSpawnLocation();
    }

    /**
     * Only a player still in spectator mode gets the old mode back; a manual game mode change is kept.
     */
    private static void restoreGameMode(Player player, Session session) {
        if (player.getGameMode() == GameMode.SPECTATOR && session.gameMode() != GameMode.SPECTATOR) {
            player.setGameMode(session.gameMode());
        }
    }
}
