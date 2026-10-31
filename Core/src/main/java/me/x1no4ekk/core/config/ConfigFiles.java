package me.x1no4ekk.core.config;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * YAML files of one (re)load pass. Each file is read from disk at most once per pass, so modules sharing
 * settings.yml see the same snapshot.
 */
public final class ConfigFiles {

    public static final String SETTINGS = "settings.yml";
    public static final String MESSAGES = "messages.yml";

    private final JavaPlugin plugin;
    private final Map<String, YamlConfiguration> loaded = new HashMap<>();

    public ConfigFiles(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Loads a bundled YAML file from the data folder (created from the jar on first run).
     */
    public YamlConfiguration get(String name) throws IOException, InvalidConfigurationException {
        YamlConfiguration config = loaded.get(name);
        if (config == null) {
            try {
                config = ConfigFile.load(plugin, name);
            } catch (InvalidConfigurationException e) {
                throw new InvalidConfigurationException(name + ": " + e.getMessage(), e);
            }
            loaded.put(name, config);
        }
        return config;
    }

    public YamlConfiguration settings() throws IOException, InvalidConfigurationException {
        return get(SETTINGS);
    }

    public YamlConfiguration messages() throws IOException, InvalidConfigurationException {
        return get(MESSAGES);
    }
}
