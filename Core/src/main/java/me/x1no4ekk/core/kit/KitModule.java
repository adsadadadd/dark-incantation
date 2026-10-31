package me.x1no4ekk.core.kit;

import me.x1no4ekk.core.CorePlugin;
import me.x1no4ekk.core.config.ConfigFiles;
import me.x1no4ekk.core.module.CoreModule;
import org.bukkit.Bukkit;

/**
 * Kits ({@code /kit}): kit menu and preview, free claims with cooldowns, purchases for АР, start kits and
 * administrative editing of kits.yml.
 */
public final class KitModule implements CoreModule {

    private final CorePlugin plugin;
    private final KitService service;
    private final KitFileEditor editor;
    private volatile KitSettings settings;

    public KitModule(CorePlugin plugin) {
        this.plugin = plugin;
        this.service = new KitService(plugin, this);
        this.editor = new KitFileEditor(plugin, this);
    }

    @Override
    public String id() {
        return "kits";
    }

    @Override
    public Runnable loadConfig(ConfigFiles files) throws Exception {
        KitSettings loaded = KitSettings.load(files.get(KitFileEditor.FILE));
        return () -> publish(loaded);
    }

    @Override
    public void enable() {
        Bukkit.getPluginManager().registerEvents(new KitListener(plugin, this), plugin);
        plugin.registerCommand("kit", new KitCommand(plugin, this));
    }

    void publish(KitSettings loaded) {
        this.settings = loaded;
        if (!loaded.overflow().isEmpty()) {
            plugin.getLogger().warning("[kits] No free slot in the kit menu for: " + String.join(", ", loaded.overflow())
                    + " (add slots to gui.kits.content-slots or set 'slot'); they are available only via /kit <name>.");
        }
    }

    KitSettings settings() {
        return settings;
    }

    KitService service() {
        return service;
    }

    KitFileEditor editor() {
        return editor;
    }
}
