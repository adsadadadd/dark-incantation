package me.x1no4ekk.core.travel.command;

import me.x1no4ekk.core.CorePlugin;
import me.x1no4ekk.core.command.BaseCommand;
import me.x1no4ekk.core.travel.TravelModule;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * /setspawn — stores the sender's position as the server spawn.
 */
public final class SetSpawnCommand extends BaseCommand {

    private static final String PERMISSION = "core.setspawn";

    private final TravelModule module;

    public SetSpawnCommand(CorePlugin plugin, TravelModule module) {
        super(plugin);
        this.module = module;
    }

    @Override
    protected void execute(CommandSender sender, String label, String[] args) {
        Player player = playerOnly(sender);
        requirePermission(sender, PERMISSION);
        if (args.length != 0) {
            throw usage("travel.spawn.setspawn-usage");
        }
        Location location = player.getLocation();
        UUID uuid = player.getUniqueId();
        module.spawn().set(location, error -> {
            Player online = Bukkit.getPlayer(uuid);
            if (online != null) {
                send(online, "general.database-error");
            }
        });
        send(player, "travel.spawn.set", placeholder("world", location.getWorld().getName()),
                placeholder("x", location.getBlockX()), placeholder("y", location.getBlockY()),
                placeholder("z", location.getBlockZ()));
    }
}
