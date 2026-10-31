package me.x1no4ekk.core.travel.command;

import me.x1no4ekk.core.CorePlugin;
import me.x1no4ekk.core.command.BaseCommand;
import me.x1no4ekk.core.teleport.TeleportOptions;
import me.x1no4ekk.core.travel.TravelModule;
import me.x1no4ekk.core.util.Durations;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * /back — returns to the previous location (last teleport origin or death point).
 */
public final class BackCommand extends BaseCommand {

    private static final String PERMISSION = "core.back";
    private static final String BYPASS_BLOCK_PERMISSION = "core.back.bypass-block";

    private final TravelModule module;

    public BackCommand(CorePlugin plugin, TravelModule module) {
        super(plugin);
        this.module = module;
    }

    @Override
    protected void execute(CommandSender sender, String label, String[] args) {
        Player player = playerOnly(sender);
        requirePermission(sender, PERMISSION);
        if (args.length != 0) {
            throw usage("travel.back.usage");
        }
        UUID uuid = player.getUniqueId();
        Location back = plugin.teleportService().getBackLocation(uuid);
        if (back == null) {
            throw fail("travel.back.none");
        }
        plugin.teleportService().teleport(player, back, TeleportOptions.DEFAULT).thenAccept(result -> {
            Player online = Bukkit.getPlayer(uuid);
            if (!result.isSuccess() || online == null) {
                return;
            }
            send(online, "travel.back.teleported");
            long block = module.settings().backPickupBlockMillis();
            if (block > 0 && !online.hasPermission(BYPASS_BLOCK_PERMISSION)) {
                module.blockPickup(uuid, block);
                send(online, "travel.back.pickup-blocked", placeholder("time", Durations.format(block)));
            }
        });
    }
}
