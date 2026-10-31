package me.x1no4ekk.core.chat;

import me.x1no4ekk.core.CorePlugin;
import me.x1no4ekk.core.chat.announce.AnnouncerListener;
import me.x1no4ekk.core.chat.announce.AnnouncerSettings;
import me.x1no4ekk.core.chat.announce.AutoBroadcaster;
import me.x1no4ekk.core.chat.announce.BossBarRotator;
import me.x1no4ekk.core.chat.announce.BossBarToggleCommand;
import me.x1no4ekk.core.config.ConfigFiles;
import me.x1no4ekk.core.module.CoreModule;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

/**
 * Automatic chat announcements and rotating boss bar announcements, /bossbartoggle.
 */
public final class AnnouncerModule implements CoreModule {

    private final CorePlugin plugin;
    private final AutoBroadcaster broadcaster;
    private final BossBarRotator bossBars;

    private volatile AnnouncerSettings settings;
    private BukkitTask broadcastTask;
    private BukkitTask bossBarTask;

    public AnnouncerModule(CorePlugin plugin) {
        this.plugin = plugin;
        this.broadcaster = new AutoBroadcaster(plugin, this);
        this.bossBars = new BossBarRotator(plugin, this);
    }

    @Override
    public String id() {
        return "announcer";
    }

    @Override
    public Runnable loadConfig(ConfigFiles files) throws Exception {
        AnnouncerSettings loaded = AnnouncerSettings.load(files.get(ChatYaml.FILE));
        return () -> this.settings = loaded;
    }

    @Override
    public void enable() {
        Bukkit.getPluginManager().registerEvents(new AnnouncerListener(bossBars), plugin);
        plugin.registerCommand("bossbartoggle", new BossBarToggleCommand(plugin, bossBars));
        broadcastTask = Bukkit.getScheduler().runTaskTimer(plugin, broadcaster, 20L, 20L);
        bossBarTask = Bukkit.getScheduler().runTaskTimer(plugin, bossBars, 1L, BossBarRotator.PERIOD_TICKS);
    }

    @Override
    public void disable() {
        if (broadcastTask != null) {
            broadcastTask.cancel();
            broadcastTask = null;
        }
        if (bossBarTask != null) {
            bossBarTask.cancel();
            bossBarTask = null;
        }
        bossBars.stop();
        broadcaster.reset();
    }

    public AnnouncerSettings settings() {
        return settings;
    }
}
