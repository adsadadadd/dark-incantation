package me.x1no4ekk.core.travel.command;

import me.x1no4ekk.core.CorePlugin;
import me.x1no4ekk.core.command.BaseCommand;
import me.x1no4ekk.core.teleport.TeleportOptions;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

/**
 * /tppos &lt;x&gt; &lt;y&gt; &lt;z&gt; [world] [yaw pitch] — instant teleport to coordinates; {@code ~} means relative.
 */
public final class TpPosCommand extends BaseCommand {

    private static final String PERMISSION = "core.tppos";
    private static final double MAX_HORIZONTAL = 29_999_984D;
    private static final double MAX_VERTICAL = 20_000D;

    public TpPosCommand(CorePlugin plugin) {
        super(plugin);
    }

    @Override
    protected void execute(CommandSender sender, String label, String[] args) {
        Player player = playerOnly(sender);
        requirePermission(sender, PERMISSION);
        if (args.length < 3 || args.length > 6) {
            throw usage("travel.tppos.usage");
        }
        Location base = player.getLocation();
        World world = base.getWorld();
        int angleIndex = 3;
        if (args.length == 4 || args.length == 6) {
            world = Bukkit.getWorld(args[3]);
            if (world == null) {
                throw fail("travel.tppos.unknown-world", placeholder("world", args[3]));
            }
            angleIndex = 4;
        }
        double x = coordinate(args[0], base.getX(), MAX_HORIZONTAL, true);
        double y = coordinate(args[1], base.getY(), MAX_VERTICAL, false);
        double z = coordinate(args[2], base.getZ(), MAX_HORIZONTAL, true);
        float yaw = base.getYaw();
        float pitch = base.getPitch();
        if (args.length >= 5) {
            yaw = (float) coordinate(args[angleIndex], base.getYaw(), 360D, false);
            pitch = (float) Math.clamp(coordinate(args[angleIndex + 1], base.getPitch(), 90D, false), -90D, 90D);
        }
        Location destination = new Location(world, x, y, z, yaw, pitch);
        String worldName = world.getName();
        UUID uuid = player.getUniqueId();
        plugin.teleportService().teleport(player, destination, TeleportOptions.INSTANT).thenAccept(result -> {
            Player online = Bukkit.getPlayer(uuid);
            if (result.isSuccess() && online != null) {
                send(online, "travel.tppos.teleported", placeholder("world", worldName),
                        placeholder("x", (long) Math.floor(x)), placeholder("y", (long) Math.floor(y)),
                        placeholder("z", (long) Math.floor(z)));
            }
        });
    }

    /**
     * Parses an absolute or {@code ~}-relative value. Whole absolute x/z values point at the block centre, as in vanilla.
     */
    private static double coordinate(String raw, double current, double limit, boolean centre) {
        if (raw.startsWith("~")) {
            double offset = raw.length() == 1 ? 0D : parseDouble(raw.substring(1), -limit, limit);
            double value = current + offset;
            if (Math.abs(value) > limit) {
                throw fail("general.number-range", placeholder("value", raw),
                        placeholder("min", (long) -limit), placeholder("max", (long) limit));
            }
            return value;
        }
        double value = parseDouble(raw, -limit, limit);
        return centre && raw.indexOf('.') < 0 && raw.indexOf(',') < 0 ? value + 0.5D : value;
    }

    @Override
    protected List<String> complete(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player player) || !sender.hasPermission(PERMISSION)) {
            return List.of();
        }
        Location location = player.getLocation();
        return switch (args.length) {
            case 1 -> filter(List.of("~", String.valueOf(location.getBlockX())), args[0]);
            case 2 -> filter(List.of("~", String.valueOf(location.getBlockY())), args[1]);
            case 3 -> filter(List.of("~", String.valueOf(location.getBlockZ())), args[2]);
            case 4 -> filter(Bukkit.getWorlds().stream().map(World::getName), args[3]);
            default -> List.of();
        };
    }
}
