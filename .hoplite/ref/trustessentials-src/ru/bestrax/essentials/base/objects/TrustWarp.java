/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.Location
 *  org.bukkit.World
 *  org.bukkit.entity.Player
 *  org.bukkit.inventory.ItemStack
 *  org.jetbrains.annotations.Nullable
 */
package ru.bestrax.essentials.base.objects;

import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import ru.bestrax.essentials.api.EssentialsApi;
import ru.bestrax.essentials.api.User;
import ru.bestrax.essentials.api.Warp;
import ru.bestrax.essentials.api.events.UserTeleportToWarpEvent;
import ru.bestrax.essentials.api.manager.UserManager;
import ru.bestrax.essentials.api.manager.WarpManager;
import ru.bestrax.essentials.base.EssentialsData;
import ru.bestrax.essentials.base.objects.TrustUser;
import ru.bestrax.essentials.utils.BukkitUtils;
import ru.bestrax.essentials.utils.HeadUtils;
import ru.bestrax.essentials.utils.LocationUtils;
import ru.bestrax.essentials.utils.PlayerUtils;

public class TrustWarp
implements Warp {
    private static final UserManager USER_MANAGER = EssentialsApi.getUserManager();
    private static final WarpManager WARP_MANAGER = EssentialsApi.getWarpManager();
    private final String name;
    private String localizedName;
    private final String ownerName;
    private int grade;
    private boolean updateWarpGrade;
    private Map<String, Integer> grades;
    private final Date date;
    private ItemStack icon;
    private Player player;
    private int guiSlot;
    private String locationString;
    private Location location;

    public TrustWarp(String name, @Nullable Map<String, Integer> grades, String owner, Location location) {
        this.name = name;
        if (grades != null) {
            this.setGrades(grades);
        } else {
            this.grades = new ConcurrentHashMap<String, Integer>();
        }
        this.ownerName = owner;
        this.location = location;
        this.date = new Date(System.currentTimeMillis());
    }

    public TrustWarp(String name, @Nullable Map<String, Integer> grades, String lozalizedName, Location location, ItemStack icon) {
        this.name = name;
        if (grades != null) {
            this.setGrades(grades);
        } else {
            this.grades = new ConcurrentHashMap<String, Integer>();
        }
        this.localizedName = lozalizedName;
        this.location = location;
        this.date = new Date(0L);
        this.icon = icon;
        this.ownerName = "\u0421\u0435\u0440\u0432\u0435\u0440";
    }

    public TrustWarp(String name, Map<String, Integer> grades, String owner, String locationString, long date) {
        this.name = name;
        this.grades = grades;
        if (!grades.isEmpty()) {
            this.grade = this.calcGrade();
        }
        this.locationString = locationString;
        this.date = new Date(date);
        this.ownerName = owner;
    }

    @Override
    public String getNameOwner() {
        return this.getOwner().getDisplayName();
    }

    @Override
    public Player getOwner() {
        return Bukkit.getPlayer((String)this.ownerName);
    }

    @Override
    public Collection<Player> getNearbyPlayers(int size) {
        return PlayerUtils.getNearbyPlayers(this.location, size);
    }

    @Override
    public ItemStack getIcon() {
        return this.icon == null ? HeadUtils.getHead(this.ownerName) : this.icon;
    }

    public int getGuiSlot() {
        return this.guiSlot;
    }

    public void setGuiSlot(int guiSlot) {
        this.guiSlot = guiSlot;
    }

    @Override
    public Location getLocation() {
        if (this.location == null) {
            this.location = LocationUtils.stringToLocation(this.locationString, true);
            if (this.location.getWorld() == null) {
                this.location = EssentialsApi.getSpawn();
            }
        }
        return this.location.clone();
    }

    @Override
    public World getWorld() {
        return this.getLocation().getWorld();
    }

    @Override
    public void teleport(Player player) {
        TrustUser user;
        if (player != null && (user = (TrustUser)USER_MANAGER.getUser(player)) != null) {
            UserTeleportToWarpEvent event = new UserTeleportToWarpEvent((User)user, this);
            BukkitUtils.callEvent(event, EssentialsApi.getInstance());
            if (!event.isCancelled()) {
                user.addWaitTeleport(LocationUtils.stringFromLocation(this.getLocation(), true), EssentialsApi.getInstance().getLang().getString("WARP_TO", "%name%", this.getName()));
            }
        }
    }

    @Override
    public void grade(Player player, int grade) {
        this.grades.put(player.getName().toLowerCase(), grade);
        this.grade = this.calcGrade();
        this.updateWarpGrade = true;
    }

    private int calcGrade() {
        int grade = 0;
        int count = 0;
        for (Map.Entry<String, Integer> entry : this.grades.entrySet()) {
            grade += entry.getValue().intValue();
            ++count;
        }
        return grade / count;
    }

    @Override
    public int getGrade() {
        return this.grade;
    }

    public void saveWarp() {
        EssentialsData.saveWarpGrade(this);
    }

    @Override
    public void remove() {
        WARP_MANAGER.removeWarp(this);
    }

    @Override
    public boolean isSystem() {
        return this.ownerName.equalsIgnoreCase("\u0421\u0435\u0440\u0432\u0435\u0440");
    }

    @Override
    public Map<String, Integer> getGrades() {
        return new ConcurrentHashMap<String, Integer>(this.grades);
    }

    @Override
    public void setGrades(Map<String, Integer> grades) {
        this.grades = grades;
        this.grade = this.calcGrade();
        this.updateWarpGrade = true;
    }

    public String toString() {
        return "Warp{name = " + this.name + ", owner = " + this.ownerName + ", location = " + this.location + "}";
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getLocalizedName() {
        return this.localizedName;
    }

    @Override
    public Date getDate() {
        return this.date;
    }

    @Override
    public String getOwnerName() {
        return this.ownerName;
    }
}
