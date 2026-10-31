package me.x1no4ekk.core.top;

import me.x1no4ekk.core.CorePlugin;
import me.x1no4ekk.core.config.ConfigFiles;
import me.x1no4ekk.core.config.ConfigUtil;
import me.x1no4ekk.core.module.CoreModule;

/**
 * Leaderboards: /killtop, /deathtop (core_stats) and /baltop (table of the Economy plugin).
 */
public final class TopModule implements CoreModule {

    private final CorePlugin plugin;
    private final TopManager manager;
    private volatile TopSettings settings;
    private boolean enabled;

    public TopModule(CorePlugin plugin) {
        this.plugin = plugin;
        this.manager = new TopManager(plugin, this::settings);
    }

    @Override
    public String id() {
        return "top";
    }

    @Override
    public Runnable loadConfig(ConfigFiles files) throws Exception {
        TopSettings loaded = TopSettings.load(ConfigUtil.section(files.settings(), "tops"));
        return () -> {
            this.settings = loaded;
            if (enabled) {
                manager.schedule(); // picks up a changed refresh interval on /core reload
            }
        };
    }

    @Override
    public void enable() {
        enabled = true;
        manager.schedule();
        for (TopType type : TopType.values()) {
            plugin.registerCommand(type.command(), new TopListCommand(plugin, this, type));
        }
    }

    @Override
    public void disable() {
        enabled = false;
        manager.clear();
    }

    public TopSettings settings() {
        return settings;
    }

    public TopManager manager() {
        return manager;
    }
}
