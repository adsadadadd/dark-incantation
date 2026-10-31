package me.x1no4ekk.core.warp;

import me.x1no4ekk.core.CorePlugin;
import me.x1no4ekk.core.user.User;
import me.x1no4ekk.core.util.Numbers;
import me.x1no4ekk.core.util.Text;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.time.Instant;
import java.util.Locale;

/**
 * Placeholders describing a warp in menus and /warpinfo: {@code <warp> <display> <owner> <world> <x> <y> <z>
 * <visits> <rating> <votes> <date> <description> <players>}.
 */
public final class WarpPlaceholders {

    private WarpPlaceholders() {
    }

    /**
     * Must be called on the main thread (counts nearby players).
     */
    public static TagResolver of(CorePlugin plugin, Warp warp, WarpRating rating, WarpSettings config) {
        String description = warp.description();
        return TagResolver.resolver(
                Placeholder.unparsed("warp", warp.name()),
                Placeholder.component("display", display(warp, config)),
                Placeholder.component("owner", owner(warp, config)),
                Placeholder.unparsed("world", warp.world()),
                Placeholder.unparsed("x", String.valueOf((long) Math.floor(warp.x()))),
                Placeholder.unparsed("y", String.valueOf((long) Math.floor(warp.y()))),
                Placeholder.unparsed("z", String.valueOf((long) Math.floor(warp.z()))),
                Placeholder.unparsed("visits", Numbers.format(warp.visits())),
                Placeholder.component("rating", rating(rating, config)),
                Placeholder.unparsed("votes", String.valueOf(rating.count())),
                Placeholder.unparsed("date", config.dateFormat().format(Instant.ofEpochMilli(warp.createdAt()))),
                description == null || description.isBlank()
                        ? Placeholder.component("description", Text.parse(config.noDescription()))
                        : Placeholder.unparsed("description", description),
                Placeholder.unparsed("players", String.valueOf(nearby(plugin, warp, config.nearbyRadius()))));
    }

    /**
     * Configured display name of a system warp, or the plain name of a player warp.
     */
    public static Component display(Warp warp, WarpSettings config) {
        if (!warp.system()) {
            return Component.text(warp.name());
        }
        String custom = config.systemDisplayNames().get(warp.key());
        return custom != null ? Text.parse(custom)
                : Text.parse(config.systemNameFormat(), Placeholder.unparsed("name", warp.name()));
    }

    private static Component owner(Warp warp, WarpSettings config) {
        if (warp.system() || warp.owner() == null) {
            return Text.parse(config.serverOwner());
        }
        return Component.text(warp.ownerName() == null ? "?" : warp.ownerName());
    }

    public static Component rating(WarpRating rating, WarpSettings config) {
        if (rating.count() == 0) {
            return Text.parse(config.noRating());
        }
        return Text.parse(config.ratingFormat(),
                Placeholder.unparsed("rating", String.format(Locale.ROOT, "%.1f", rating.average())),
                Placeholder.unparsed("votes", String.valueOf(rating.count())));
    }

    /**
     * Visible (non-vanished) players within {@code radius} blocks of the warp.
     */
    private static int nearby(CorePlugin plugin, Warp warp, int radius) {
        if (radius <= 0) {
            return 0;
        }
        double radiusSq = (double) radius * radius;
        int count = 0;
        for (Player online : Bukkit.getOnlinePlayers()) {
            Location at = online.getLocation();
            if (!at.getWorld().getName().equals(warp.world())) {
                continue;
            }
            double dx = at.getX() - warp.x();
            double dy = at.getY() - warp.y();
            double dz = at.getZ() - warp.z();
            if (dx * dx + dy * dy + dz * dz > radiusSq) {
                continue;
            }
            User user = plugin.userManager().getUser(online);
            if (user == null || !user.isVanish()) {
                count++;
            }
        }
        return count;
    }
}
