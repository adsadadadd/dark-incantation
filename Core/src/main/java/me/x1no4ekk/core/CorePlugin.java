package me.x1no4ekk.core;

import me.x1no4ekk.core.api.CoreAPI;
import me.x1no4ekk.core.api.CoreApiImpl;
import me.x1no4ekk.core.combat.CombatManager;
import me.x1no4ekk.core.command.CoreCommand;
import me.x1no4ekk.core.command.DelHomeCommand;
import me.x1no4ekk.core.command.HomeCommand;
import me.x1no4ekk.core.command.PlayerHomeCommand;
import me.x1no4ekk.core.command.SetHomeCommand;
import me.x1no4ekk.core.config.ConfigFiles;
import me.x1no4ekk.core.config.Messages;
import me.x1no4ekk.core.config.Settings;
import me.x1no4ekk.core.database.DatabaseManager;
import me.x1no4ekk.core.gui.GuiListener;
import me.x1no4ekk.core.home.HomeManager;
import me.x1no4ekk.core.home.HomeRepository;
import me.x1no4ekk.core.home.HomeTeleporter;
import me.x1no4ekk.core.hook.Hooks;
import me.x1no4ekk.core.horn.HornListener;
import me.x1no4ekk.core.horn.TeleportHorn;
import me.x1no4ekk.core.module.CoreModule;
import me.x1no4ekk.core.teleport.TeleportService;
import me.x1no4ekk.core.user.UserListener;
import me.x1no4ekk.core.user.UserManager;
import me.x1no4ekk.core.user.UserRepository;
import me.x1no4ekk.core.util.MainThreadExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.logging.Level;

public final class CorePlugin extends JavaPlugin {

    private volatile Settings settings;
    private Messages messages;
    private Executor mainThread;
    private DatabaseManager database;
    private Hooks hooks;
    private UserManager userManager;
    private HomeManager homeManager;
    private HomeTeleporter homeTeleporter;
    private TeleportHorn teleportHorn;
    private TeleportService teleportService;
    private CombatManager combatManager;
    private List<CoreModule> modules = List.of();
    /** Modules whose enable() completed; only these are disabled. */
    private final List<CoreModule> enabledModules = new ArrayList<>();

