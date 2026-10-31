package me.x1no4ekk.core.gameplay;

import me.x1no4ekk.core.CorePlugin;
import me.x1no4ekk.core.user.User;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Fly mode ({@link User#isFly()}, persisted) and the elytra flight mode (in memory).
 * <p>
 * Elytra flight: while enabled and an intact elytra is worn, a survival player may fly as with /fly; every second
 * in the air costs elytra durability. State is a UUID set used on the main thread only, dropped on quit and disable.
 */
public final class FlightService {

    public static final String FLY_PERMISSION = "core.fly";

    private final CorePlugin plugin;
    private final GameplayModule module;
    private final Set<UUID> elytraFly = new HashSet<>();

    FlightService(CorePlugin plugin, GameplayModule module) {
        this.plugin = plugin;
        this.module = module;
    }

    /**
     * Creative and spectator players can always fly; flight is managed only in survival and adventure.
     */
    public static boolean survivalLike(Player player) {
        GameMode mode = player.getGameMode();
        return mode == GameMode.SURVIVAL || mode == GameMode.ADVENTURE;
    }

    // ---------------------------------------------------------------- /fly

    public void setFly(Player player, User user, boolean enabled) {
        user.setFly(enabled);
        if (!survivalLike(player)) {
            return;
        }
        if (enabled) {
            player.setAllowFlight(true);
        } else {
            player.setFlying(false);
            player.setAllowFlight(false);
            // An active elytra mode takes over again on the next tick.
        }
    }

    /**
     * Gives flight back after join, respawn, world or game mode change (Bukkit resets it).
     *
     * @param liftIfAirborne start flying right away when there is no block below (safe login)
     */
    public void restoreFly(Player player, boolean liftIfAirborne) {
        User user = plugin.userManager().getUser(player);
        if (user == null || !user.isFly() || !survivalLike(player) || !player.hasPermission(FLY_PERMISSION)
                || plugin.combatManager().isInCombat(player.getUniqueId())) {
            return;
        }
        player.setAllowFlight(true);
        if (liftIfAirborne && player.getLocation().getBlock().getRelative(BlockFace.DOWN).isPassable()) {
            player.setFlying(true);
        }
    }

    /**
     * Restores flight on the next tick, after the server has applied the new game mode / world.
     */
    public void scheduleRestore(UUID uuid) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                restoreFly(player, false);
                if (elytraFly.contains(uuid)) {
                    tickElytra(player);
                }
            }
        });
    }

    // ---------------------------------------------------------------- /flyelytra

    public boolean isElytraFly(UUID uuid) {
        return elytraFly.contains(uuid);
    }

    public void setElytraFly(Player player, boolean enabled) {
        if (enabled) {
            elytraFly.add(player.getUniqueId());
            tickElytra(player);
        } else {
            elytraFly.remove(player.getUniqueId());
            revokeElytraFlight(player);
        }
    }

    /**
     * Called every second by the module ticker.
     */
    void tickElytra() {
        if (elytraFly.isEmpty()) {
            return;
        }
        Iterator<UUID> iterator = elytraFly.iterator();
        while (iterator.hasNext()) {
            Player player = Bukkit.getPlayer(iterator.next());
            if (player == null) {
                iterator.remove();
                continue;
            }
            tickElytra(player);
        }
    }

    private void tickElytra(Player player) {
        if (!survivalLike(player)) {
            return;
        }
        User user = plugin.userManager().getUser(player);
        if (user != null && user.isFly()) {
            return; // regular /fly is in charge
        }
        GameplaySettings.FlyElytra config = module.settings().flyElytra();
        boolean blocked = !config.enabled()
                || config.disableInCombat() && plugin.combatManager().isInCombat(player.getUniqueId());
        ItemStack elytra = player.getInventory().getChestplate();
        if (blocked || !isUsableElytra(elytra)) {
            if (player.getAllowFlight()) {
                player.setFlying(false);
                player.setAllowFlight(false);
            }
            return;
        }
        if (!player.getAllowFlight()) {
            player.setAllowFlight(true);
        }
        if (player.isFlying() && !wearElytra(player, elytra, config.durabilityPerSecond())) {
            player.setFlying(false);
            player.setAllowFlight(false);
            plugin.messages().send(player, "gameplay.flyelytra.broken");
        }
    }

    private static boolean isUsableElytra(ItemStack item) {
        if (item == null || item.getType() != Material.ELYTRA) {
            return false;
        }
        if (!(item.getItemMeta() instanceof Damageable meta) || meta.isUnbreakable()) {
            return true;
        }
        return meta.getDamage() < maxDamage(item, meta) - 1;
    }

    /**
     * Takes durability like vanilla gliding (Unbreaking may save a point). The elytra never breaks completely:
     * it stops at 1 durability, as a broken vanilla elytra does.
     *
     * @return {@code false} if the elytra is now too damaged to fly
     */
    private static boolean wearElytra(Player player, ItemStack elytra, int amount) {
        if (amount <= 0 || !(elytra.getItemMeta() instanceof Damageable meta) || meta.isUnbreakable()) {
            return true;
        }
        int unbreaking = elytra.getEnchantmentLevel(Enchantment.UNBREAKING);
        int damage = 0;
        for (int i = 0; i < amount; i++) {
            if (unbreaking == 0 || ThreadLocalRandom.current().nextInt(unbreaking + 1) == 0) {
                damage++;
            }
        }
        int limit = maxDamage(elytra, meta) - 1;
        if (damage > 0) {
            meta.setDamage(Math.min(meta.getDamage() + damage, limit));
            elytra.setItemMeta(meta);
            player.getInventory().setChestplate(elytra);
        }
        return meta.getDamage() < limit;
    }

    private static int maxDamage(ItemStack item, Damageable meta) {
        return meta.hasMaxDamage() ? meta.getMaxDamage() : item.getType().getMaxDurability();
    }

    private void revokeElytraFlight(Player player) {
        User user = plugin.userManager().getUser(player);
        if (survivalLike(player) && (user == null || !user.isFly()) && player.getAllowFlight()) {
            player.setFlying(false);
            player.setAllowFlight(false);
        }
    }

    // ---------------------------------------------------------------- lifecycle

    void remove(UUID uuid) {
        elytraFly.remove(uuid);
    }

    /**
     * Turns elytra flight off for everyone (module disable or the feature switched off on reload).
     */
    void clear() {
        for (UUID uuid : elytraFly) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                revokeElytraFlight(player);
            }
        }
        elytraFly.clear();
    }
}
