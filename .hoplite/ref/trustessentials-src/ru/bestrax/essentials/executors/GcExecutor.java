/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.command.CommandSender
 */
package ru.bestrax.essentials.executors;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.text.DecimalFormat;
import java.time.Duration;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import ru.bestrax.essentials.api.EssentialsApi;
import ru.bestrax.essentials.api.commands.TrustCommand;
import ru.bestrax.essentials.utils.NMSUtils;
import ru.bestrax.essentials.utils.TimeUtils;

public class GcExecutor
extends TrustCommand {
    public GcExecutor() {
        super("gc", new String[0]);
        this.setPermission("trustessentials.gc");
        this.register();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        DecimalFormat decimalFormat = new DecimalFormat("##.##");
        Runtime runtime = Runtime.getRuntime();
        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        double tps = NMSUtils.getAverageTPS();
        String tpsStr = tps >= 20.0 ? "*20.0" : decimalFormat.format(tps);
        String uptimeStr = TimeUtils.getTimeInSeconds(Duration.ofMillis(runtimeMXBean.getUptime()).toMillis() / 1000L);
        this.sendMessages(sender, "GC", "%java%", runtimeMXBean.getVmVersion(), "%worlds%", String.valueOf(Bukkit.getWorlds().size()), "%maxMemory%", String.valueOf(runtime.maxMemory() / 1000000L), "%freeMemory%", String.valueOf(runtime.freeMemory() / 1000000L), "%totalMemory%", String.valueOf(runtime.totalMemory() / 1000000L), "%tps%", tpsStr, "%uptime%", uptimeStr, "%online%", String.valueOf(Bukkit.getOnlinePlayers().size() + (EssentialsApi.getBotManager() != null ? EssentialsApi.getBotManager().getUsedAccounts().size() : 0)), "%players%", String.valueOf(Bukkit.getMaxPlayers()), "%plugins%", String.valueOf(Bukkit.getPluginManager().getPlugins().length));
    }
}
