package me.x1no4ekk.core.gameplay;

import me.x1no4ekk.core.CorePlugin;
import me.x1no4ekk.core.config.ConfigFiles;
import me.x1no4ekk.core.config.ConfigUtil;
import me.x1no4ekk.core.gameplay.command.BurnCommand;
import me.x1no4ekk.core.gameplay.command.ClearInventoryCommand;
import me.x1no4ekk.core.gameplay.command.EnchantCommand;
import me.x1no4ekk.core.gameplay.command.ExtCommand;
import me.x1no4ekk.core.gameplay.command.FeedCommand;
import me.x1no4ekk.core.gameplay.command.FireworkCommand;
import me.x1no4ekk.core.gameplay.command.FlyCommand;
import me.x1no4ekk.core.gameplay.command.FlyElytraCommand;
import me.x1no4ekk.core.gameplay.command.GamemodeCommand;
import me.x1no4ekk.core.gameplay.command.GiveCommand;
import me.x1no4ekk.core.gameplay.command.GodCommand;
import me.x1no4ekk.core.gameplay.command.HatCommand;
import me.x1no4ekk.core.gameplay.command.HeadCommand;
import me.x1no4ekk.core.gameplay.command.HealCommand;
import me.x1no4ekk.core.gameplay.command.ItemDbCommand;
import me.x1no4ekk.core.gameplay.command.ItemRenameCommand;
import me.x1no4ekk.core.gameplay.command.KillCommand;
import me.x1no4ekk.core.gameplay.command.MilkCommand;
import me.x1no4ekk.core.gameplay.command.MoreCommand;
import me.x1no4ekk.core.gameplay.command.RepairCommand;
import me.x1no4ekk.core.gameplay.command.SpeedCommand;
import me.x1no4ekk.core.gameplay.command.SuicideCommand;
import me.x1no4ekk.core.module.CoreModule;
import me.x1no4ekk.core.user.User;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

/**
 * Player and item commands (/gamemode, /fly, /god, /heal, /give, /repair...) with fly/god handling.
 * <p>
 * Per-player state (elytra flight, pending joins, pickup notices) is UUID-keyed, removed on quit and cleared on
 * disable; the short /fly-others and /firework cooldowns survive a quit and are purged by the 1-second ticker.
 */
public final class GameplayModule implements CoreModule {

    public static final String GOD_PERMISSION = "core.god";
    private static final float DEFAULT_WALK_SPEED = 0.2f;
    private static final float DEFAULT_FLY_SPEED = 0.1f;

    private final CorePlugin plugin;
    private final FlightService flight;
    private final Cooldowns flyOthersCooldowns = new Cooldowns();
    private final Cooldowns fireworkCooldowns = new Cooldowns();
    private final Cooldowns pickupNotices = new Cooldowns();
    // Players whose User was not cached yet at join (async fallback load).
    private final Set<UUID> pendingJoins = new HashSet<>();
    private final NamespacedKey harmlessFireworkKey;
    private volatile GameplaySettings settings;
    private BukkitTask ticker;
    private int tickCounter;

    public GameplayModule(CorePlugin plugin) {
        this.plugin = plugin;
        this.flight = new FlightService(plugin, this);
        this.harmlessFireworkKey = new NamespacedKey(plugin, "harmless_firework");
    }

    @Override
    public String id() {
        return "gameplay";
    }

    @Override
    public Runnable loadConfig(ConfigFiles files) throws Exception {
        GameplaySettings loaded = GameplaySettings.load(ConfigUtil.section(files.settings(), "gameplay"));
        return () -> {
            this.settings = loaded;
            if (!loaded.flyElytra().enabled()) {
                flight.clear();
            }
        };
    }

