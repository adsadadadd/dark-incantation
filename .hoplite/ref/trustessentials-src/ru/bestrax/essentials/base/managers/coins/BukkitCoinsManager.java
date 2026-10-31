/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.OfflinePlayer
 *  ru.howexide.api.gamer.GamerManager
 *  ru.howexide.base.api.gamer.IBaseGamer
 */
package ru.bestrax.essentials.base.managers.coins;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import ru.bestrax.essentials.api.manager.CoinsManager;
import ru.howexide.api.gamer.GamerManager;
import ru.howexide.base.api.gamer.IBaseGamer;

public class BukkitCoinsManager
implements CoinsManager {
    @Override
    public boolean setup() {
        return Bukkit.getPluginManager().isPluginEnabled("BukkitAPI");
    }

    @Override
    public boolean hasCoins(OfflinePlayer player, int count) {
        return this.hasCoins(player.getName(), count);
    }

    @Override
    public void withdraw(OfflinePlayer player, int count) {
        this.withdraw(player.getName(), count);
    }

    @Override
    public void give(OfflinePlayer player, int count) {
        this.give(player.getName(), count);
    }

    @Override
    public int get(OfflinePlayer player) {
        return this.get(player.getName());
    }

    @Override
    public void set(OfflinePlayer player, int count) {
        this.set(player.getName(), count);
    }

    @Override
    public boolean hasCoins(String player, int count) {
        return this.get(player) >= count;
    }

    @Override
    public void withdraw(String player, int count) {
        this.set(player, this.get(player) - count);
    }

    @Override
    public void give(String player, int count) {
        this.set(player, this.get(player) + count);
    }

    @Override
    public void set(String player, int count) {
        IBaseGamer gamer = GamerManager.getOrCreate((String)player);
        if (gamer == null) {
            return;
        }
        gamer.setCoins(count, true);
    }

    @Override
    public int get(String player) {
        IBaseGamer gamer = GamerManager.getOrCreate((String)player);
        if (gamer == null) {
            return 0;
        }
        return gamer.getCoins();
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
