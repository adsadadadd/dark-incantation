package me.x1no4ekk.core.chat;

import me.x1no4ekk.core.CorePlugin;
import me.x1no4ekk.core.chat.pm.IgnoreCommand;
import me.x1no4ekk.core.chat.pm.IgnoreRepository;
import me.x1no4ekk.core.chat.pm.MsgCommand;
import me.x1no4ekk.core.chat.pm.MsgToggleCommand;
import me.x1no4ekk.core.chat.pm.PrivateMessageListener;
import me.x1no4ekk.core.chat.pm.PrivateMessageService;
import me.x1no4ekk.core.chat.pm.PrivateMessageSettings;
import me.x1no4ekk.core.chat.pm.ReplyCommand;
import me.x1no4ekk.core.chat.pm.SocialSpyCommand;
import me.x1no4ekk.core.chat.pm.TargetSpyCommand;
import me.x1no4ekk.core.chat.pm.TargetSpyService;
import me.x1no4ekk.core.config.ConfigFiles;
import me.x1no4ekk.core.module.CoreModule;
import org.bukkit.Bukkit;

/**
 * Private messages: /msg, /reply, /msgtoggle, /ignore, /socialspy, /targetspy.
 */
public final class PrivateMessageModule implements CoreModule {

    private final CorePlugin plugin;
    private final PrivateMessageService service;
    private final TargetSpyService targetSpy = new TargetSpyService();
    private final IgnoreRepository ignores;

    private volatile PrivateMessageSettings settings;

    public PrivateMessageModule(CorePlugin plugin) {
        this.plugin = plugin;
        this.service = new PrivateMessageService(plugin, this);
        this.ignores = new IgnoreRepository(plugin.database());
    }

    @Override
    public String id() {
        return "pm";
    }

    @Override
    public Runnable loadConfig(ConfigFiles files) throws Exception {
        PrivateMessageSettings loaded = PrivateMessageSettings.load(files.get(ChatYaml.FILE));
        return () -> this.settings = loaded;
    }

    @Override
    public void enable() {
        Bukkit.getPluginManager().registerEvents(new PrivateMessageListener(plugin, this), plugin);
        plugin.registerCommand("msg", new MsgCommand(plugin, this));
        plugin.registerCommand("reply", new ReplyCommand(plugin, this));
        plugin.registerCommand("msgtoggle", new MsgToggleCommand(plugin));
        plugin.registerCommand("ignore", new IgnoreCommand(plugin, this));
        plugin.registerCommand("socialspy", new SocialSpyCommand(plugin));
        plugin.registerCommand("targetspy", new TargetSpyCommand(plugin, this));
    }

    @Override
    public void disable() {
        service.clear();
        targetSpy.clear();
    }

    public PrivateMessageSettings settings() {
        return settings;
    }

    public PrivateMessageService service() {
        return service;
    }

    public TargetSpyService targetSpy() {
        return targetSpy;
    }

    public IgnoreRepository ignores() {
        return ignores;
    }
}
