package me.x1no4ekk.core.combat;

import me.x1no4ekk.core.CorePlugin;
import me.x1no4ekk.core.config.ConfigFiles;
import me.x1no4ekk.core.config.ConfigUtil;
import me.x1no4ekk.core.module.CoreModule;
import me.x1no4ekk.core.teleport.TeleportResult;
import me.x1no4ekk.core.user.User;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.scheduler.BukkitTask;

import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * Combat tag (anti-relog): PvP marks both players for {@code combat.duration-seconds}. While tagged, listed commands
 * and teleports are blocked and flight is disabled; leaving the server is punished.
 * <p>
 * State is a UUID → expiry map (thread-safe, because the API may be queried from async threads), cleared on quit,
 * death and disable. A single 1-second task shows the countdown and expires tags.
 */
public final class CombatManager implements CoreModule, Listener {

    public static final String BYPASS_PERMISSION = "core.combat.bypass";

    private final CorePlugin plugin;
    private volatile CombatSettings settings;
    private final Map<UUID, Long> taggedUntil = new ConcurrentHashMap<>();
    private BukkitTask ticker;

    public CombatManager(CorePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public String id() {
        return "combat";
    }

    @Override
    public Runnable loadConfig(ConfigFiles files) throws Exception {
        CombatSettings loaded = CombatSettings.load(ConfigUtil.section(files.settings(), "combat"));
        return () -> {
            this.settings = loaded;
            if (!loaded.enabled()) {
                taggedUntil.clear();
            }
        };
    }

    @Override
    public void enable() {
        Bukkit.getPluginManager().registerEvents(this, plugin);
        ticker = Bukkit.getScheduler().runTaskTimer(plugin, this::tick, 20L, 20L);
    }

    @Override
    public void disable() {
        if (ticker != null) {
            ticker.cancel();
            ticker = null;
        }
        taggedUntil.clear();
    }

    // ---------------------------------------------------------------- API

    public boolean isInCombat(UUID uuid) {
        Long until = taggedUntil.get(uuid);
        return until != null && until > System.currentTimeMillis();
    }

    /**
     * @return whole seconds of combat left (0 if not in combat)
     */
    public int secondsLeft(UUID uuid) {
        Long until = taggedUntil.get(uuid);
        if (until == null) {
            return 0;
        }
        long left = until - System.currentTimeMillis();
        return left <= 0 ? 0 : (int) ((left + 999) / 1000);
    }

    /**
     * Tags a player (main thread). Players in creative/spectator, with the bypass permission or in disabled worlds
     * are ignored.
     */
    public void tag(Player player) {
        CombatSettings config = settings;
        if (!config.enabled() || player.hasPermission(BYPASS_PERMISSION)
                || player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR
                || config.disabledWorlds().contains(player.getWorld().getName().toLowerCase(Locale.ROOT))) {
            return;
        }
        UUID uuid = player.getUniqueId();
        boolean wasTagged = isInCombat(uuid);
        taggedUntil.put(uuid, System.currentTimeMillis() + config.durationSeconds() * 1000L);
        if (wasTagged) {
            return;
        }
        plugin.messages().send(player, "combat.tagged", Placeholder.unparsed("seconds", String.valueOf(config.durationSeconds())));
        if (config.cancelTeleport()) {
            plugin.teleportService().cancel(uuid, TeleportResult.CANCELLED_COMBAT, "teleport.cancelled-combat");
        }
        if (config.disableFly() && (player.isFlying() || player.getAllowFlight())) {
            player.setFlying(false);
            player.setAllowFlight(false);
            plugin.messages().send(player, "combat.fly-disabled");
        }
        if (config.disableGliding() && player.isGliding()) {
            player.setGliding(false);
        }
    }

    /**
     * Removes the tag without the "combat ended" message.
     */
    public void untag(UUID uuid) {
        taggedUntil.remove(uuid);
    }

    // ---------------------------------------------------------------- ticker

    private void tick() {
        if (taggedUntil.isEmpty()) {
            return;
        }
        long now = System.currentTimeMillis();
        boolean actionBar = settings.actionBar();
        Iterator<Map.Entry<UUID, Long>> iterator = taggedUntil.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, Long> entry = iterator.next();
            Player player = Bukkit.getPlayer(entry.getKey());
            if (player == null) {
                iterator.remove();
                continue;
            }
            long left = entry.getValue() - now;
            if (left <= 0) {
                iterator.remove();
                onCombatEnd(player);
            } else if (actionBar) {
                plugin.messages().sendActionBar(player, "combat.action-bar",
                        Placeholder.unparsed("seconds", String.valueOf((left + 999) / 1000)));
            }
        }
    }

