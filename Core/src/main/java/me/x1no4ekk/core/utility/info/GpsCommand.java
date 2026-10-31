package me.x1no4ekk.core.utility.info;

import me.x1no4ekk.core.CorePlugin;
import me.x1no4ekk.core.command.BaseCommand;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * {@code /gps <x> [y] <z> [world] | <player> | off}.
 */
final class GpsCommand extends BaseCommand {

    private static final double LIMIT = 30_000_000;
    private static final String TRACK_PERMISSION = "core.gps.player";

    private final GpsService gps;

    GpsCommand(CorePlugin plugin, GpsService gps) {
        super(plugin);
        this.gps = gps;
    }

    @Override
    protected void execute(CommandSender sender, String label, String[] args) {
        Player player = playerOnly(sender);
        requirePermission(player, "core.gps");
        if (args.length == 0) {
            if (!gps.stop(player, "utility.gps.disabled")) {
                throw usage("utility.gps.usage");
            }
            return;
        }
        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("off")) {
                if (!gps.stop(player, "utility.gps.disabled")) {
                    throw fail("utility.gps.not-active");
                }
                return;
            }
            requirePermission(player, TRACK_PERMISSION);
            Player target = player(player, args[0]);
            if (target.equals(player)) {
                throw fail("utility.gps.self");
            }
            gps.start(player, null, 0, 0, target.getUniqueId(), target.getName());
            send(player, "utility.gps.enabled-player", Placeholder.component("player", target.displayName()));
            return;
        }
        double x = parseDouble(args[0], -LIMIT, LIMIT);
        // "<x> <y> <z> [world]" when the third argument is a number, otherwise "<x> <z> [world]".
        int zIndex = args.length >= 3 && isNumber(args[2]) ? 2 : 1;
        double z = parseDouble(args[zIndex], -LIMIT, LIMIT);
        if (zIndex == 2) {
            parseDouble(args[1], -LIMIT, LIMIT);
        }
        int worldIndex = zIndex + 1;
        if (args.length > worldIndex + 1) {
            throw usage("utility.gps.usage");
        }
        World world = args.length > worldIndex ? Bukkit.getWorld(args[worldIndex]) : player.getWorld();
        if (world == null) {
            throw fail("utility.gps.world-not-found", placeholder("world", args[worldIndex]));
        }
        String targetLabel = (long) Math.floor(x) + ", " + (long) Math.floor(z);
        gps.start(player, world.getName(), x, z, null, targetLabel);
        send(player, "utility.gps.enabled", placeholder("target", targetLabel), placeholder("world", world.getName()));
    }

    private static boolean isNumber(String raw) {
        try {
            Double.parseDouble(raw.replace(',', '.'));
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Override
    protected List<String> complete(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            return List.of();
        }
        List<String> options = new ArrayList<>();
        switch (args.length) {
            case 1 -> {
                options.add("off");
                options.add(String.valueOf(player.getLocation().getBlockX()));
                if (player.hasPermission(TRACK_PERMISSION)) {
                    options.addAll(players(player, args[0]));
                }
            }
            case 2 -> options.add(String.valueOf(player.getLocation().getBlockZ()));
            case 3, 4 -> {
                for (World world : Bukkit.getWorlds()) {
                    options.add(world.getName());
                }
            }
            default -> {
                return List.of();
            }
        }
        return filter(options, args[args.length - 1]);
    }
}
