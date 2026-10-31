package me.x1no4ekk.core.api;

import me.x1no4ekk.core.hook.EconomyHook;
import me.x1no4ekk.core.teleport.TeleportOptions;
import me.x1no4ekk.core.teleport.TeleportResult;
import me.x1no4ekk.core.user.User;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * Public API of Core for other plugins (Guilds, Economy). Registered in Bukkit's ServicesManager while Core is enabled.
 * <pre>
 * CoreAPI core = CoreAPI.get();
 * core.teleport(player, guildHome, TeleportOptions.DEFAULT).thenAccept(result -&gt; ...);
 * </pre>
 * Do not keep the instance across a Core reload/disable; call {@link #get()} when needed.
 */
public interface CoreAPI {

    /**
     * @throws IllegalStateException if Core is not enabled
     */
    static CoreAPI get() {
        CoreAPI api = Bukkit.getServicesManager().load(CoreAPI.class);
        if (api == null) {
            throw new IllegalStateException("Core is not enabled");
        }
        return api;
    }

    /**
     * Whether the player is combat-tagged (thread-safe).
     */
    boolean isInCombat(UUID player);

    /**
     * Whole seconds of combat left, 0 if not in combat (thread-safe).
     */
    int getCombatSecondsLeft(UUID player);

    /**
     * Teleport with Core's warmup, combat check and /back handling. Main thread recommended;
     * the future completes on the main thread. Every result except SUCCESS is already reported to the player.
     */
    CompletableFuture<TeleportResult> teleport(Player player, Location destination, TeleportOptions options);

    /**
     * Like {@link #teleport(Player, Location, TeleportOptions)} but the destination is resolved when the warmup ends.
     */
    CompletableFuture<TeleportResult> teleport(Player player, Supplier<Location> destination, TeleportOptions options);

    boolean isTeleporting(UUID player);

    /**
     * Stores the player's current position as the /back location.
     */
    void recordBackLocation(Player player);

    boolean isVanished(UUID player);

    boolean isGodMode(UUID player);

    /**
     * Cached data of an online player, or {@code null}.
     */
    @Nullable
    User getUser(UUID player);

    /**
     * Vault economy bridge (АРЫ).
     */
    EconomyHook economy();
}
