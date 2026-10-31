/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.black_ixx.playerpoints.PlayerPoints
 *  org.black_ixx.playerpoints.PlayerPointsAPI
 *  org.bukkit.Bukkit
 *  org.bukkit.OfflinePlayer
 *  org.bukkit.plugin.Plugin
 */
package ru.bestrax.essentials.base.managers.coins;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;
import org.black_ixx.playerpoints.PlayerPoints;
import org.black_ixx.playerpoints.PlayerPointsAPI;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.Plugin;
import ru.bestrax.essentials.api.manager.CoinsManager;

public class PlayerPointsManager
implements CoinsManager {
    private PlayerPointsAPI playerPointsAPI;

    @Override
    public boolean setup() {
        Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("PlayerPoints");
        PlayerPoints playerPoints = (PlayerPoints)plugin;
        if (playerPoints != null) {
            this.playerPointsAPI = playerPoints.getAPI();
        }
        return this.playerPointsAPI != null;
    }

    @Override
    public boolean hasCoins(OfflinePlayer player, int count) {
        return this.playerPointsAPI.look(player.getUniqueId()) >= count;
    }

    @Override
    public void withdraw(OfflinePlayer player, int count) {
        this.playerPointsAPI.take(player.getUniqueId(), count);
    }

    @Override
    public void give(OfflinePlayer player, int count) {
        this.playerPointsAPI.give(player.getUniqueId(), count);
    }

    @Override
    public int get(OfflinePlayer player) {
        return this.playerPointsAPI.look(player.getUniqueId());
    }

    @Override
    public void set(OfflinePlayer player, int count) {
        this.playerPointsAPI.set(player.getUniqueId(), count);
    }

    @Override
    public boolean hasCoins(String player, int count) {
        return this.hasCoins(Bukkit.getOfflinePlayer((String)player), count);
    }

    @Override
    public void withdraw(String player, int count) {
        this.withdraw(Bukkit.getOfflinePlayer((String)player), count);
    }

    @Override
    public void give(String player, int count) {
        this.give(Bukkit.getOfflinePlayer((String)player), count);
    }

    @Override
    public void set(String player, int count) {
        this.set(Bukkit.getOfflinePlayer((String)player), count);
    }

    @Override
    public int get(String player) {
        return this.get(Bukkit.getOfflinePlayer((String)player));
    }

    @Override
    public List<Pair<String, Integer>> getTop(int max) {
        ArrayList<Pair<String, Integer>> top = new ArrayList<Pair<String, Integer>>();
        for (OfflinePlayer offlinePlayer : Bukkit.getOfflinePlayers()) {
            if (offlinePlayer == null || offlinePlayer.getName() == null) continue;
            double balance = this.get(offlinePlayer);
            top.add(Pair.of(offlinePlayer.getName(), (int)balance));
        }
        if (top.size() < max) {
            for (int i = 0; i < max - top.size(); ++i) {
                top.add(Pair.of("\u041d\u0435 \u043d\u0430\u0439\u0434\u0435\u043d", -1));
            }
        }
        top.sort(Comparator.comparing(Pair::getRight));
        Collections.reverse(top);
        return top;
    }
}