    @Override
    public void enable() {
        Bukkit.getPluginManager().registerEvents(new GameplayListener(plugin, this), plugin);

        plugin.registerCommand("gamemode", new GamemodeCommand(plugin, null));
        plugin.registerCommand("gms", new GamemodeCommand(plugin, GameMode.SURVIVAL));
        plugin.registerCommand("gmc", new GamemodeCommand(plugin, GameMode.CREATIVE));
        plugin.registerCommand("gma", new GamemodeCommand(plugin, GameMode.ADVENTURE));
        plugin.registerCommand("gmsp", new GamemodeCommand(plugin, GameMode.SPECTATOR));
        plugin.registerCommand("fly", new FlyCommand(plugin, this));
        plugin.registerCommand("flyelytra", new FlyElytraCommand(plugin, this));
        plugin.registerCommand("speed", new SpeedCommand(plugin, false));
        plugin.registerCommand("flyspeed", new SpeedCommand(plugin, true));
        plugin.registerCommand("god", new GodCommand(plugin, this));
        plugin.registerCommand("heal", new HealCommand(plugin, this));
        plugin.registerCommand("feed", new FeedCommand(plugin, this));
        plugin.registerCommand("ext", new ExtCommand(plugin, this));
        plugin.registerCommand("burn", new BurnCommand(plugin, this));
        plugin.registerCommand("milk", new MilkCommand(plugin, this));
        plugin.registerCommand("kill", new KillCommand(plugin));
        plugin.registerCommand("suicide", new SuicideCommand(plugin));
        plugin.registerCommand("more", new MoreCommand(plugin));
        plugin.registerCommand("hat", new HatCommand(plugin));
        plugin.registerCommand("head", new HeadCommand(plugin, this));
        plugin.registerCommand("repair", new RepairCommand(plugin, this));
        plugin.registerCommand("itemrename", new ItemRenameCommand(plugin, this));
        plugin.registerCommand("itemdb", new ItemDbCommand(plugin));
        plugin.registerCommand("enchant", new EnchantCommand(plugin));
        plugin.registerCommand("give", new GiveCommand(plugin, this));
        plugin.registerCommand("clearinventory", new ClearInventoryCommand(plugin, this));
        plugin.registerCommand("firework", new FireworkCommand(plugin, this));

        ticker = Bukkit.getScheduler().runTaskTimer(plugin, this::tick, 20L, 20L);

        // Late enable: users of online players are loaded after modules, the ticker applies them.
        for (Player player : Bukkit.getOnlinePlayers()) {
            handleJoin(player);
        }
    }

    @Override
    public void disable() {
        if (ticker != null) {
            ticker.cancel();
            ticker = null;
        }
        flight.clear();
        flyOthersCooldowns.clear();
        fireworkCooldowns.clear();
        pickupNotices.clear();
        pendingJoins.clear();
    }

    private void tick() {
        flight.tickElytra();
        processPendingJoins();
        if (++tickCounter >= 30) {
            tickCounter = 0;
            flyOthersCooldowns.purge();
            fireworkCooldowns.purge();
            pickupNotices.purge();
        }
    }

    // ---------------------------------------------------------------- join / quit

    void handleJoin(Player player) {
        User user = plugin.userManager().getUser(player);
        if (user == null) {
            pendingJoins.add(player.getUniqueId());
            return;
        }
        applyJoin(player, user);
    }

    private void processPendingJoins() {
        if (pendingJoins.isEmpty()) {
            return;
        }
        Iterator<UUID> iterator = pendingJoins.iterator();
        while (iterator.hasNext()) {
            UUID uuid = iterator.next();
            Player player = Bukkit.getPlayer(uuid);
            if (player == null) {
                iterator.remove();
                continue;
            }
            User user = plugin.userManager().getUser(uuid);
            if (user != null) {
                iterator.remove();
                applyJoin(player, user);
            }
        }
    }

    /**
     * God and fly are only kept while the player still has the permission.
     */
    private void applyJoin(Player player, User user) {
        if (user.isGod() && !player.hasPermission(GOD_PERMISSION)) {
            user.setGod(false);
        }
        if (user.isFly()) {
            if (player.hasPermission(FlightService.FLY_PERMISSION)) {
                flight.restoreFly(player, settings.fly().safeLogin());
            } else {
                user.setFly(false);
            }
        }
    }

    void handleQuit(Player player) {
        UUID uuid = player.getUniqueId();
        flight.remove(uuid);
        pendingJoins.remove(uuid);
        pickupNotices.remove(uuid);
        if (settings.speedResetOnQuit()) {
            player.setWalkSpeed(DEFAULT_WALK_SPEED);
            player.setFlySpeed(DEFAULT_FLY_SPEED);
        }
    }

    // ---------------------------------------------------------------- accessors

    public GameplaySettings settings() {
        return settings;
    }

    public FlightService flight() {
        return flight;
    }

    public Cooldowns flyOthersCooldowns() {
        return flyOthersCooldowns;
    }

    public Cooldowns fireworkCooldowns() {
        return fireworkCooldowns;
    }

    Cooldowns pickupNotices() {
        return pickupNotices;
    }

    /**
     * Marks fireworks launched by /firework so their explosion deals no damage.
     */
    public NamespacedKey harmlessFireworkKey() {
        return harmlessFireworkKey;
    }
}
