package me.x1no4ekk.core.travel.command;

import me.x1no4ekk.core.CorePlugin;
import me.x1no4ekk.core.command.BaseCommand;
import me.x1no4ekk.core.teleport.TeleportOptions;
import me.x1no4ekk.core.travel.TpaManager;
import me.x1no4ekk.core.travel.TravelModule;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * /tpaccept [player] — accepts a /tpa or /tpahere request. The destination is resolved when the warmup ends.
 */
public final class TpAcceptCommand extends BaseCommand {

    private static final String PERMISSION = "core.tpaccept";

    private final TravelModule module;

    public TpAcceptCommand(CorePlugin plugin, TravelModule module) {
        super(plugin);
        this.module = module;
    }

    @Override
    protected void execute(CommandSender sender, String label, String[] args) {
        Player player = playerOnly(sender);
        requirePermission(sender, PERMISSION);
        if (args.length > 1) {
            throw usage("travel.tpaccept.usage");
        }
        List<TpaManager.Request> requests = module.tpa().incoming(player.getUniqueId());
        if (requests.isEmpty()) {
            throw fail("travel.tpa.no-requests");
        }
        TpaManager.Request request = TpaRequests.select(requests, args.length == 1 ? args[0] : null,
                TpaManager.Request::senderName, "travel.tpa.multiple", "travel.tpa.no-request-from");
        module.tpa().remove(request.sender(), request.target());
        Player requester = Bukkit.getPlayer(request.sender());
        if (requester == null) {
            throw fail("general.player-not-found", placeholder("player", request.senderName()));
        }

        boolean senderTravels = request.type() == TpaManager.Type.TO_TARGET;
        Player mover = senderTravels ? requester : player;
        Player host = senderTravels ? player : requester;
        UUID moverId = mover.getUniqueId();
        UUID hostId = host.getUniqueId();
        String moverName = mover.getName();
        String hostName = host.getName();

        send(player, "travel.tpa.accepted", placeholder("player", requester.getName()));
        send(requester, "travel.tpa.accepted-sender", placeholder("player", player.getName()));

        Supplier<Location> destination = () -> {
            Player target = Bukkit.getPlayer(hostId);
            return target == null ? null : target.getLocation();
        };
        plugin.teleportService().teleport(mover, destination, TeleportOptions.DEFAULT).thenAccept(result -> {
            if (!result.isSuccess()) {
                return;
            }
            Player moved = Bukkit.getPlayer(moverId);
            if (moved != null) {
                send(moved, "travel.tpa.teleported", placeholder("player", hostName));
            }
            Player target = Bukkit.getPlayer(hostId);
            if (target != null) {
                send(target, "travel.tpa.arrived", placeholder("player", moverName));
            }
        });
    }

    @Override
    protected List<String> complete(CommandSender sender, String label, String[] args) {
        if (args.length != 1 || !(sender instanceof Player player) || !sender.hasPermission(PERMISSION)) {
            return List.of();
        }
        return filter(module.tpa().incoming(player.getUniqueId()).stream().map(TpaManager.Request::senderName), args[0]);
    }
}
