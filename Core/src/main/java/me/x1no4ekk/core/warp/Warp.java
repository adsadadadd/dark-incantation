package me.x1no4ekk.core.warp;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

import java.util.Locale;
import java.util.UUID;

/**
 * Immutable warp snapshot. System warps have no owner and may be pinned to a menu slot.
 *
 * @param owner     owner UUID, {@code null} for system warps
 * @param ownerName last known owner name ({@code null} if unknown)
 * @param icon      custom icon, or {@code null} for the default (owner head / configured material)
 * @param slot      SystemWarpGui slot, or {@code -1}
 */
public record Warp(String name, UUID owner, String ownerName, boolean system, String world, double x, double y, double z,
                   float yaw, float pitch, Material icon, String description, int visits, long createdAt, int slot) {

    public static final int MAX_NAME_LENGTH = 32;
    public static final int MAX_DESCRIPTION_LENGTH = 255;

    public static Warp create(String name, UUID owner, String ownerName, boolean system, Location location,
                              Material icon, String description, int slot) {
        return new Warp(name, owner, ownerName, system, location.getWorld().getName(), location.getX(), location.getY(),
                location.getZ(), location.getYaw(), location.getPitch(), icon, description, 0, System.currentTimeMillis(), slot);
    }

    /**
     * Cache key: warp names are unique case-insensitively.
     */
    public static String key(String name) {
        return name.toLowerCase(Locale.ROOT);
    }

    public String key() {
        return key(name);
    }

    public boolean isOwnedBy(UUID uuid) {
        return owner != null && owner.equals(uuid);
    }

    /**
     * @return the location, or {@code null} if the world is not loaded
     */
    public Location toLocation() {
        World loaded = Bukkit.getWorld(world);
        return loaded == null ? null : new Location(loaded, x, y, z, yaw, pitch);
    }

    public Warp withVisits(int visits) {
        return new Warp(name, owner, ownerName, system, world, x, y, z, yaw, pitch, icon, description, visits, createdAt, slot);
    }

    public Warp withIcon(Material icon) {
        return new Warp(name, owner, ownerName, system, world, x, y, z, yaw, pitch, icon, description, visits, createdAt, slot);
    }

    public Warp withDescription(String description) {
        return new Warp(name, owner, ownerName, system, world, x, y, z, yaw, pitch, icon, description, visits, createdAt, slot);
    }

    public Warp withSlot(int slot) {
        return new Warp(name, owner, ownerName, system, world, x, y, z, yaw, pitch, icon, description, visits, createdAt, slot);
    }

    public Warp withLocation(Location location) {
        return new Warp(name, owner, ownerName, system, location.getWorld().getName(), location.getX(), location.getY(),
                location.getZ(), location.getYaw(), location.getPitch(), icon, description, visits, createdAt, slot);
    }
}
