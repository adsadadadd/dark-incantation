package me.x1no4ekk.core.reward;

import me.x1no4ekk.core.CorePlugin;
import me.x1no4ekk.core.config.ConfigFiles;
import me.x1no4ekk.core.module.CoreModule;
import org.bukkit.Bukkit;

/**
 * Kill statistics and money rewards for killing players and mobs (killermoney.yml).
 */
public final class RewardModule implements CoreModule {

    private final CorePlugin plugin;
    private volatile RewardSettings settings;

    public RewardModule(CorePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public String id() {
        return "rewards";
    }

    @Override
    public Runnable loadConfig(ConfigFiles files) throws Exception {
        RewardSettings loaded = RewardSettings.load(files.get("killermoney.yml"));
        return () -> this.settings = loaded;
    }

    @Override
    public void enable() {
        Bukkit.getPluginManager().registerEvents(new RewardListener(plugin, this), plugin);
    }

    RewardSettings settings() {
        return settings;
    }
}
