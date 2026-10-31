package me.x1no4ekk.core.warp;

import me.x1no4ekk.core.CorePlugin;
import me.x1no4ekk.core.config.ConfigFiles;
import me.x1no4ekk.core.database.SchemaUtil;
import me.x1no4ekk.core.module.CoreModule;
import me.x1no4ekk.core.warp.command.DelWarpCommand;
import me.x1no4ekk.core.warp.command.EditWarpCommand;
import me.x1no4ekk.core.warp.command.ServerWarpCommand;
import me.x1no4ekk.core.warp.command.SetWarpCommand;
import me.x1no4ekk.core.warp.command.WarpCommand;
import me.x1no4ekk.core.warp.command.WarpInfoCommand;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Player and system warps with menus, visits and 1-5 ratings.
 */
public final class WarpModule implements CoreModule {

    public static final String CONFIG_FILE = "warps.yml";

    private final CorePlugin plugin;
    private final WarpManager manager;
    private volatile WarpSettings settings;

    public WarpModule(CorePlugin plugin) {
        this.plugin = plugin;
        this.manager = new WarpManager(plugin, this::settings);
    }

    @Override
    public String id() {
        return "warp";
    }

    @Override
    public void createSchema(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS core_warp_grades (
                        warp       VARCHAR(32) NOT NULL,
                        uuid       CHAR(36) CHARACTER SET ascii NOT NULL,
                        grade      TINYINT     NOT NULL,
                        graded_at  BIGINT      NOT NULL,
                        PRIMARY KEY (warp, uuid)
                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci""");
        }
        // Menu slot of system warps; core_warps itself is created by the core schema.
        SchemaUtil.addColumnIfMissing(connection, "core_warps", "slot", "INT NULL");
    }

    @Override
    public Runnable loadConfig(ConfigFiles files) throws Exception {
        WarpSettings loaded = WarpSettings.load(files.get(CONFIG_FILE));
        return () -> this.settings = loaded;
    }

    @Override
    public void enable() {
        manager.start();
        plugin.registerCommand("warp", new WarpCommand(plugin, this));
        plugin.registerCommand("setwarp", new SetWarpCommand(plugin, this));
        plugin.registerCommand("delwarp", new DelWarpCommand(plugin, this));
        plugin.registerCommand("warpinfo", new WarpInfoCommand(plugin, this));
        plugin.registerCommand("editwarp", new EditWarpCommand(plugin, this));
        plugin.registerCommand("serverwarp", new ServerWarpCommand(plugin, this));
    }

    @Override
    public void disable() {
        manager.shutdown();
    }

    public WarpSettings settings() {
        return settings;
    }

    public WarpManager manager() {
        return manager;
    }
}
