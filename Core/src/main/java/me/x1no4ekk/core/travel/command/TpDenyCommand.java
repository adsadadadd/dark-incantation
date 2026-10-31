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
 * /tpdeny [player] — rejects an incoming request.
 */
public final class TpDenyCommand extends BaseCommand {

    private static final String PERMISSION = "core.tpdeny";

    private final TravelModule module;

    public TpDenyCommand(CorePlugin plugin, TravelModule module) {
        super(plugin);
        this.module = module;
    }

    @Override
    protected void execute(CommandSender sender, String label, String[] args) {
        Player player = playerOnly(sender);
        requirePermission(sender, PERMISSION);
        if (args.length > 1) {
            throw usage("travel.tpdeny.usage");
        }
        List<TpaManager.Request> requests = module.tpa().incoming(player.getUniqueId());
        if (requests.isEmpty()) {
            throw fail("travel.tpa.no-requests");
        }
        TpaManager.Request request = TpaRequests.select(requests, args.length == 1 ? args[0] : null,
                TpaManager.Request::senderName, "travel.tpa.multiple", "travel.tpa.no-request-from");
        module.tpa().remove(request.sender(), request.target());
        send(player, "travel.tpa.denied", placeholder("player", request.senderName()));
        Player requester = Bukkit.getPlayer(request.sender());
        if (requester != null) {
            send(requester, "travel.tpa.denied-sender", placeholder("player", player.getName()));
        }
    }

    @Override
    protected List<String> complete(CommandSender sender, String label, String[] args) {
        if (args.length != 1 || !(sender instanceof Player player) || !sender.hasPermission(PERMISSION)) {
            return List.of();
        }
        return filter(module.tpa().incoming(player.getUniqueId()).stream().map(TpaManager.Request::senderName), args[0]);
    }
}
