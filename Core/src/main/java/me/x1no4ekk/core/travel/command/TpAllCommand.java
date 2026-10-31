package me.x1no4ekk.core.travel.command;

import me.x1no4ekk.core.CorePlugin;
import me.x1no4ekk.core.command.BaseCommand;
import me.x1no4ekk.core.teleport.TeleportOptions;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * /tpall — instantly brings every visible player who accepts teleports to the sender.
 */
public final class TpAllCommand extends BaseCommand {

    private static final String PERMISSION = "core.tpall";

    public TpAllCommand(CorePlugin plugin) {
        super(plugin);
    }

    @Override
    protected void execute(CommandSender sender, String label, String[] args) {
        Player player = playerOnly(sender);
        requirePermission(sender, PERMISSION);
        if (args.length != 0) {
            throw usage("travel.tpall.usage");
        }
        Location destination = player.getLocation();
        String senderName = player.getName();
        List<Player> targets = new ArrayList<>();
        int skipped = 0;
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online.equals(player) || !canSee(sender, online)) {
                continue;
            }
            if (TeleportToggles.accepts(plugin, sender, online, false)) {
                targets.add(online);
            } else {
                skipped++;
            }
        }
        if (targets.isEmpty()) {
            throw fail("travel.tpall.nobody");
        }
        for (Player target : targets) {
            UUID targetId = target.getUniqueId();
            plugin.teleportService().teleport(target, destination, TeleportOptions.INSTANT).thenAccept(result -> {
                Player moved = Bukkit.getPlayer(targetId);
                if (result.isSuccess() && moved != null) {
                    send(moved, "travel.tpall.teleported", placeholder("player", senderName));
                }
            });
        }
        send(player, "travel.tpall.done", placeholder("count", targets.size()), placeholder("skipped", skipped));
    }
}
