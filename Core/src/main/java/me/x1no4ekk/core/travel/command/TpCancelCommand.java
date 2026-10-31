package me.x1no4ekk.core.travel.command;

import me.x1no4ekk.core.CorePlugin;
import me.x1no4ekk.core.command.BaseCommand;
import me.x1no4ekk.core.travel.TpaManager;
import me.x1no4ekk.core.travel.TravelModule;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * /tpcancel [player] — withdraws the sender's outgoing request(s); without a name all of them.
 */
public final class TpCancelCommand extends BaseCommand {

    private static final String PERMISSION = "core.tpcancel";

    private final TravelModule module;

    public TpCancelCommand(CorePlugin plugin, TravelModule module) {
        super(plugin);
        this.module = module;
    }

    @Override
    protected void execute(CommandSender sender, String label, String[] args) {
        Player player = playerOnly(sender);
        requirePermission(sender, PERMISSION);
        if (args.length > 1) {
            throw usage("travel.tpcancel.usage");
        }
        List<TpaManager.Request> requests = module.tpa().outgoing(player.getUniqueId());
        if (requests.isEmpty()) {
            throw fail("travel.tpa.no-outgoing");
        }
        List<TpaManager.Request> cancelled = args.length == 0 ? requests
                : List.of(TpaRequests.select(requests, args[0], TpaManager.Request::targetName,
                "travel.tpa.multiple", "travel.tpa.no-request-to"));
        for (TpaManager.Request request : cancelled) {
            module.tpa().remove(request.sender(), request.target());
            send(player, "travel.tpa.cancelled", placeholder("player", request.targetName()));
            Player target = Bukkit.getPlayer(request.target());
            if (target != null) {
                send(target, "travel.tpa.cancelled-target", placeholder("player", player.getName()));
            }
        }
    }

    @Override
    protected List<String> complete(CommandSender sender, String label, String[] args) {
        if (args.length != 1 || !(sender instanceof Player player) || !sender.hasPermission(PERMISSION)) {
            return List.of();
        }
        return filter(module.tpa().outgoing(player.getUniqueId()).stream().map(TpaManager.Request::targetName), args[0]);
    }
}
