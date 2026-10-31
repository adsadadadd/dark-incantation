package me.x1no4ekk.core.utility.moderation;

import me.x1no4ekk.core.CorePlugin;
import me.x1no4ekk.core.user.User;
import me.x1no4ekk.core.utility.info.InfoModule;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Vanish on top of {@link Player#hidePlayer}: the flag lives in {@link User#isVanish()} (persisted),
 * so this class keeps no state of its own.
 */
public final class VanishService {

    public static final String SEE_PERMISSION = "core.vanish.see";

    private final CorePlugin plugin;
    private final ModerationModule module;

    VanishService(CorePlugin plugin, ModerationModule module) {
        this.plugin = plugin;
        this.module = module;
    }

    /**
     * Thread-safe: reads the volatile user flag only.
     */
    public boolean isVanished(UUID uuid) {
        User user = plugin.userManager().getUser(uuid);
        return user != null && user.isVanish();
    }

    /**
     * Changes the flag and updates who can see the player.
     */
    void setVanished(Player player, User user, boolean vanished) {
        user.setVanish(vanished);
        boolean fake = module.settings().vanish().fakeMessages();
        for (Player viewer : Bukkit.getOnlinePlayers()) {
            if (viewer.equals(player)) {
                continue;
            }
            boolean affected = !viewer.hasPermission(SEE_PERMISSION);
            if (vanished && affected) {
                viewer.hidePlayer(plugin, player);
            } else {
                viewer.showPlayer(plugin, player);
            }
            if (fake && affected) {
                plugin.messages().send(viewer, vanished ? "utility.vanish.fake-quit" : "utility.vanish.fake-join",
                        Placeholder.component("player", player.displayName()));
            }
        }
    }

    /**
     * Re-applies the visibility of {@code player} for every viewer (join, late enable).
     * Showing a non-vanished player also clears hides left over from a previous session.
     */
    void refresh(Player player, boolean vanished) {
        for (Player viewer : Bukkit.getOnlinePlayers()) {
            if (viewer.equals(player)) {
                continue;
            }
            if (vanished && !viewer.hasPermission(SEE_PERMISSION)) {
                viewer.hidePlayer(plugin, player);
            } else {
                viewer.showPlayer(plugin, player);
            }
        }
    }

    /**
     * Hides every vanished player from a viewer that has just joined.
     */
    void hideVanishedFrom(Player viewer) {
        if (viewer.hasPermission(SEE_PERMISSION)) {
            return;
        }
        for (Player other : Bukkit.getOnlinePlayers()) {
            if (!other.equals(viewer) && isVanished(other.getUniqueId())) {
                viewer.hidePlayer(plugin, other);
            }
        }
    }

    /**
     * Action-bar reminder for vanished players; skipped while the GPS uses the action bar.
     */
    void remind() {
        InfoModule info = plugin.module(InfoModule.class);
        for (Player player : Bukkit.getOnlinePlayers()) {
            UUID uuid = player.getUniqueId();
            if (isVanished(uuid) && (info == null || !info.isGpsActive(uuid))) {
                plugin.messages().sendActionBar(player, "utility.vanish.action-bar");
            }
        }
    }

    /**
     * Makes every vanished player visible again (module disable).
     */
    void showAll() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (isVanished(player.getUniqueId())) {
                for (Player viewer : Bukkit.getOnlinePlayers()) {
                    if (!viewer.equals(player)) {
                        viewer.showPlayer(plugin, player);
                    }
                }
            }
        }
    }
}
