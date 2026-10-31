package me.x1no4ekk.core.travel.command;

import me.x1no4ekk.core.CorePlugin;
import me.x1no4ekk.core.command.BaseCommand;
import me.x1no4ekk.core.teleport.TeleportOptions;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

/**
 * /tphere &lt;player&gt; — instantly brings a player to the sender.
 */
public final class TpHereCommand extends BaseCommand {

    private static final String PERMISSION = "core.tphere";

    public TpHereCommand(CorePlugin plugin) {
        super(plugin);
    }

    @Override
    protected void execute(CommandSender sender, String label, String[] args) {
        Player player = playerOnly(sender);
        requirePermission(sender, PERMISSION);
        if (args.length != 1) {
            throw usage("travel.tphere.usage");
        }
        Player target = player(sender, args[0]);
        if (target.equals(player)) {
            throw fail("travel.tp.self");
        }
        if (!TeleportToggles.accepts(plugin, sender, target, false)) {
            throw fail("travel.tp-disabled", placeholder("player", target.getName()));
        }
        UUID senderId = player.getUniqueId();
        UUID targetId = target.getUniqueId();
        String senderName = player.getName();
        String targetName = target.getName();
        plugin.teleportService().teleport(target, player.getLocation(), TeleportOptions.INSTANT).thenAccept(result -> {
            if (!result.isSuccess()) {
                return;
            }
            Player moved = Bukkit.getPlayer(targetId);
            if (moved != null) {
                send(moved, "travel.tphere.teleported", placeholder("player", senderName));
            }
            Player self = Bukkit.getPlayer(senderId);
            if (self != null) {
                send(self, "travel.tphere.done", placeholder("player", targetName));
            }
        });
    }

    @Override
    protected List<String> complete(CommandSender sender, String label, String[] args) {
        return args.length == 1 && sender.hasPermission(PERMISSION) ? players(sender, args[0]) : List.of();
    }
}
