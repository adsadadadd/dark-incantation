package me.x1no4ekk.core.utility.info;

import me.x1no4ekk.core.CorePlugin;
import me.x1no4ekk.core.command.BaseCommand;
import me.x1no4ekk.core.util.Durations;
import me.x1no4ekk.core.util.Numbers;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;

import java.lang.management.ManagementFactory;
import java.util.Locale;

/**
 * {@code /gc}: TPS, tick time, memory, uptime and per-world counters (cheap counters only, no chunk iteration).
 */
final class GcCommand extends BaseCommand {

    private static final long MB = 1024L * 1024L;

    GcCommand(CorePlugin plugin) {
        super(plugin);
    }

    @Override
    protected void execute(CommandSender sender, String label, String[] args) {
        requirePermission(sender, "core.gc");
        double[] tps = Bukkit.getTPS();
        double mspt = Bukkit.getAverageTickTime();
        Runtime runtime = Runtime.getRuntime();
        long allocated = runtime.totalMemory() / MB;
        long used = (runtime.totalMemory() - runtime.freeMemory()) / MB;
        send(sender, "utility.gc.header", TagResolver.resolver(
                Placeholder.component("tps-1m", tps(tps[0])),
                Placeholder.component("tps-5m", tps(tps[1])),
                Placeholder.component("tps-15m", tps(tps[2])),
                Placeholder.component("mspt", Component.text(String.format(Locale.ROOT, "%.2f", mspt),
                        mspt <= 40.0 ? NamedTextColor.GREEN : mspt <= 50.0 ? NamedTextColor.YELLOW : NamedTextColor.RED)),
                placeholder("uptime", Durations.format(ManagementFactory.getRuntimeMXBean().getUptime())),
                placeholder("used", Numbers.format(used)),
                placeholder("allocated", Numbers.format(allocated)),
                placeholder("max", Numbers.format(runtime.maxMemory() / MB)),
                placeholder("java", System.getProperty("java.version")),
                placeholder("online", Bukkit.getOnlinePlayers().size()),
                placeholder("max-players", Bukkit.getMaxPlayers()),
                placeholder("worlds", Bukkit.getWorlds().size())));
        for (World world : Bukkit.getWorlds()) {
            send(sender, "utility.gc.world",
                    placeholder("world", world.getName()),
                    placeholder("chunks", Numbers.format(world.getChunkCount())),
                    placeholder("entities", Numbers.format(world.getEntityCount())),
                    placeholder("tiles", Numbers.format(world.getTileEntityCount())),
                    placeholder("players", world.getPlayerCount()));
        }
        send(sender, "utility.gc.footer");
    }

    private static Component tps(double value) {
        double shown = Math.min(20.0, value);
        NamedTextColor color = shown >= 18.0 ? NamedTextColor.GREEN : shown >= 15.0 ? NamedTextColor.YELLOW : NamedTextColor.RED;
        return Component.text(String.format(Locale.ROOT, "%.2f", shown), color);
    }
}
