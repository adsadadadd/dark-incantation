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
 * /tp &lt;player&gt; and /tp &lt;player&gt; &lt;target&gt; — instant staff teleport.
 */
public final class TpCommand extends BaseCommand {

    private static final String PERMISSION = "core.tp";
    private static final String OTHERS_PERMISSION = "core.tp.others";

    public TpCommand(CorePlugin plugin) {
        super(plugin);
    }

    @Override
    protected void execute(CommandSender sender, String label, String[] args) {
        requirePermission(sender, PERMISSION);
        if (args.length == 1) {
            Player player = playerOnly(sender);
            Player target = player(sender, args[0]);
            if (target.equals(player)) {
                throw fail("travel.tp.self");
            }
            checkToggle(sender, target);
            String targetName = target.getName();
            UUID uuid = player.getUniqueId();
            plugin.teleportService().teleport(player, target.getLocation(), TeleportOptions.INSTANT).thenAccept(result -> {
                Player online = Bukkit.getPlayer(uuid);
                if (result.isSuccess() && online != null) {
                    send(online, "travel.tp.teleported", placeholder("player", targetName));
                }
            });
            return;
        }
        if (args.length != 2) {
            throw usage("travel.tp.usage");
        }
        requirePermission(sender, OTHERS_PERMISSION);
        Player who = player(sender, args[0]);
        Player target = player(sender, args[1]);
        if (who.equals(target)) {
            throw fail("travel.tp.same-player");
        }
        checkToggle(sender, target);
        String whoName = who.getName();
        String targetName = target.getName();
        UUID whoId = who.getUniqueId();
        boolean self = who.equals(sender);
        UUID senderId = sender instanceof Player player ? player.getUniqueId() : null;
        plugin.teleportService().teleport(who, target.getLocation(), TeleportOptions.INSTANT).thenAccept(result -> {
            if (!result.isSuccess()) {
                return;
            }
            Player moved = Bukkit.getPlayer(whoId);
            if (moved != null) {
                send(moved, "travel.tp.teleported", placeholder("player", targetName));
            }
            CommandSender actor = senderId == null ? Bukkit.getConsoleSender() : Bukkit.getPlayer(senderId);
            if (!self && actor != null) {
                send(actor, "travel.tp.teleported-other", placeholder("player", whoName), placeholder("target", targetName));
            }
        });
    }

    private void checkToggle(CommandSender sender, Player target) {
        if (!TeleportToggles.accepts(plugin, sender, target, false)) {
            throw fail("travel.tp-disabled", placeholder("player", target.getName()));
        }
    }

    @Override
    protected List<String> complete(CommandSender sender, String label, String[] args) {
        if (!sender.hasPermission(PERMISSION)) {
            return List.of();
        }
        if (args.length == 1 || (args.length == 2 && sender.hasPermission(OTHERS_PERMISSION))) {
            return players(sender, args[args.length - 1]);
        }
        return List.of();
    }
}
