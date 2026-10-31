package me.x1no4ekk.core.api;

import me.x1no4ekk.core.CorePlugin;
import me.x1no4ekk.core.hook.EconomyHook;
import me.x1no4ekk.core.teleport.TeleportOptions;
import me.x1no4ekk.core.teleport.TeleportResult;
import me.x1no4ekk.core.user.User;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * {@link CoreAPI} backed by the plugin's services.
 */
public final class CoreApiImpl implements CoreAPI {

    private final CorePlugin plugin;

    public CoreApiImpl(CorePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean isInCombat(UUID player) {
        return plugin.combatManager().isInCombat(player);
    }

    @Override
    public int getCombatSecondsLeft(UUID player) {
        return plugin.combatManager().secondsLeft(player);
    }

    @Override
    public CompletableFuture<TeleportResult> teleport(Player player, Location destination, TeleportOptions options) {
        return plugin.teleportService().teleport(player, destination, options);
    }

    @Override
    public CompletableFuture<TeleportResult> teleport(Player player, Supplier<Location> destination, TeleportOptions options) {
        return plugin.teleportService().teleport(player, destination, options);
    }

    @Override
    public boolean isTeleporting(UUID player) {
        return plugin.teleportService().isTeleporting(player);
    }

    @Override
    public void recordBackLocation(Player player) {
        plugin.teleportService().recordBack(player);
    }

    @Override
    public boolean isVanished(UUID player) {
        User user = plugin.userManager().getUser(player);
        return user != null && user.isVanish();
    }

    @Override
    public boolean isGodMode(UUID player) {
        User user = plugin.userManager().getUser(player);
        return user != null && user.isGod();
    }

    @Override
    public @Nullable User getUser(UUID player) {
        return plugin.userManager().getUser(player);
    }

    @Override
    public EconomyHook economy() {
        return plugin.hooks().economy();
    }
}
