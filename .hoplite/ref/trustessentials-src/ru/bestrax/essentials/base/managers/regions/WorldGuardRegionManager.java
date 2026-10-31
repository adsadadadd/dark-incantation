/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.sk89q.worldguard.protection.managers.RegionManager
 *  com.sk89q.worldguard.protection.regions.ProtectedRegion
 *  org.bukkit.Bukkit
 *  org.bukkit.Location
 *  org.bukkit.World
 *  org.bukkit.entity.Player
 */
package ru.bestrax.essentials.base.managers.regions;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import ru.bestrax.essentials.api.EssentialsApi;
import ru.bestrax.essentials.api.manager.RegionManager;
import ru.bestrax.essentials.base.objects.TrustUser;
import ru.bestrax.essentials.utils.LocationUtils;

public class WorldGuardRegionManager
implements RegionManager {
    private Object worldGuard;

    public com.sk89q.worldguard.protection.managers.RegionManager getRegionManager(World world) {
        try {
            if (this.hasClass("com.sk89q.worldguard.WorldGuard")) {
                Object platform = this.getWorldGuardPlugin().getClass().getMethod("getPlatform", new Class[0]).invoke(this.getWorldGuardPlugin(), new Object[0]);
                Object regionContainer = platform.getClass().getMethod("getRegionContainer", new Class[0]).invoke(platform, new Object[0]);
                Object adapt = Class.forName("com.sk89q.worldedit.bukkit.BukkitAdapter").getMethod("adapt", World.class).invoke(null, world);
                return (com.sk89q.worldguard.protection.managers.RegionManager)regionContainer.getClass().getMethod("get", Class.forName("com.sk89q.worldedit.world.World")).invoke(regionContainer, adapt);
            }
            return (com.sk89q.worldguard.protection.managers.RegionManager)this.getWorldGuardPlugin().getClass().getMethod("getRegionManager", World.class).invoke(this.getWorldGuardPlugin(), world);
        }
        catch (ReflectiveOperationException e) {
            throw new RuntimeException("\u0412\u0430\u0448\u0430 \u0432\u0435\u0440\u0441\u0438\u044f WorldGuard, WorldEdit \u043d\u0435 \u043f\u043e\u0434\u0434\u0435\u0440\u0436\u0438\u0432\u0430\u0435\u0442\u0441\u044f :(");
        }
    }

    public List<CustomBlockVector2D> getPoints(ProtectedRegion protectedRegion) {
        ArrayList<CustomBlockVector2D> list = new ArrayList<CustomBlockVector2D>();
        try {
            for (int i = 0; i < protectedRegion.getPoints().size(); ++i) {
                Object points = protectedRegion.getClass().getDeclaredMethod("getPoints", new Class[0]).invoke((Object)protectedRegion, new Object[0]);
                Object vector = points.getClass().getDeclaredMethod("get", Integer.TYPE).invoke(points, i);
                list.add(new CustomBlockVector2D((Integer)vector.getClass().getMethod("getBlockX", new Class[0]).invoke(vector, new Object[0]), (Integer)vector.getClass().getMethod("getBlockY", new Class[0]).invoke(vector, new Object[0]), (Integer)vector.getClass().getMethod("getBlockZ", new Class[0]).invoke(vector, new Object[0])));
            }
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    @Override
    public boolean teleportRandomRegion(Player player, World world) {
        return this.teleportRandomRegion(world, player, 0);
    }

    private boolean teleportRandomRegion(World world, Player player, int retry) {
        CustomBlockVector2D firstPoint;
        List<String> blocks = EssentialsApi.instance.getSettings().getStringList("teleportRegionBlock", new String[0]);
        if (retry > 10) {
            return false;
        }
        com.sk89q.worldguard.protection.managers.RegionManager regionManager = this.getRegionManager(world);
        if (regionManager.getRegions().size() == 0) {
            return false;
        }
        ProtectedRegion protectedRegion = (ProtectedRegion)new ArrayList(regionManager.getRegions().values()).get(new Random().nextInt(regionManager.getRegions().size()));
        if (blocks.contains(protectedRegion.getId())) {
            return this.teleportRandomRegion(world, player, retry + 1);
        }
        try {
            firstPoint = this.getPoints(protectedRegion).get(0);
        }
        catch (Exception e) {
            return this.teleportRandomRegion(world, player, retry + 1);
        }
        int y = world.getHighestBlockYAt(firstPoint.getBlockX(), firstPoint.getBlockZ());
        Location finalLocation = new Location(world, (double)firstPoint.getBlockX(), (double)y, (double)firstPoint.getBlockZ());
        ((TrustUser)EssentialsApi.getUserManager().getUser(player)).addWaitTeleport(LocationUtils.stringFromLocation(finalLocation, true), EssentialsApi.getInstance().getLang().getString("RTP_REGION_TELEPORT", new String[0]));
        return true;
    }

    public Object getWorldGuardPlugin() {
        try {
            return this.worldGuard == null ? (this.worldGuard = this.getWorldGuardInstance()) : this.worldGuard;
        }
        catch (ClassNotFoundException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    private Object getWorldGuardInstance() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        if (this.hasClass("com.sk89q.worldguard.WorldGuard")) {
            return Class.forName("com.sk89q.worldguard.WorldGuard").getMethod("getInstance", new Class[0]).invoke(null, new Object[0]);
        }
        return Bukkit.getPluginManager().getPlugin("WorldGuard");
    }

    private boolean hasClass(String clazz) {
        try {
            Class.forName(clazz);
            return true;
        }
        catch (ClassNotFoundException e) {
            return false;
        }
    }

    public class CustomBlockVector2D {
        private final int blockX;
        private final int blockY;
        private final int blockZ;

        public CustomBlockVector2D(int blockX, int blockY, int blockZ) {
            this.blockX = blockX;
            this.blockY = blockY;
            this.blockZ = blockZ;
        }

        public int getBlockY() {
            return this.blockY;
        }

        public int getBlockX() {
            return this.blockX;
        }

        public int getBlockZ() {
            return this.blockZ;
        }
    }
}
