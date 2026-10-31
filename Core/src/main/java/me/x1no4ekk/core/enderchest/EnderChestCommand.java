package me.x1no4ekk.core.enderchest;

import me.x1no4ekk.core.CorePlugin;
import me.x1no4ekk.core.command.BaseCommand;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

/**
 * {@code /enderchest [player]} — own chest, or another player's chest (also offline) for staff.
 */
final class EnderChestCommand extends BaseCommand {

    private final EnderChestService service;

    EnderChestCommand(CorePlugin plugin, EnderChestService service) {
        super(plugin);
        this.service = service;
    }

    @Override
    protected void execute(CommandSender sender, String label, String[] args) {
        Player player = playerOnly(sender);
        requirePermission(sender, EnderChestService.PERMISSION);
        if (args.length == 0) {
            service.open(player, player.getUniqueId(), player.getName());
            return;
        }
        if (args.length > 1) {
            throw usage("enderchest.usage");
        }
        requirePermission(sender, EnderChestService.OTHERS_PERMISSION);
        String name = args[0];
        if (name.equalsIgnoreCase(player.getName())) {
            service.open(player, player.getUniqueId(), player.getName());
            return;
        }
        Player online = Bukkit.getPlayerExact(name);
        if (online != null && canSee(player, online)) {
            service.open(player, online.getUniqueId(), online.getName());
            return;
        }
        UUID viewerId = player.getUniqueId();
        plugin.userManager().findByName(name).whenCompleteAsync((result, error) -> {
            Player viewer = Bukkit.getPlayer(viewerId);
            if (error != null) {
                plugin.getLogger().log(Level.SEVERE, "Failed to look up player '" + name + "'", error);
                if (viewer != null) {
                    send(viewer, "general.database-error");
                }
                return;
            }
            if (viewer == null) {
                return;
            }
            if (result.isEmpty()) {
                send(viewer, "general.player-not-found", placeholder("player", name));
                return;
            }
            Map.Entry<UUID, String> found = result.get();
            service.open(viewer, found.getKey(), found.getValue());
        }, plugin.mainThread());
    }

    @Override
    protected List<String> complete(CommandSender sender, String label, String[] args) {
        if (args.length == 1 && sender.hasPermission(EnderChestService.OTHERS_PERMISSION)) {
            return players(sender, args[0]);
        }
        return List.of();
    }
}
