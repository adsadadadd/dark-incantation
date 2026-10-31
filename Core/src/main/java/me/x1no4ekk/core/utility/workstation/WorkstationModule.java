package me.x1no4ekk.core.utility.workstation;

import me.x1no4ekk.core.CorePlugin;
import me.x1no4ekk.core.config.ConfigFiles;
import me.x1no4ekk.core.config.ConfigUtil;
import me.x1no4ekk.core.module.CoreModule;
import org.bukkit.Bukkit;

/**
 * Virtual workstations ({@code /workbench}, {@code /anvil}, ...) and {@code /invsee}.
 */
public final class WorkstationModule implements CoreModule {

    private final CorePlugin plugin;
    private volatile WorkstationSettings settings;

    public WorkstationModule(CorePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public String id() {
        return "utility-workstations";
    }

    @Override
    public Runnable loadConfig(ConfigFiles files) throws Exception {
        WorkstationSettings loaded = WorkstationSettings.load(ConfigUtil.section(files.settings(), "utility"));
        return () -> this.settings = loaded;
    }

    @Override
    public void enable() {
        for (Workstation workstation : Workstation.values()) {
            plugin.registerCommand(workstation.command(), new WorkstationCommand(plugin, this, workstation));
        }
        plugin.registerCommand("invsee", new InvseeCommand(plugin));
        Bukkit.getPluginManager().registerEvents(new InvseeListener(), plugin);
    }

    @Override
    public void disable() {
        InvseeListener.closeAll();
    }

    WorkstationSettings settings() {
        return settings;
    }
}
