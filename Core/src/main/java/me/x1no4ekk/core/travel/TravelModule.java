package me.x1no4ekk.core.travel;

import me.x1no4ekk.core.CorePlugin;
import me.x1no4ekk.core.config.ConfigFiles;
import me.x1no4ekk.core.config.ConfigUtil;
import me.x1no4ekk.core.module.CoreModule;
import me.x1no4ekk.core.travel.command.BackCommand;
import me.x1no4ekk.core.travel.command.SetSpawnCommand;
import me.x1no4ekk.core.travel.command.SpawnCommand;
import me.x1no4ekk.core.travel.command.TeleportCancelCommand;
import me.x1no4ekk.core.travel.command.TopCommand;
import me.x1no4ekk.core.travel.command.TpAcceptCommand;
import me.x1no4ekk.core.travel.command.TpAllCommand;
import me.x1no4ekk.core.travel.command.TpCancelCommand;
import me.x1no4ekk.core.travel.command.TpCommand;
import me.x1no4ekk.core.travel.command.TpDenyCommand;
import me.x1no4ekk.core.travel.command.TpHereCommand;
import me.x1no4ekk.core.travel.command.TpPosCommand;
import me.x1no4ekk.core.travel.command.TpToggleCommand;
import me.x1no4ekk.core.travel.command.TpaCommand;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

/**
 * Player and staff teleports: /tp family, /tpa requests, /back, /top, spawn with join/respawn/void handling.
 */
public final class TravelModule implements CoreModule {

    /** Ignore {@code User#isTpToggle()} of the target (and ignore lists for /tpa). */
    public static final String BYPASS_TOGGLE_PERMISSION = "core.tp.bypass-toggle";

    private static final long PURGE_PERIOD_TICKS = 20L * 60L;

    private final CorePlugin plugin;
    private final SpawnManager spawn;
    private final TpaManager tpa;
    private final PickupGuard pickupGuard = new PickupGuard();
    private volatile TravelSettings settings;
    private TravelListener listener;
    private BukkitTask purgeTask;

    public TravelModule(CorePlugin plugin) {
        this.plugin = plugin;
        this.spawn = new SpawnManager(plugin);
        this.tpa = new TpaManager(plugin, this::settings);
    }

    @Override
    public String id() {
        return "travel";
    }

    @Override
    public void createSchema(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS core_server_locations (
                        name   VARCHAR(32) NOT NULL,
                        world  VARCHAR(64) NOT NULL,
                        x      DOUBLE      NOT NULL,
                        y      DOUBLE      NOT NULL,
                        z      DOUBLE      NOT NULL,
                        yaw    FLOAT       NOT NULL,
                        pitch  FLOAT       NOT NULL,
                        PRIMARY KEY (name)
                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci""");
        }
    }

    @Override
    public Runnable loadConfig(ConfigFiles files) throws Exception {
        TravelSettings loaded = TravelSettings.load(ConfigUtil.section(files.settings(), "travel"));
        return () -> this.settings = loaded;
    }

    @Override
    public void enable() {
        spawn.load();
        listener = new TravelListener(plugin, this, pickupGuard);
        Bukkit.getPluginManager().registerEvents(listener, plugin);

        plugin.registerCommand("tp", new TpCommand(plugin));
        plugin.registerCommand("tphere", new TpHereCommand(plugin));
        plugin.registerCommand("tppos", new TpPosCommand(plugin));
        plugin.registerCommand("tpall", new TpAllCommand(plugin));
        plugin.registerCommand("tpa", new TpaCommand(plugin, this, TpaManager.Type.TO_TARGET));
        plugin.registerCommand("tpahere", new TpaCommand(plugin, this, TpaManager.Type.TO_SENDER));
        plugin.registerCommand("tpaccept", new TpAcceptCommand(plugin, this));
        plugin.registerCommand("tpdeny", new TpDenyCommand(plugin, this));
        plugin.registerCommand("tpcancel", new TpCancelCommand(plugin, this));
        plugin.registerCommand("teleportcancel", new TeleportCancelCommand(plugin));
        plugin.registerCommand("tptoggle", new TpToggleCommand(plugin));
        plugin.registerCommand("back", new BackCommand(plugin, this));
        plugin.registerCommand("top", new TopCommand(plugin, this));
        plugin.registerCommand("setspawn", new SetSpawnCommand(plugin, this));
        plugin.registerCommand("spawn", new SpawnCommand(plugin, this));

        purgeTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            tpa.purgeCooldowns();
            pickupGuard.purge();
        }, PURGE_PERIOD_TICKS, PURGE_PERIOD_TICKS);
    }

    @Override
    public void disable() {
        if (purgeTask != null) {
            purgeTask.cancel();
            purgeTask = null;
        }
        tpa.clear();
        pickupGuard.clear();
        if (listener != null) {
            listener.clear();
        }
    }

    public TravelSettings settings() {
        return settings;
    }

    public SpawnManager spawn() {
        return spawn;
    }

    public TpaManager tpa() {
        return tpa;
    }

    /**
     * Forbids picking up items for {@code millis} (applied after /back).
     */
    public void blockPickup(UUID uuid, long millis) {
        pickupGuard.block(uuid, millis);
    }
}
