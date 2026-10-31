package me.x1no4ekk.core.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.regex.Pattern;

/**
 * Idempotent schema migrations for module tables (MySQL has no {@code ADD COLUMN IF NOT EXISTS}).
 */
public final class SchemaUtil {

    private static final Pattern IDENTIFIER = Pattern.compile("[a-z0-9_]{1,64}");

    private SchemaUtil() {
    }

    public static boolean columnExists(Connection connection, String table, String column) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ? AND COLUMN_NAME = ?")) {
            statement.setString(1, table);
            statement.setString(2, column);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    public static boolean tableExists(Connection connection, String table) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT COUNT(*) FROM information_schema.TABLES WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ?")) {
            statement.setString(1, table);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    /**
     * Adds a column if it does not exist yet.
     *
     * @param table      constant table name (validated, never user input)
     * @param column     constant column name (validated, never user input)
     * @param definition column definition, e.g. {@code INT NOT NULL DEFAULT 0}
     */
    public static void addColumnIfMissing(Connection connection, String table, String column, String definition) throws SQLException {
        if (!IDENTIFIER.matcher(table).matches() || !IDENTIFIER.matcher(column).matches()) {
            throw new IllegalArgumentException("Invalid identifier: " + table + "." + column);
        }
        if (columnExists(connection, table, column)) {
            return;
        }
        try (Statement statement = connection.createStatement()) {
            statement.execute("ALTER TABLE " + table + " ADD COLUMN " + column + " " + definition);
        }
    }
}
