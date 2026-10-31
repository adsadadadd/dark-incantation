/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.milkbowl.vault.economy.Economy
 *  org.bukkit.Bukkit
 *  org.bukkit.OfflinePlayer
 *  org.bukkit.plugin.RegisteredServiceProvider
 */
package ru.bestrax.essentials.base.managers.money;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import net.milkbowl.vault.economy.Economy;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;
import ru.bestrax.essentials.api.manager.MoneyManager;

public class VaultMoneyManager
implements MoneyManager {
    private Economy economy;

    @Override
    public boolean setup() {
        if (Bukkit.getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider rsp = Bukkit.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        this.economy = (Economy)rsp.getProvider();
        return this.economy != null;
    }

    @Override
    public boolean hasCoins(OfflinePlayer player, double count) {
        return this.economy.has(player, count);
    }

    @Override
    public void withdraw(OfflinePlayer player, double count) {
        this.economy.withdrawPlayer(player, count);
    }

    @Override
    public void give(OfflinePlayer player, double count) {
        this.economy.depositPlayer(player, count);
    }

    @Override
    public double get(OfflinePlayer player) {
        return this.economy.getBalance(player);
    }

    @Override
    public void set(OfflinePlayer player, double count) {
        this.withdraw(player, this.get(player));
        this.give(player, count);
    }

    @Override
    public boolean hasCoins(String player, double count) {
        return this.economy.has(player, count);
    }

    @Override
    public void withdraw(String player, double count) {
        this.economy.withdrawPlayer(player, count);
    }

    @Override
    public void give(String player, double count) {
        this.economy.depositPlayer(player, count);
    }

    @Override
    public void set(String player, double count) {
        this.withdraw(player, this.get(player));
        this.give(player, count);
    }

    @Override
    public double get(String player) {
        return this.economy.getBalance(player);
    }

    @Override
    public List<Pair<String, Integer>> getTop(int max) {
        ArrayList<Pair<String, Integer>> top = new ArrayList<Pair<String, Integer>>();
        for (OfflinePlayer offlinePlayer : Bukkit.getOfflinePlayers()) {
            double balance;
            if (offlinePlayer == null || offlinePlayer.getName() == null) continue;
            try {
                balance = this.get(offlinePlayer);
            }
            catch (NullPointerException e) {
                continue;
            }
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
