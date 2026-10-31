package me.x1no4ekk.core.utility.info;

import me.x1no4ekk.core.CorePlugin;
import me.x1no4ekk.core.command.BaseCommand;
import me.x1no4ekk.core.utility.UtilityText;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * {@code /compass}: the cardinal direction the player is facing.
 */
final class CompassCommand extends BaseCommand {

    CompassCommand(CorePlugin plugin) {
        super(plugin);
    }

    @Override
    protected void execute(CommandSender sender, String label, String[] args) {
        Player player = playerOnly(sender);
        requirePermission(player, "core.compass");
        float yaw = player.getLocation().getYaw();
        send(player, "utility.compass.message",
                Placeholder.component("direction", UtilityText.cardinal(messages(), yaw)),
                placeholder("degrees", UtilityText.bearing(yaw)));
    }
}
