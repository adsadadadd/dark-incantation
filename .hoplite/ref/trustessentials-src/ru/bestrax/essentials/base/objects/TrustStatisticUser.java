/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.Statistic
 *  org.bukkit.entity.Player
 */
package ru.bestrax.essentials.base.objects;

import java.util.List;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Bukkit;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;
import ru.bestrax.essentials.api.EssentialsApi;
import ru.bestrax.essentials.api.EssentialsStatistic;
import ru.bestrax.essentials.api.StatisticUser;
import ru.bestrax.essentials.api.events.StatisticChangeEvent;
import ru.bestrax.essentials.base.EssentialsData;
import ru.bestrax.essentials.utils.BukkitUtils;

public class TrustStatisticUser
implements StatisticUser {
    private List<Pair<EssentialsStatistic, Long>> statistic;
    private String name;
    private boolean update;

    public TrustStatisticUser(String name, List<Pair<EssentialsStatistic, Long>> statistic) {
        this.name = name;
        this.statistic = statistic;
    }

    @Override
    public long getStatistic(EssentialsStatistic statistic) {
        if (statistic == EssentialsStatistic.PLAY_ONE_TICK) {
            Player player = Bukkit.getPlayer((String)this.name);
            if (player == null || !player.isOnline()) {
                return 0L;
            }
            Statistic statisticVal = null;
            try {
                statisticVal = Statistic.valueOf((String)"PLAY_ONE_MINUTE");
            }
            catch (Throwable e) {
                statisticVal = Statistic.valueOf((String)"PLAY_ONE_TICK");
            }
            return statisticVal != null ? (long)player.getStatistic(statisticVal) : 0L;
        }
        Pair eStatistic = this.statistic.stream().filter(x -> x.getKey() == statistic).findAny().orElse(null);
        if (eStatistic == null) {
            return 0L;
        }
        return (Long)eStatistic.getValue();
    }

    @Override
    public void setStatistic(EssentialsStatistic statistic, long value) {
        Pair pair;
        StatisticChangeEvent statisticChangeEvent = new StatisticChangeEvent(this, statistic, value);
        BukkitUtils.callEvent(statisticChangeEvent, EssentialsApi.getInstance());
        if (statisticChangeEvent.isCancelled()) {
            return;
        }
        if (this.getStatistic(statistic) != value) {
            this.update = true;
        }
        if ((pair = (Pair)this.statistic.stream().filter(x -> x.getKey() == statistic).findAny().orElse(null)) == null) {
            return;
        }
        pair.setValue(value);
    }

    public void save() {
        if (this.update) {
            for (EssentialsStatistic value : EssentialsStatistic.values()) {
                EssentialsData.setStatistic(this.getUsername(), value, this.getStatistic(value));
            }
        }
    }

    @Override
    public String getUsername() {
        return this.name;
    }
}
