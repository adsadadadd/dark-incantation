package me.x1no4ekk.core.travel;

import me.x1no4ekk.core.CorePlugin;
import org.bukkit.Location;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.Consumer;
import java.util.logging.Level;

/**
 * Server spawn stored in {@code core_server_locations}, cached as an immutable record.
 */
public final class SpawnManager {

    static final String SPAWN = "spawn";

    private static final String SELECT =
            "SELECT world, x, y, z, yaw, pitch FROM core_server_locations WHERE name = ?";
    private static final String UPSERT =
            "INSERT INTO core_server_locations (name, world, x, y, z, yaw, pitch) VALUES (?, ?, ?, ?, ?, ?, ?) "
                    + "ON DUPLICATE KEY UPDATE world = VALUES(world), x = VALUES(x), y = VALUES(y), z = VALUES(z), "
                    + "yaw = VALUES(yaw), pitch = VALUES(pitch)";

    private final CorePlugin plugin;
    private volatile ServerLocation spawn;

    SpawnManager(CorePlugin plugin) {
        this.plugin = plugin;
    }

    void load() {
        plugin.database().query(SpawnManager::select).whenCompleteAsync((loaded, error) -> {
            if (error != null) {
                plugin.getLogger().log(Level.SEVERE, "Failed to load the server spawn", error);
                return;
            }
            // A /setspawn issued while loading wins over the stored value.
            if (spawn == null) {
                spawn = loaded;
            }
        }, plugin.mainThread());
    }

    private static ServerLocation select(Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(SELECT)) {
            statement.setString(1, SPAWN);
            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return new ServerLocation(SPAWN, rs.getString("world"), rs.getDouble("x"), rs.getDouble("y"),
                        rs.getDouble("z"), rs.getFloat("yaw"), rs.getFloat("pitch"));
            }
        }
    }

    /**
     * @return the stored spawn point, or {@code null} if it was never set
     */
    public ServerLocation get() {
        return spawn;
    }

    /**
     * @return the spawn location, or {@code null} if it is not set or its world is not loaded
     */
    public Location location() {
        ServerLocation current = spawn;
        return current == null ? null : current.toLocation();
    }

    /**
     * Updates the cache immediately and MySQL asynchronously; the cache is rolled back if the write fails.
     *
     * @param onFailure runs on the main thread after a failed write
     */
    public void set(Location location, Consumer<Throwable> onFailure) {
        ServerLocation previous = spawn;
        ServerLocation updated = ServerLocation.of(SPAWN, location);
        spawn = updated;
        plugin.database().update(connection -> {
            try (PreparedStatement statement = connection.prepareStatement(UPSERT)) {
                statement.setString(1, updated.name());
                statement.setString(2, updated.world());
                statement.setDouble(3, updated.x());
                statement.setDouble(4, updated.y());
                statement.setDouble(5, updated.z());
                statement.setFloat(6, updated.yaw());
                statement.setFloat(7, updated.pitch());
                statement.executeUpdate();
            }
        }).whenCompleteAsync((ignored, error) -> {
            if (error != null) {
                plugin.getLogger().log(Level.SEVERE, "Failed to save the server spawn", error);
                if (spawn == updated) {
                    spawn = previous;
                }
                onFailure.accept(error);
            }
        }, plugin.mainThread());
    }
}
