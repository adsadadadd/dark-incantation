package me.x1no4ekk.core.utility.moderation;

import me.x1no4ekk.core.CorePlugin;
import me.x1no4ekk.core.config.ConfigFiles;
import me.x1no4ekk.core.config.ConfigUtil;
import me.x1no4ekk.core.module.CoreModule;
import me.x1no4ekk.core.user.User;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.UUID;
import java.util.function.BiConsumer;

/**
 * Staff tools: {@code /vanish}, {@code /spectate}, {@code /spectatestop}, {@code /whois}, {@code /realname},
 * {@code /nick} (display names) and {@code /kick}.
 */
public final class ModerationModule implements CoreModule {

    /** How long to wait for a user that was not cached at join (late enable / duplicate-login race). */
    private static final int LOAD_WAIT_ATTEMPTS = 40;
    private static final long LOAD_WAIT_PERIOD_TICKS = 5L;

    private final CorePlugin plugin;
    private final VanishService vanish;
    private final SpectateService spectate;
    private final NicknameService nicknames;
    private volatile ModerationSettings settings;
    private BukkitTask heartbeat;
    private long seconds;
    private boolean enabled;

    public ModerationModule(CorePlugin plugin) {
        this.plugin = plugin;
        this.vanish = new VanishService(plugin, this);
        this.spectate = new SpectateService(plugin, this);
        this.nicknames = new NicknameService(plugin, this);
    }

    @Override
    public String id() {
        return "utility-moderation";
    }

    @Override
    public Runnable loadConfig(ConfigFiles files) throws Exception {
        ModerationSettings loaded = ModerationSettings.load(ConfigUtil.section(files.settings(), "utility"));
        return () -> {
            this.settings = loaded;
            if (enabled) {
                nicknames.applyAll();
            }
        };
    }

    @Override
    public void enable() {
        Bukkit.getPluginManager().registerEvents(new ModerationListener(plugin, this), plugin);
        plugin.registerCommand("vanish", new VanishCommand(plugin, this));
        plugin.registerCommand("spectate", new SpectateCommand(plugin, this));
        plugin.registerCommand("spectatestop", new SpectateStopCommand(plugin, this));
        plugin.registerCommand("whois", new WhoisCommand(plugin, this));
        plugin.registerCommand("realname", new RealNameCommand(plugin));
        plugin.registerCommand("nick", new NickCommand(plugin, this));
        plugin.registerCommand("kick", new KickCommand(plugin));
        heartbeat = Bukkit.getScheduler().runTaskTimer(plugin, this::tick, 20L, 20L);
        enabled = true;
        // Players already online (late enable) are loaded asynchronously by the core after the modules.
        for (Player player : Bukkit.getOnlinePlayers()) {
            whenLoaded(player, this::applyJoinState);
        }
    }

    @Override
    public void disable() {
        enabled = false;
        if (heartbeat != null) {
            heartbeat.cancel();
            heartbeat = null;
        }
        spectate.restoreAll();
        vanish.showAll();
        nicknames.resetAll();
    }

    ModerationSettings settings() {
        return settings;
    }

    public VanishService vanish() {
        return vanish;
    }

    SpectateService spectate() {
        return spectate;
    }

    NicknameService nicknames() {
        return nicknames;
    }

    /**
     * Visibility and display name of a player whose data is loaded.
     */
    void applyJoinState(Player player, User user) {
        vanish.refresh(player, user.isVanish());
        nicknames.apply(player, user);
    }

    /**
     * Runs {@code action} once the player's {@link User} is cached; polls briefly if it is still loading.
     * Only the UUID is captured by the scheduled task.
     */
    void whenLoaded(Player player, BiConsumer<Player, User> action) {
        User user = plugin.userManager().getUser(player);
        if (user != null) {
            action.accept(player, user);
            return;
        }
        UUID uuid = player.getUniqueId();
        int[] attempts = {0};
        Bukkit.getScheduler().runTaskTimer(plugin, task -> {
            Player online = Bukkit.getPlayer(uuid);
            User loaded = plugin.userManager().getUser(uuid);
            if (online == null || loaded != null || ++attempts[0] >= LOAD_WAIT_ATTEMPTS) {
                task.cancel();
                if (online != null && loaded != null && enabled) {
                    action.accept(online, loaded);
                }
            }
        }, LOAD_WAIT_PERIOD_TICKS, LOAD_WAIT_PERIOD_TICKS);
    }

    private void tick() {
        seconds++;
        spectate.tick();
        int interval = settings.vanish().actionBarIntervalSeconds();
        if (interval > 0 && seconds % interval == 0) {
            vanish.remind();
        }
    }
}