    private void onCombatEnd(Player player) {
        plugin.messages().send(player, "combat.ended");
        if (actionBarEnabled()) {
            plugin.messages().sendActionBar(player, "combat.action-bar-ended");
        }
        // Give back flight that was taken away when the fight started.
        User user = plugin.userManager().getUser(player);
        if (settings.disableFly() && user != null && user.isFly() && player.hasPermission("core.fly")
                && (player.getGameMode() == GameMode.SURVIVAL || player.getGameMode() == GameMode.ADVENTURE)) {
            player.setAllowFlight(true);
        }
    }

    private boolean actionBarEnabled() {
        return settings.actionBar();
    }

    // ---------------------------------------------------------------- listeners

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!settings.enabled() || !(event.getEntity() instanceof Player victim)) {
            return;
        }
        Player attacker = resolveAttacker(event.getDamager());
        if (attacker == null || attacker.equals(victim)) {
            return;
        }
        tag(attacker);
        tag(victim);
    }

    private static Player resolveAttacker(Entity damager) {
        if (damager instanceof Player player) {
            return player;
        }
        if (damager instanceof Projectile projectile) {
            ProjectileSource shooter = projectile.getShooter();
            return shooter instanceof Player player ? player : null;
        }
        if (damager instanceof TNTPrimed tnt) {
            return tnt.getSource() instanceof Player player ? player : null;
        }
        if (damager instanceof AreaEffectCloud cloud) {
            return cloud.getSource() instanceof Player player ? player : null;
        }
        return null;
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        if (!isInCombat(player.getUniqueId())) {
            return;
        }
        String line = event.getMessage();
        if (line.startsWith("/")) {
            line = line.substring(1);
        }
        if (settings.isBlocked(line)) {
            event.setCancelled(true);
            plugin.messages().send(player, "combat.command-blocked",
                    Placeholder.unparsed("seconds", String.valueOf(secondsLeft(player.getUniqueId()))));
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onToggleFlight(PlayerToggleFlightEvent event) {
        Player player = event.getPlayer();
        if (event.isFlying() && settings.disableFly() && isInCombat(player.getUniqueId())) {
            event.setCancelled(true);
            player.setAllowFlight(false);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onGlide(EntityToggleGlideEvent event) {
        if (event.isGliding() && settings.disableGliding() && event.getEntity() instanceof Player player
                && isInCombat(player.getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDeath(PlayerDeathEvent event) {
        taggedUntil.remove(event.getEntity().getUniqueId());
    }

    /**
     * Runs before the user is unloaded (MONITOR), so the death below is still recorded in the cached stats.
     */
    @EventHandler(priority = EventPriority.LOW)
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        boolean inCombat = isInCombat(uuid);
        taggedUntil.remove(uuid);
        CombatSettings config = settings;
        if (!inCombat || !config.punishEnabled() || !config.punishReasons().contains(event.getReason())) {
            return;
        }
        if (config.punishKill() && !player.isDead()) {
            player.setHealth(0.0);
        }
        if (config.punishBroadcast()) {
            plugin.messages().broadcast("combat.logout-broadcast", Placeholder.unparsed("player", player.getName()));
        }
        for (String command : config.punishCommands()) {
            String line = command.replace("%player%", player.getName()).replace("%uuid%", uuid.toString());
            try {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), line.startsWith("/") ? line.substring(1) : line);
            } catch (RuntimeException e) {
                plugin.getLogger().log(Level.WARNING, "Combat-logout command failed: " + line, e);
            }
        }
    }
}
