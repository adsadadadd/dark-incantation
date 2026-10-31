package me.x1no4ekk.core.travel.command;

import me.x1no4ekk.core.CorePlugin;
import me.x1no4ekk.core.travel.TravelModule;
import me.x1no4ekk.core.user.User;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Shared {@code /tptoggle} check for staff teleports and requests.
 */
final class TeleportToggles {

    private TeleportToggles() {
    }

    /**
     * Whether {@code actor} may teleport to/with {@code target}: the target accepts teleports
     * (and, for requests, does not ignore the actor), or the actor has the bypass permission.
     */
    static boolean accepts(CorePlugin plugin, CommandSender actor, Player target, boolean checkIgnore) {
        if (actor.equals(target) || actor.hasPermission(TravelModule.BYPASS_TOGGLE_PERMISSION)) {
            return true;
        }
        User user = plugin.userManager().getUser(target);
        if (user == null) {
            return true;
        }
        if (!user.isTpToggle()) {
            return false;
        }
        return !checkIgnore || !(actor instanceof Player player) || !user.isIgnoring(player.getUniqueId());
    }
}
