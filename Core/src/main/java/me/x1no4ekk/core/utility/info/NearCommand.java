package me.x1no4ekk.core.utility.info;

import me.x1no4ekk.core.CorePlugin;
import me.x1no4ekk.core.command.BaseCommand;
import me.x1no4ekk.core.util.Numbers;
import me.x1no4ekk.core.utility.UtilityText;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

/**
 * {@code /near [radius]}: visible players nearby in the same world, nearest first.
 */
final class NearCommand extends BaseCommand {

    private record Nearby(Player player, double distanceSquared) {
    }

    private final InfoModule module;

    NearCommand(CorePlugin plugin, InfoModule module) {
        super(plugin);
        this.module = module;
    }

    @Override
    protected void execute(CommandSender sender, String label, String[] args) {
        Player player = playerOnly(sender);
        requirePermission(player, "core.near");
        if (args.length > 1) {
            throw usage("utility.near.usage");
        }
        InfoSettings.Near settings = module.settings().near();
        int allowed = settings.allowedRadius(player);
        int radius = args.length == 1 ? parseInt(args[0], 1, allowed) : Math.min(settings.defaultRadius(), allowed);
        double radiusSquared = (double) radius * radius;
        Location origin = player.getLocation();
        List<Nearby> found = new ArrayList<>();
        for (Player other : player.getWorld().getPlayers()) {
            if (other.equals(player) || !player.canSee(other) || other.getGameMode() == GameMode.SPECTATOR
                    || (settings.hideInvisible() && other.hasPotionEffect(PotionEffectType.INVISIBILITY))) {
                continue;
            }
            double distanceSquared = other.getLocation().distanceSquared(origin);
            if (distanceSquared <= radiusSquared) {
                found.add(new Nearby(other, distanceSquared));
            }
        }
        if (found.isEmpty()) {
            throw fail("utility.near.nobody", placeholder("radius", radius));
        }
        found.sort(Comparator.comparingDouble(Nearby::distanceSquared));
        send(player, "utility.near.header", placeholder("radius", radius), placeholder("count", found.size()));
        boolean clickable = player.hasPermission("core.invsee");
        for (Nearby nearby : found.subList(0, Math.min(settings.maxEntries(), found.size()))) {
            Player other = nearby.player();
            Location location = other.getLocation();
            Component line = messages().get("utility.near.entry",
                    Placeholder.component("player", other.displayName()),
                    placeholder("distance", Numbers.format(Math.round(Math.sqrt(nearby.distanceSquared())))),
                    placeholder("direction", UtilityText.arrow(origin, location.getX(), location.getZ())),
                    placeholder("y", location.getBlockY()));
            if (clickable) {
                line = line.clickEvent(ClickEvent.runCommand("/invsee " + other.getName()))
                        .hoverEvent(HoverEvent.showText(messages().get("utility.near.hover")));
            }
            player.sendMessage(line);
        }
    }

    @Override
    protected List<String> complete(CommandSender sender, String label, String[] args) {
        if (args.length == 1 && sender instanceof Player player) {
            InfoSettings.Near settings = module.settings().near();
            int allowed = settings.allowedRadius(player);
            return filter(Stream.of(Math.min(settings.defaultRadius(), allowed), allowed).distinct().map(String::valueOf), args[0]);
        }
        return List.of();
    }
}