    @Override
    public void onEnable() {
        this.mainThread = new MainThreadExecutor(this);
        this.messages = new Messages(getLogger());

        ConfigFiles files = new ConfigFiles(this);
        try {
            this.settings = Settings.load(files.settings());
            messages.load(files.messages());
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Invalid configuration (settings.yml / messages.yml): " + e.getMessage(), e);
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Hard requirement: without MySQL the plugin must not run at all.
        DatabaseManager db = new DatabaseManager(getLogger(), settings.database());
        try {
            db.connect();
            db.createSchema();
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "==============================================================");
            getLogger().log(Level.SEVERE, " Core: cannot connect to MySQL at " + settings.database().host() + ":"
                    + settings.database().port() + "/" + settings.database().name());
            getLogger().log(Level.SEVERE, " Reason: " + e.getMessage());
            getLogger().log(Level.SEVERE, " Check the 'database' section in settings.yml. The plugin is now DISABLED.");
            getLogger().log(Level.SEVERE, "==============================================================", e);
            db.close();
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        this.database = db;
        this.hooks = Hooks.create();

        HomeRepository homeRepository = new HomeRepository();
        this.userManager = new UserManager(this, database, new UserRepository(homeRepository), mainThread);
        this.homeManager = new HomeManager(getLogger(), database, homeRepository, this::settings, mainThread);
        this.teleportHorn = new TeleportHorn(this, this::settings);
        this.homeTeleporter = new HomeTeleporter(userManager, homeManager, teleportHorn, messages, mainThread);
        this.teleportService = new TeleportService(this);
        this.combatManager = new CombatManager(this);
        this.modules = List.copyOf(createModules());

        try {
            database.execute(connection -> {
                for (CoreModule module : modules) {
                    module.createSchema(connection);
                }
                return null;
            });
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Core: could not create module tables in MySQL. The plugin is now DISABLED.", e);
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        try {
            loadModuleConfigs(files).forEach(Runnable::run);
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Invalid configuration: " + e.getMessage(), e);
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        PluginManager pluginManager = getServer().getPluginManager();
        pluginManager.registerEvents(new UserListener(userManager, messages, getLogger(),
                () -> settings.users().loadTimeoutSeconds()), this);
        pluginManager.registerEvents(new GuiListener(), this);
        pluginManager.registerEvents(new HornListener(this, teleportHorn), this);

        registerCommand("core", new CoreCommand(this));
        registerCommand("sethome", new SetHomeCommand(this));
        registerCommand("delhome", new DelHomeCommand(this));
        registerCommand("playerhome", new PlayerHomeCommand(this));
        registerCommand("home", new HomeCommand(this));

        for (CoreModule module : modules) {
            try {
                module.enable();
                enabledModules.add(module);
            } catch (RuntimeException e) {
                getLogger().log(Level.SEVERE, "Module '" + module.id() + "' failed to enable", e);
            }
        }

        teleportHorn.registerRecipe();
        userManager.start(settings.users().autosaveIntervalMinutes());
        getServer().getServicesManager().register(CoreAPI.class, new CoreApiImpl(this), this, ServicePriority.Normal);

        // Players already online (plugin enabled late / reloaded) missed AsyncPlayerPreLoginEvent.
        for (Player player : getServer().getOnlinePlayers()) {
            userManager.loadAsync(player.getUniqueId(), player.getName());
            teleportHorn.discoverRecipe(player);
        }

        getLogger().info("Core enabled: MySQL connected, " + modules.size() + " module(s), "
                + getServer().getOnlinePlayers().size() + " online player(s) loading.");
    }

    /**
     * Every feature module in enable order. Core services come first because features depend on them.
     */
    private List<CoreModule> createModules() {
        List<CoreModule> list = new ArrayList<>();
        list.add(teleportService);
        list.add(combatManager);
        list.add(new me.x1no4ekk.core.gameplay.GameplayModule(this));
        return list;
    }

    @Override
    public void onDisable() {
        getServer().getServicesManager().unregisterAll(this);
        // Closing menus first lets modules save open containers while the database is still running.
        GuiListener.closeAll();
        List<CoreModule> reversed = new ArrayList<>(enabledModules);
        Collections.reverse(reversed);
        for (CoreModule module : reversed) {
            try {
                module.disable();
            } catch (RuntimeException e) {
                getLogger().log(Level.SEVERE, "Module '" + module.id() + "' failed to disable cleanly", e);
            }
        }
        enabledModules.clear();
        modules = List.of();
        if (teleportHorn != null) {
            teleportHorn.unregisterRecipe();
        }
        if (userManager != null) {
            userManager.shutdown();
        }
        if (database != null) {
            database.close(); // drains queued writes, including the final saves above
        }
        userManager = null;
        homeManager = null;
        homeTeleporter = null;
        teleportService = null;
        combatManager = null;
        database = null;
    }

    private List<Runnable> loadModuleConfigs(ConfigFiles files) throws Exception {
        List<Runnable> commits = new ArrayList<>(modules.size());
        for (CoreModule module : modules) {
            try {
                commits.add(module.loadConfig(files));
            } catch (Exception e) {
                throw new IllegalArgumentException("[" + module.id() + "] " + e.getMessage(), e);
            }
        }
        return commits;
    }

    /**
     * Reloads every configuration file. All-or-nothing: if any file or module is invalid, nothing changes.
     * Database settings require a restart.
     *
     * @throws Exception if a file is invalid; the previous settings remain active
     */
    public void reloadSettings() throws Exception {
        ConfigFiles files = new ConfigFiles(this);
        Settings loaded = Settings.load(files.settings());
        var messagesFile = files.messages();
        List<Runnable> commits = loadModuleConfigs(files);

        messages.load(messagesFile);
        this.settings = loaded;
        commits.forEach(Runnable::run);
        teleportHorn.registerRecipe();
    }

    /**
     * Binds an executor to a command declared in plugin.yml.
     */
    public void registerCommand(String name, TabExecutor executor) {
        PluginCommand command = getCommand(name);
        if (command == null) {
            getLogger().warning("Command '" + name + "' is missing from plugin.yml");
            return;
        }
        command.setExecutor(executor);
        command.setTabCompleter(executor);
    }

    // ---------------------------------------------------------------- accessors

    public Settings settings() {
        return settings;
    }

    public Messages messages() {
        return messages;
    }

    public Executor mainThread() {
        return mainThread;
    }

    public DatabaseManager database() {
        return database;
    }

    public Hooks hooks() {
        return hooks;
    }

    public UserManager userManager() {
        return userManager;
    }

    public HomeManager homeManager() {
        return homeManager;
    }

    public HomeTeleporter homeTeleporter() {
        return homeTeleporter;
    }

    public TeleportHorn teleportHorn() {
        return teleportHorn;
    }

    public TeleportService teleportService() {
        return teleportService;
    }

    public CombatManager combatManager() {
        return combatManager;
    }

    /**
     * Finds an enabled module by type, or {@code null}.
     */
    public <T extends CoreModule> T module(Class<T> type) {
        for (CoreModule module : modules) {
            if (type.isInstance(module)) {
                return type.cast(module);
            }
        }
        return null;
    }
}
