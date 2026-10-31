package me.x1no4ekk.core.travel.rtp;

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

/**
 * Random teleport (/rtp) with configurable modes: random point in a ring, or near a random online player.
 */
public final class RtpModule implements CoreModule, Listener {

    private static final long PURGE_PERIOD_TICKS = 20L * 60L;

    private final CorePlugin plugin;
    private final RtpManager manager;
    private volatile RtpSettings settings;
    private BukkitTask purgeTask;

    public RtpModule(CorePlugin plugin) {
        this.plugin = plugin;
        this.manager = new RtpManager(plugin, this::settings);
    }

    @Override
    public String id() {
        return "rtp";
    }

    @Override
    public Runnable loadConfig(ConfigFiles files) throws Exception {
        RtpSettings loaded = RtpSettings.load(ConfigUtil.section(files.settings(), "rtp"));
        return () -> this.settings = loaded;
    }

    @Override
    public void enable() {
        Bukkit.getPluginManager().registerEvents(this, plugin);
        plugin.registerCommand("rtp", new RtpCommand(plugin, this));
        purgeTask = Bukkit.getScheduler().runTaskTimer(plugin, manager::purgeCooldowns, PURGE_PERIOD_TICKS, PURGE_PERIOD_TICKS);
    }

    @Override
    public void disable() {
        if (purgeTask != null) {
            purgeTask.cancel();
            purgeTask = null;
        }
        manager.clear();
    }

    public RtpSettings settings() {
        return settings;
    }

    public RtpManager manager() {
        return manager;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        manager.handleQuit(event.getPlayer().getUniqueId());
    }
}
