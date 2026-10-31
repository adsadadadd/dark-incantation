package me.x1no4ekk.core.utility.moderation;

import me.x1no4ekk.core.CorePlugin;
import me.x1no4ekk.core.command.BaseCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * {@code /spectatestop}: returns to the position and game mode from before {@code /spectate}.
 */
final class SpectateStopCommand extends BaseCommand {

    private final ModerationModule module;

    SpectateStopCommand(CorePlugin plugin, ModerationModule module) {
        super(plugin);
        this.module = module;
    }

    @Override
    protected void execute(CommandSender sender, String label, String[] args) {
        Player spectator = playerOnly(sender);
        requirePermission(spectator, "core.spectatestop");
        if (args.length != 0) {
            throw usage("utility.spectate.stop-usage");
        }
        if (!module.spectate().stop(spectator, "utility.spectate.stopped")) {
            throw fail("utility.spectate.not-spectating");
        }
    }
}
