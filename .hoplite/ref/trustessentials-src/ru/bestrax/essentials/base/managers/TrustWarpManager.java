/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.Location
 *  org.bukkit.World
 *  org.bukkit.configuration.ConfigurationSection
 */
package ru.bestrax.essentials.base.managers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import ru.bestrax.essentials.api.EssentialsApi;
import ru.bestrax.essentials.api.User;
import ru.bestrax.essentials.api.Warp;
import ru.bestrax.essentials.api.config.Config;
import ru.bestrax.essentials.api.manager.WarpManager;
import ru.bestrax.essentials.base.EssentialsData;
import ru.bestrax.essentials.base.objects.TrustWarp;
import ru.bestrax.essentials.utils.ItemBuilder;

public class TrustWarpManager
implements WarpManager {
    private Map<String, Warp> warps = new ConcurrentHashMap<String, Warp>();
    private Map<String, Integer> warpsLimit = new ConcurrentHashMap<String, Integer>();

    public TrustWarpManager() {
        this.loadWarpLimit();
    }

    @Override
    public void loadWarpLimit() {
        Config settings = EssentialsApi.instance.getSettings();
        this.warpsLimit.clear();
        this.warpsLimit.put("default", settings.getInt("setWarpDefault"));
        for (String string : settings.getStringList("setWarpLimit", new String[0])) {
            String groupName;
            if (!string.contains(":") || (groupName = string.split(":")[0]).equalsIgnoreCase("default")) continue;
            int limit = Integer.parseInt(string.split(":")[1]);
            this.warpsLimit.put(groupName, limit);
        }
    }

    @Override
    public void addSystemWarp(String warp, ConfigurationSection section) {
        if (this.getWarp(warp) == null) {
            World world = Bukkit.getWorld((String)section.getString("world"));
            if (world == null) {
                throw new IllegalArgumentException("\u041d\u0435 \u043d\u0430\u0439\u0434\u0435\u043d \u043c\u0438\u0440 " + section.getString("world"));
            }
            TrustWarp trustWarp = new TrustWarp(warp, null, section.getString("localized_name"), new Location(world, section.getDouble("x"), section.getDouble("y"), section.getDouble("z"), (float)section.getDouble("yaw"), (float)section.getDouble("pitch")), ItemBuilder.builder(section.getString("icon")).build());
            trustWarp.setGuiSlot(section.getInt("slot"));
            this.addWarp(trustWarp);
        }
    }

    @Override
    public int getWarpLimit(String group) {
        return this.warpsLimit.getOrDefault(group, 0);
    }

    @Override
    public void addWarp(Warp warp) {
        String name = warp.getName();
        this.warps.put(name.toLowerCase(), warp);
    }

    @Override
    public void addToDataBase(Warp warp) {
        this.addWarp(warp);
        if (!warp.isSystem()) {
            EssentialsData.addWarp(warp.getName(), warp.getOwnerName(), warp.getLocation());
        }
    }

    @Override
    public Warp getWarp(String name) {
        return this.warps.get(name.toLowerCase());
    }

    @Override
    public List<Warp> getWarps(User user) {
        return this.getWarps(user.getUsername());
    }

    @Override
    public List<Warp> getWarps(String playerName) {
        ArrayList<Warp> warps = new ArrayList<Warp>();
        this.warps.values().forEach(warp -> {
            if (!warp.isSystem() && warp.getOwnerName().equals(playerName)) {
                warps.add((Warp)warp);
            }
        });
        return warps;
    }

    @Override
    public Map<String, Warp> getWarps() {
        return this.warps;
    }

    @Override
    public Map<String, Warp> getPlayersWarps() {
        HashMap<String, Warp> warps = new HashMap<String, Warp>();
        this.warps.forEach((key, value) -> {
            if (!value.isSystem()) {
                warps.put((String)key, (Warp)value);
            }
        });
        return warps;
    }

    @Override
    public List<Warp> getSystemWarps() {
        return this.warps.values().stream().filter(Warp::isSystem).collect(Collectors.toList());
    }

    @Override
    public void removeWarp(Warp warp) {
        if (warp != null) {
            this.removeWarp(warp.getName());
        }
    }

    @Override
    public void removeWarp(String name) {
        EssentialsData.removeWarp(name);
        this.warps.remove(name.toLowerCase());
    }

    @Override
    public int size() {
        return this.warps.size();
    }
}
