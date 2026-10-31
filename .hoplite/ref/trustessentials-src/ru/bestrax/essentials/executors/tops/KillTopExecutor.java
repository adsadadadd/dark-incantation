/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.OfflinePlayer
 *  org.bukkit.command.CommandSender
 */
package ru.bestrax.essentials.executors.tops;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import ru.bestrax.essentials.api.EssentialsApi;
import ru.bestrax.essentials.api.EssentialsStatistic;
import ru.bestrax.essentials.executors.tops.TopExecutor;
import ru.bestrax.essentials.utils.BukkitUtils;

public class KillTopExecutor
extends TopExecutor {
    public KillTopExecutor() {
        super("topkills", "topkill", "killtop");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        BukkitUtils.runTaskAsync(() -> this.send(sender, "KILLTOP", this.getKillTop(100)), EssentialsApi.getInstance());
    }

    public List<Pair<String, Integer>> getKillTop(int max) {
        ArrayList<Pair<String, Integer>> top = new ArrayList<Pair<String, Integer>>();
        for (OfflinePlayer offlinePlayer : Bukkit.getOfflinePlayers()) {
            if (offlinePlayer == null || offlinePlayer.getName() == null) continue;
            top.add(Pair.of(offlinePlayer.getName(), (int)EssentialsApi.getStatisticManager().getUser(offlinePlayer).getStatistic(EssentialsStatistic.KILLS)));
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
