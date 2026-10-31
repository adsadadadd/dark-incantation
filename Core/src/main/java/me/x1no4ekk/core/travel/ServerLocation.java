package me.x1no4ekk.core.travel;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

/**
 * Immutable named server point (spawn...). Keeps the world name only, never a {@link World}.
 */
public record ServerLocation(String name, String world, double x, double y, double z, float yaw, float pitch) {

    public static ServerLocation of(String name, Location location) {
        return new ServerLocation(name, location.getWorld().getName(), location.getX(), location.getY(), location.getZ(),
                location.getYaw(), location.getPitch());
    }

    /**
     * @return the location, or {@code null} if the world is not loaded
     */
    public Location toLocation() {
        World loaded = Bukkit.getWorld(world);
        return loaded == null ? null : new Location(loaded, x, y, z, yaw, pitch);
    }
}
