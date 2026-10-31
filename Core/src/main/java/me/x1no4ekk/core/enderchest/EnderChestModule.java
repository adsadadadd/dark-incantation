package me.x1no4ekk.core.enderchest;

import me.x1no4ekk.core.CorePlugin;
import me.x1no4ekk.core.config.ConfigFiles;
import me.x1no4ekk.core.config.ConfigUtil;
import me.x1no4ekk.core.module.CoreModule;
import org.bukkit.Bukkit;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Multi-page ender chest ({@code /enderchest}) stored in MySQL, with pages by permission and purchasable pages.
 */
public final class EnderChestModule implements CoreModule {

    private final CorePlugin plugin;
    private final EnderChestRepository repository = new EnderChestRepository();
    private final EnderChestService service;
    private volatile EnderChestSettings settings;

    public EnderChestModule(CorePlugin plugin) {
        this.plugin = plugin;
        this.service = new EnderChestService(plugin, this, repository);
    }

    @Override
    public String id() {
        return "enderchest";
    }

    @Override
    public void createSchema(Connection connection) throws SQLException {
        repository.createSchema(connection);
    }

    @Override
    public Runnable loadConfig(ConfigFiles files) throws Exception {
        EnderChestSettings loaded = EnderChestSettings.load(ConfigUtil.section(files.settings(), "enderchest"));
        return () -> {
            this.settings = loaded;
            service.refreshNavigation();
        };
    }

    @Override
    public void enable() {
        Bukkit.getPluginManager().registerEvents(new EnderChestListener(this, service), plugin);
        plugin.registerCommand("enderchest", new EnderChestCommand(plugin, service));
    }

    @Override
    public void disable() {
        service.shutdown();
    }

    EnderChestSettings settings() {
        return settings;
    }
}
