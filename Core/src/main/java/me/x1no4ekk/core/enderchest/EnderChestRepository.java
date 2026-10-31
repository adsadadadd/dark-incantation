package me.x1no4ekk.core.enderchest;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Blocking JDBC access to core_enderchests (one row per non-empty page, {@code page} starts at 0) and
 * core_enderchest_meta (purchased pages).
 */
final class EnderChestRepository {

    private static final String CREATE_META = """
            CREATE TABLE IF NOT EXISTS core_enderchest_meta (
                uuid          CHAR(36) CHARACTER SET ascii NOT NULL,
                bought_pages  INT NOT NULL DEFAULT 0,
                PRIMARY KEY (uuid)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci""";
    private static final String SELECT_PAGES = "SELECT page, contents FROM core_enderchests WHERE uuid = ?";
    private static final String SELECT_META = "SELECT bought_pages FROM core_enderchest_meta WHERE uuid = ?";
    private static final String UPSERT_PAGE =
            "INSERT INTO core_enderchests (uuid, page, contents) VALUES (?, ?, ?) "
                    + "ON DUPLICATE KEY UPDATE contents = VALUES(contents)";
    private static final String DELETE_PAGE = "DELETE FROM core_enderchests WHERE uuid = ? AND page = ?";
    private static final String UPSERT_META =
            "INSERT INTO core_enderchest_meta (uuid, bought_pages) VALUES (?, ?) "
                    + "ON DUPLICATE KEY UPDATE bought_pages = VALUES(bought_pages)";

    /**
     * Stored chest of one player.
     *
     * @param pages page index → serialized items ({@link org.bukkit.inventory.ItemStack#serializeItemsAsBytes})
     */
    record StoredChest(Map<Integer, byte[]> pages, int boughtPages) {
    }

    void createSchema(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute(CREATE_META);
        }
    }

    StoredChest load(Connection connection, UUID owner) throws SQLException {
        Map<Integer, byte[]> pages = new HashMap<>();
        try (PreparedStatement statement = connection.prepareStatement(SELECT_PAGES)) {
            statement.setString(1, owner.toString());
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    pages.put(rs.getInt("page"), rs.getBytes("contents"));
                }
            }
        }
        int bought = 0;
        try (PreparedStatement statement = connection.prepareStatement(SELECT_META)) {
            statement.setString(1, owner.toString());
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    bought = Math.max(0, rs.getInt("bought_pages"));
                }
            }
        }
        return new StoredChest(pages, bought);
    }

    /**
     * Stores a page; {@code null} contents delete it (empty page).
     */
    void savePage(Connection connection, UUID owner, int page, byte[] contents) throws SQLException {
        if (contents == null) {
            try (PreparedStatement statement = connection.prepareStatement(DELETE_PAGE)) {
                statement.setString(1, owner.toString());
                statement.setInt(2, page);
                statement.executeUpdate();
            }
            return;
        }
        try (PreparedStatement statement = connection.prepareStatement(UPSERT_PAGE)) {
            statement.setString(1, owner.toString());
            statement.setInt(2, page);
            statement.setBytes(3, contents);
            statement.executeUpdate();
        }
    }

    void saveBoughtPages(Connection connection, UUID owner, int boughtPages) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(UPSERT_META)) {
            statement.setString(1, owner.toString());
            statement.setInt(2, boughtPages);
            statement.executeUpdate();
        }
    }
}
