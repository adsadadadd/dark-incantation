package me.x1no4ekk.core.top;

import me.x1no4ekk.core.CorePlugin;
import me.x1no4ekk.core.database.SchemaUtil;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import java.util.logging.Level;

/**
 * Leaderboards refreshed asynchronously on a timer and cached as immutable lists.
 */
public final class TopManager {

    private static final String STATS_TOP =
            "SELECT s.uuid, u.name, s.%1$s AS value FROM core_stats s JOIN core_users u ON u.uuid = s.uuid "
                    + "WHERE s.%1$s > 0 ORDER BY s.%1$s DESC, u.name ASC LIMIT ?";

    private final CorePlugin plugin;
    private final Supplier<TopSettings> settings;
    private final Map<TopType, List<TopEntry>> tops = new ConcurrentHashMap<>();
    private final AtomicBoolean refreshing = new AtomicBoolean();
    private final AtomicBoolean balanceProblemLogged = new AtomicBoolean();
    private volatile boolean balanceAvailable;
    private volatile boolean ready;
    private BukkitTask task;
    private int scheduledMinutes;

    TopManager(CorePlugin plugin, Supplier<TopSettings> settings) {
        this.plugin = plugin;
        this.settings = settings;
    }

    private record Result(List<TopEntry> kills, List<TopEntry> deaths, List<TopEntry> balance) {
    }

    /**
     * (Re)starts the refresh timer if the interval changed; the first refresh runs immediately.
     */
    void schedule() {
        int minutes = settings.get().refreshMinutes();
        if (task != null && minutes == scheduledMinutes) {
            return;
        }
        cancel();
        scheduledMinutes = minutes;
        long period = minutes * 60L * 20L;
        task = Bukkit.getScheduler().runTaskTimer(plugin, this::refresh, 1L, period);
    }

    void cancel() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    void clear() {
        cancel();
        tops.clear();
        balanceAvailable = false;
        ready = false;
    }

    /**
     * @return the cached leaderboard, or {@code null} before the first refresh
     */
    public List<TopEntry> get(TopType type) {
        return tops.get(type);
    }

    public boolean isBalanceAvailable() {
        return balanceAvailable;
    }

    /**
     * @return {@code true} once the first refresh has completed
     */
    public boolean isReady() {
        return ready;
    }

    private void refresh() {
        if (!refreshing.compareAndSet(false, true)) {
            return;
        }
        TopSettings config = settings.get();
        plugin.database().query(connection -> new Result(
                statsTop(connection, "kills", config.size()),
                statsTop(connection, "deaths", config.size()),
                balanceTop(connection, config)
        )).whenComplete((result, error) -> {
            refreshing.set(false);
            if (error != null) {
                plugin.getLogger().log(Level.WARNING, "Failed to refresh leaderboards", error);
                return;
            }
            tops.put(TopType.KILLS, result.kills());
            tops.put(TopType.DEATHS, result.deaths());
            if (result.balance() != null) {
                tops.put(TopType.BALANCE, result.balance());
                balanceAvailable = true;
            } else {
                tops.remove(TopType.BALANCE);
                balanceAvailable = false;
            }
            ready = true;
        });
    }

    private static List<TopEntry> statsTop(Connection connection, String column, int limit) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(String.format(STATS_TOP, column))) {
            statement.setInt(1, limit);
            return read(statement, "uuid", "name", "value");
        }
    }

    /**
     * @return the balance leaderboard, or {@code null} if the Economy table is missing or unreadable
     */
    private List<TopEntry> balanceTop(Connection connection, TopSettings config) {
        if (!config.balanceEnabled()) {
            return null;
        }
        try {
            if (!SchemaUtil.tableExists(connection, config.balanceTable())) {
                if (balanceProblemLogged.compareAndSet(false, true)) {
                    plugin.getLogger().warning("Table '" + config.balanceTable()
                            + "' does not exist yet (Economy plugin not installed?): /baltop is unavailable.");
                }
                return null;
            }
            String sql = "SELECT " + config.uuidColumn() + " AS uuid, " + config.nameColumn() + " AS name, "
                    + config.balanceColumn() + " AS value FROM " + config.balanceTable()
                    + " ORDER BY " + config.balanceColumn() + " DESC LIMIT ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, config.size());
                List<TopEntry> entries = read(statement, "uuid", "name", "value");
                balanceProblemLogged.set(false);
                return entries;
            }
        } catch (SQLException e) {
            if (balanceProblemLogged.compareAndSet(false, true)) {
                plugin.getLogger().log(Level.WARNING, "Cannot read balances from '" + config.balanceTable()
                        + "' (check tops.baltop in settings.yml): /baltop is unavailable.", e);
            }
            return null;
        }
    }

    private static List<TopEntry> read(PreparedStatement statement, String uuidColumn, String nameColumn,
                                       String valueColumn) throws SQLException {
        List<TopEntry> entries = new ArrayList<>();
        try (ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                UUID uuid;
                try {
                    uuid = UUID.fromString(rs.getString(uuidColumn));
                } catch (IllegalArgumentException | NullPointerException e) {
                    continue;
                }
                String name = rs.getString(nameColumn);
                entries.add(new TopEntry(uuid, name == null ? "?" : name, rs.getDouble(valueColumn)));
            }
        }
        return List.copyOf(entries);
    }
}
