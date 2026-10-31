package me.x1no4ekk.core.warp;

import org.bukkit.Material;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * SQL for {@code core_warps} and {@code core_warp_grades}. Blocking; database threads only.
 */
final class WarpRepository {

    private static final String SELECT_ALL =
            "SELECT w.name, w.owner_uuid, u.name AS owner_name, w.system_warp, w.world, w.x, w.y, w.z, w.yaw, w.pitch, "
                    + "w.icon, w.description, w.visits, w.created_at, w.slot "
                    + "FROM core_warps w LEFT JOIN core_users u ON u.uuid = w.owner_uuid";
    private static final String SELECT_RATINGS =
            "SELECT warp, SUM(grade) AS total, COUNT(*) AS votes FROM core_warp_grades GROUP BY warp";
    private static final String INSERT =
            "INSERT INTO core_warps (name, owner_uuid, system_warp, world, x, y, z, yaw, pitch, icon, description, visits, created_at, slot) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0, ?, ?)";
    private static final String DELETE_GRADES = "DELETE FROM core_warp_grades WHERE warp = ?";
    private static final String DELETE = "DELETE FROM core_warps WHERE name = ?";
    private static final String UPDATE_DETAILS =
            "UPDATE core_warps SET world = ?, x = ?, y = ?, z = ?, yaw = ?, pitch = ?, icon = ?, description = ?, slot = ? WHERE name = ?";
    private static final String INCREMENT_VISITS = "UPDATE core_warps SET visits = visits + 1 WHERE name = ?";
    private static final String EXISTS = "SELECT 1 FROM core_warps WHERE name = ?";
    private static final String SELECT_GRADE =
            "SELECT grade, graded_at FROM core_warp_grades WHERE warp = ? AND uuid = ? FOR UPDATE";
    private static final String UPSERT_GRADE =
            "INSERT INTO core_warp_grades (warp, uuid, grade, graded_at) VALUES (?, ?, ?, ?) "
                    + "ON DUPLICATE KEY UPDATE grade = VALUES(grade), graded_at = VALUES(graded_at)";

    private WarpRepository() {
    }

    record Snapshot(List<Warp> warps, Map<String, WarpRating> ratings) {
    }

    enum GradeStatus {
        GRADED, COOLDOWN, NOT_FOUND
    }

    /**
     * @param previous previous grade of the player, 0 if none
     * @param remainingMillis cooldown left for {@link GradeStatus#COOLDOWN}
     */
    record GradeOutcome(GradeStatus status, int previous, long remainingMillis) {
    }

    static Snapshot loadAll(Connection connection) throws SQLException {
        List<Warp> warps = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(SELECT_ALL);
             ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                String owner = rs.getString("owner_uuid");
                String icon = rs.getString("icon");
                Material material = icon == null ? null : Material.matchMaterial(icon);
                int slot = rs.getInt("slot");
                if (rs.wasNull()) {
                    slot = -1;
                }
                warps.add(new Warp(
                        rs.getString("name"),
                        owner == null ? null : UUID.fromString(owner),
                        rs.getString("owner_name"),
                        rs.getBoolean("system_warp"),
                        rs.getString("world"),
                        rs.getDouble("x"),
                        rs.getDouble("y"),
                        rs.getDouble("z"),
                        rs.getFloat("yaw"),
                        rs.getFloat("pitch"),
                        material == null || !material.isItem() ? null : material,
                        rs.getString("description"),
                        rs.getInt("visits"),
                        rs.getLong("created_at"),
                        slot));
            }
        }
        Map<String, WarpRating> ratings = new HashMap<>();
        try (PreparedStatement statement = connection.prepareStatement(SELECT_RATINGS);
             ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                ratings.put(Warp.key(rs.getString("warp")), new WarpRating(rs.getLong("total"), rs.getInt("votes")));
            }
        }
        return new Snapshot(warps, ratings);
    }

    static void insert(Connection connection, Warp warp) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(INSERT)) {
            statement.setString(1, warp.name());
            if (warp.owner() == null) {
                statement.setNull(2, Types.CHAR);
            } else {
                statement.setString(2, warp.owner().toString());
            }
            statement.setBoolean(3, warp.system());
            statement.setString(4, warp.world());
            statement.setDouble(5, warp.x());
            statement.setDouble(6, warp.y());
            statement.setDouble(7, warp.z());
            statement.setFloat(8, warp.yaw());
            statement.setFloat(9, warp.pitch());
            setIcon(statement, 10, warp.icon());
            statement.setString(11, warp.description());
            statement.setLong(12, warp.createdAt());
            setSlot(statement, 13, warp.slot());
            statement.executeUpdate();
        }
    }

    /**
     * Deletes the warp and its grades; run inside a transaction.
     */
    static void delete(Connection connection, String name) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(DELETE_GRADES)) {
            statement.setString(1, name);
            statement.executeUpdate();
        }
        try (PreparedStatement statement = connection.prepareStatement(DELETE)) {
            statement.setString(1, name);
            statement.executeUpdate();
        }
    }

    /**
     * Writes the mutable fields (location, icon, description, slot) of a warp.
     */
    static void updateDetails(Connection connection, Warp warp) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(UPDATE_DETAILS)) {
            statement.setString(1, warp.world());
            statement.setDouble(2, warp.x());
            statement.setDouble(3, warp.y());
            statement.setDouble(4, warp.z());
            statement.setFloat(5, warp.yaw());
            statement.setFloat(6, warp.pitch());
            setIcon(statement, 7, warp.icon());
            statement.setString(8, warp.description());
            setSlot(statement, 9, warp.slot());
            statement.setString(10, warp.name());
            statement.executeUpdate();
        }
    }

    static void incrementVisits(Connection connection, String name) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(INCREMENT_VISITS)) {
            statement.setString(1, name);
            statement.executeUpdate();
        }
    }

    /**
     * Stores a grade unless the player re-grades this warp before the cooldown ends. Run inside a transaction:
     * the existence check and the row lock keep it consistent with a concurrent delete.
     */
    static GradeOutcome grade(Connection connection, String warp, UUID uuid, int grade, long now, long cooldownMillis)
            throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(EXISTS)) {
            statement.setString(1, warp);
            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) {
                    return new GradeOutcome(GradeStatus.NOT_FOUND, 0, 0L);
                }
            }
        }
        int previous = 0;
        try (PreparedStatement statement = connection.prepareStatement(SELECT_GRADE)) {
            statement.setString(1, warp);
            statement.setString(2, uuid.toString());
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    previous = rs.getInt("grade");
                    long left = rs.getLong("graded_at") + cooldownMillis - now;
                    if (left > 0) {
                        return new GradeOutcome(GradeStatus.COOLDOWN, previous, left);
                    }
                }
            }
        }
        try (PreparedStatement statement = connection.prepareStatement(UPSERT_GRADE)) {
            statement.setString(1, warp);
            statement.setString(2, uuid.toString());
            statement.setInt(3, grade);
            statement.setLong(4, now);
            statement.executeUpdate();
        }
        return new GradeOutcome(GradeStatus.GRADED, previous, 0L);
    }

    private static void setIcon(PreparedStatement statement, int index, Material icon) throws SQLException {
        if (icon == null) {
            statement.setNull(index, Types.VARCHAR);
        } else {
            statement.setString(index, icon.name());
        }
    }

    private static void setSlot(PreparedStatement statement, int index, int slot) throws SQLException {
        if (slot < 0) {
            statement.setNull(index, Types.INTEGER);
        } else {
            statement.setInt(index, slot);
        }
    }
}
