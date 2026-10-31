package me.x1no4ekk.core.utility.moderation;

import me.x1no4ekk.core.CorePlugin;
import me.x1no4ekk.core.command.BaseCommand;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

/**
 * {@code /spectate <player>}: watch a player in spectator mode; the same target again stops watching.
 */
final class SpectateCommand extends BaseCommand {

    private final ModerationModule module;

    SpectateCommand(CorePlugin plugin, ModerationModule module) {
        super(plugin);
        this.module = module;
    }

    @Override
    protected void execute(CommandSender sender, String label, String[] args) {
        Player spectator = playerOnly(sender);
        requirePermission(spectator, "core.spectate");
        if (args.length != 1) {
            throw usage("utility.spectate.usage");
        }
        Player target = player(spectator, args[0]);
        if (target.equals(spectator)) {
            throw fail("utility.spectate.self");
        }
        SpectateService spectate = module.spectate();
        if (spectate.isReturning(spectator.getUniqueId())) {
            throw fail("utility.spectate.returning");
        }
        UUID current = spectate.targetOf(spectator.getUniqueId());
        if (target.getUniqueId().equals(current)) {
            spectate.stop(spectator, "utility.spectate.stopped");
            return;
        }
        spectate.start(spectator, target);
        send(spectator, "utility.spectate.started", Placeholder.component("player", target.displayName()));
    }

    @Override
    protected List<String> complete(CommandSender sender, String label, String[] args) {
        return args.length == 1 ? players(sender, args[0]) : List.of();
    }
}
