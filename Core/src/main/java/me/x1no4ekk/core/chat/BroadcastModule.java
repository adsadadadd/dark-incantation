package me.x1no4ekk.core.chat;

import me.x1no4ekk.core.CorePlugin;
import me.x1no4ekk.core.chat.broadcast.BroadcastCommand;
import me.x1no4ekk.core.chat.broadcast.BroadcastSettings;
import me.x1no4ekk.core.chat.broadcast.BroadcastType;
import me.x1no4ekk.core.config.ConfigFiles;
import me.x1no4ekk.core.module.CoreModule;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;

/**
 * Broadcast commands (/broadcast, /adbroadcast, /buybroadcast, /sellbroadcast, /streambroadcast, /videobroadcast).
 * Cooldowns survive a quit and are purged by a timer.
 */
public final class BroadcastModule implements CoreModule {

    private static final long PURGE_PERIOD_TICKS = 20L * 60;

    private final CorePlugin plugin;
    private final Map<String, CooldownMap> cooldowns = new HashMap<>();

    private volatile BroadcastSettings settings;
    private BukkitTask purgeTask;

    public BroadcastModule(CorePlugin plugin) {
        this.plugin = plugin;
        for (String id : BroadcastSettings.TYPES) {
            cooldowns.put(id, new CooldownMap());
        }
    }

    @Override
    public String id() {
        return "broadcast";
    }

    @Override
    public Runnable loadConfig(ConfigFiles files) throws Exception {
        BroadcastSettings loaded = BroadcastSettings.load(files.get(ChatYaml.FILE));
        return () -> this.settings = loaded;
    }

    @Override
    public void enable() {
        for (BroadcastType type : settings.types().values()) {
            plugin.registerCommand(type.command(), new BroadcastCommand(plugin, this, type.id()));
        }
        purgeTask = Bukkit.getScheduler().runTaskTimer(plugin,
                () -> cooldowns.values().forEach(CooldownMap::purge), PURGE_PERIOD_TICKS, PURGE_PERIOD_TICKS);
    }

    @Override
    public void disable() {
        if (purgeTask != null) {
            purgeTask.cancel();
            purgeTask = null;
        }
        cooldowns.values().forEach(CooldownMap::clear);
    }

    public BroadcastSettings settings() {
        return settings;
    }

    public CooldownMap cooldowns(String typeId) {
        return cooldowns.get(typeId);
    }
}
