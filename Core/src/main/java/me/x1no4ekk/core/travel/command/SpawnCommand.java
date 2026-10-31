package me.x1no4ekk.core.travel.command;

import me.x1no4ekk.core.CorePlugin;
import me.x1no4ekk.core.command.BaseCommand;
import me.x1no4ekk.core.teleport.TeleportOptions;
import me.x1no4ekk.core.travel.TravelModule;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

/**
 * /spawn [player] — teleports to the server spawn; sending another player is an instant staff teleport.
 */
public final class SpawnCommand extends BaseCommand {

    private static final String PERMISSION = "core.spawn";
    private static final String OTHERS_PERMISSION = "core.spawn.others";

    private final TravelModule module;

    public SpawnCommand(CorePlugin plugin, TravelModule module) {
        super(plugin);
        this.module = module;
    }

    @Override
    protected void execute(CommandSender sender, String label, String[] args) {
        requirePermission(sender, PERMISSION);
        if (args.length > 1) {
            throw usage("travel.spawn.usage");
        }
        Player target = args.length == 1 ? otherPlayer(sender, args[0], OTHERS_PERMISSION) : playerOnly(sender);
        if (module.spawn().get() == null) {
            throw fail("travel.spawn.not-set");
        }
        Location spawn = module.spawn().location();
        if (spawn == null) {
            throw fail("travel.spawn.world-unavailable");
        }
        boolean self = target.equals(sender);
        UUID targetId = target.getUniqueId();
        UUID senderId = sender instanceof Player player ? player.getUniqueId() : null;
        String targetName = target.getName();
        TeleportOptions options = self ? TeleportOptions.DEFAULT : TeleportOptions.INSTANT;
        plugin.teleportService().teleport(target, spawn, options).thenAccept(result -> {
            if (!result.isSuccess()) {
                return;
            }
            Player moved = Bukkit.getPlayer(targetId);
            if (moved != null) {
                send(moved, "travel.spawn.teleported");
            }
            CommandSender actor = senderId == null ? Bukkit.getConsoleSender() : Bukkit.getPlayer(senderId);
            if (!self && actor != null) {
                send(actor, "travel.spawn.teleported-other", placeholder("player", targetName));
            }
        });
    }

    @Override
    protected List<String> complete(CommandSender sender, String label, String[] args) {
        return args.length == 1 && sender.hasPermission(OTHERS_PERMISSION) ? players(sender, args[0]) : List.of();
    }
}
