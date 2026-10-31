package me.x1no4ekk.core.utility.info;

import me.x1no4ekk.core.CorePlugin;
import me.x1no4ekk.core.config.ConfigFiles;
import me.x1no4ekk.core.config.ConfigUtil;
import me.x1no4ekk.core.module.CoreModule;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.UUID;

/**
 * Information commands: {@code /gc}, {@code /ping}, {@code /list}, {@code /near}, {@code /getpos},
 * {@code /compass}, {@code /gps}, {@code /date}, {@code /rules}, {@code /links}.
 */
public final class InfoModule implements CoreModule, Listener {

    private final CorePlugin plugin;
    private final GpsService gps;
    private volatile InfoSettings settings;
    private BukkitTask gpsTask;

    public InfoModule(CorePlugin plugin) {
        this.plugin = plugin;
        this.gps = new GpsService(plugin, this);
    }

    @Override
    public String id() {
        return "utility-info";
    }

    @Override
    public Runnable loadConfig(ConfigFiles files) throws Exception {
        InfoSettings loaded = InfoSettings.load(ConfigUtil.section(files.settings(), "utility"));
        return () -> this.settings = loaded;
    }

    @Override
    public void enable() {
        Bukkit.getPluginManager().registerEvents(this, plugin);
        plugin.registerCommand("gc", new GcCommand(plugin));
        plugin.registerCommand("ping", new PingCommand(plugin));
        plugin.registerCommand("list", new ListCommand(plugin, this));
        plugin.registerCommand("near", new NearCommand(plugin, this));
        plugin.registerCommand("getpos", new GetPosCommand(plugin));
        plugin.registerCommand("compass", new CompassCommand(plugin));
        plugin.registerCommand("gps", new GpsCommand(plugin, gps));
        plugin.registerCommand("date", new DateCommand(plugin, this));
        plugin.registerCommand("rules", new LinesCommand(plugin, this, "rules", InfoSettings::rules));
        plugin.registerCommand("links", new LinesCommand(plugin, this, "links", InfoSettings::links));
        gpsTask = Bukkit.getScheduler().runTaskTimer(plugin, gps::tick, 20L, 20L);
    }

    @Override
    public void disable() {
        if (gpsTask != null) {
            gpsTask.cancel();
            gpsTask = null;
        }
        gps.clearAll();
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onQuit(PlayerQuitEvent event) {
        gps.handleQuit(event.getPlayer());
    }

    /**
     * Whether the player's action bar is used by {@code /gps} (other action-bar reminders skip them).
     */
    public boolean isGpsActive(UUID uuid) {
        return gps.isActive(uuid);
    }

    InfoSettings settings() {
        return settings;
    }
}
