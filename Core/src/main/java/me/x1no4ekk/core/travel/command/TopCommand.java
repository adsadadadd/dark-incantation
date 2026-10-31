package me.x1no4ekk.core.travel.command;

import me.x1no4ekk.core.CorePlugin;
import me.x1no4ekk.core.command.BaseCommand;
import me.x1no4ekk.core.teleport.TeleportOptions;
import me.x1no4ekk.core.travel.TravelModule;
import me.x1no4ekk.core.travel.TravelSettings;
import org.bukkit.Bukkit;
import org.bukkit.HeightMap;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Set;
import java.util.UUID;

/**
 * /top — climbs onto the highest solid block above the player.
 */
public final class TopCommand extends BaseCommand {

    private static final String PERMISSION = "core.top";
    private static final Set<Material> UNSAFE = Set.of(Material.LAVA, Material.MAGMA_BLOCK, Material.CACTUS,
            Material.FIRE, Material.SOUL_FIRE, Material.CAMPFIRE, Material.SOUL_CAMPFIRE, Material.POWDER_SNOW);

    private final TravelModule module;

    public TopCommand(CorePlugin plugin, TravelModule module) {
        super(plugin);
        this.module = module;
    }

    @Override
    protected void execute(CommandSender sender, String label, String[] args) {
        Player player = playerOnly(sender);
        requirePermission(sender, PERMISSION);
        if (args.length != 0) {
            throw usage("travel.top.usage");
        }
        Location current = player.getLocation();
        World world = current.getWorld();
        if (world.hasCeiling()) {
            throw fail("travel.top.ceiling");
        }
        Block ground = world.getHighestBlockAt(current.getBlockX(), current.getBlockZ(), HeightMap.MOTION_BLOCKING);
        if (ground.getY() + 1 <= current.getBlockY()) {
            throw fail("travel.top.already");
        }
        Block feet = ground.getRelative(BlockFace.UP);
        if (ground.isLiquid() || UNSAFE.contains(ground.getType()) || !isFree(feet) || !isFree(feet.getRelative(BlockFace.UP))) {
            throw fail("travel.top.unsafe");
        }
        Location destination = new Location(world, ground.getX() + 0.5D, ground.getY() + 1D, ground.getZ() + 0.5D,
                current.getYaw(), current.getPitch());

        TravelSettings config = module.settings();
        TeleportOptions options = (config.topWarmup() ? TeleportOptions.DEFAULT : TeleportOptions.INSTANT)
                .withCheckCombat(config.topCheckCombat());
        UUID uuid = player.getUniqueId();
        plugin.teleportService().teleport(player, destination, options).thenAccept(result -> {
            Player online = Bukkit.getPlayer(uuid);
            if (result.isSuccess() && online != null) {
                send(online, "travel.top.teleported");
            }
        });
    }

    private static boolean isFree(Block block) {
        return block.isPassable() && !block.isLiquid() && !UNSAFE.contains(block.getType());
    }
}
