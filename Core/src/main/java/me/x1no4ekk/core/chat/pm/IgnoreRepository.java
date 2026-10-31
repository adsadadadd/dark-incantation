package me.x1no4ekk.core.chat.pm;

import me.x1no4ekk.core.database.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Asynchronous access to core_ignores (the cache lives in {@link me.x1no4ekk.core.user.User}).
 */
public final class IgnoreRepository {

    private static final String INSERT = "INSERT IGNORE INTO core_ignores (uuid, ignored_uuid) VALUES (?, ?)";
    private static final String DELETE = "DELETE FROM core_ignores WHERE uuid = ? AND ignored_uuid = ?";

    private final DatabaseManager database;

    public IgnoreRepository(DatabaseManager database) {
        this.database = database;
    }

    public CompletableFuture<Void> add(UUID owner, UUID ignored) {
        return database.update(connection -> write(connection, INSERT, owner, ignored));
    }

    public CompletableFuture<Void> remove(UUID owner, UUID ignored) {
        return database.update(connection -> write(connection, DELETE, owner, ignored));
    }

    /**
     * Last known names of the given players from core_users.
     */
    public CompletableFuture<Map<UUID, String>> names(Collection<UUID> uuids) {
        List<UUID> ids = List.copyOf(uuids);
        if (ids.isEmpty()) {
            return CompletableFuture.completedFuture(Map.of());
        }
        String sql = "SELECT uuid, name FROM core_users WHERE uuid IN ("
                + String.join(",", Collections.nCopies(ids.size(), "?")) + ")";
        return database.query(connection -> {
            Map<UUID, String> result = new HashMap<>();
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                for (int i = 0; i < ids.size(); i++) {
                    statement.setString(i + 1, ids.get(i).toString());
                }
                try (ResultSet rs = statement.executeQuery()) {
                    while (rs.next()) {
                        result.put(UUID.fromString(rs.getString("uuid")), rs.getString("name"));
                    }
                }
            }
            return result;
        });
    }

    private static void write(Connection connection, String sql, UUID owner, UUID ignored) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, owner.toString());
            statement.setString(2, ignored.toString());
            statement.executeUpdate();
        }
    }
}
