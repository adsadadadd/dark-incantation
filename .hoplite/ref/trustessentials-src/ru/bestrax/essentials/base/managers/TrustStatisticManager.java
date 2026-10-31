/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.OfflinePlayer
 */
package ru.bestrax.essentials.base.managers;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.OfflinePlayer;
import ru.bestrax.essentials.EssentialsPlugin;
import ru.bestrax.essentials.api.StatisticUser;
import ru.bestrax.essentials.api.manager.StatisticManager;
import ru.bestrax.essentials.base.EssentialsData;
import ru.bestrax.essentials.base.objects.TrustStatisticUser;
import ru.bestrax.essentials.utils.BukkitUtils;

public class TrustStatisticManager
implements StatisticManager {
    private Map<String, StatisticUser> loadedStatistic = new ConcurrentHashMap<String, StatisticUser>();
    private Map<String, StatisticUser> cachedStatistic = new ConcurrentHashMap<String, StatisticUser>();

    @Override
    public StatisticUser getUser(String player) {
        if (player == null) {
            throw new IllegalArgumentException("Player name cannot be null");
        }
        if (!this.loadedStatistic.containsKey(player.toLowerCase())) {
            if (this.cachedStatistic.containsKey(player.toLowerCase())) {
                this.loadedStatistic.put(player.toLowerCase(), this.cachedStatistic.get(player.toLowerCase()));
                this.cachedStatistic.remove(player.toLowerCase());
            } else {
                this.loadStatisticUser(player);
            }
        }
        return this.loadedStatistic.get(player.toLowerCase());
    }

    @Override
    public StatisticUser getUser(OfflinePlayer player) {
        if (player == null) {
            throw new IllegalArgumentException("Player cannot be null");
        }
        return this.getUser(player.getName());
    }

    @Override
    public void unloadStatisticUser(String player) {
        if (player == null) {
            throw new IllegalArgumentException("Player name cannot be null");
        }
        String nameLower = player.toLowerCase();
        StatisticUser user = this.loadedStatistic.get(nameLower);
        if (user == null) {
            return;
        }
        if (this.loadedStatistic.remove(nameLower, user)) {
            this.cachedStatistic.put(nameLower, user);
            if (user != null) {
                TrustStatisticUser statisticUser = (TrustStatisticUser)user;
                BukkitUtils.runTaskAsync(statisticUser::save, EssentialsPlugin.getProvidingPlugin(EssentialsPlugin.class));
            }
        } else {
            throw new RuntimeException("\u041d\u0435 \u0443\u0434\u0430\u043b\u043e\u0441\u044c \u0443\u0434\u0430\u043b\u0438\u0442\u044c \u043f\u043e\u043b\u044c\u0437\u043e\u0432\u0430\u0442\u0435\u043b\u044f " + nameLower);
        }
    }

    @Override
    public void loadStatisticUser(String player) {
        if (player == null) {
            throw new IllegalArgumentException("Player name cannot be null");
        }
        if (this.loadedStatistic.containsKey(player.toLowerCase())) {
            return;
        }
        this.loadedStatistic.put(player.toLowerCase(), EssentialsData.loadStatistic(player));
    }
}
