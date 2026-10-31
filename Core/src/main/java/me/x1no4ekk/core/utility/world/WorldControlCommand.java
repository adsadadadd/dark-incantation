package me.x1no4ekk.core.utility.world;

import me.x1no4ekk.core.CorePlugin;
import me.x1no4ekk.core.command.BaseCommand;
import me.x1no4ekk.core.user.User;
import me.x1no4ekk.core.util.Durations;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Shared flow of the world time and weather commands: allowed worlds, the per-world cooldown and announcements.
 */
abstract class WorldControlCommand extends BaseCommand {

    protected final WorldModule module;
    private final boolean weather;
    private final String namespace;

    protected WorldControlCommand(CorePlugin plugin, WorldModule module, boolean weather) {
        super(plugin);
        this.module = module;
        this.weather = weather;
        this.namespace = weather ? "utility.weather." : "utility.time.";
    }

    private WorldSettings.Control control() {
        return weather ? module.settings().weather() : module.settings().time();
    }

    /**
     * The named world, otherwise the sender's world if allowed, otherwise the first allowed world.
     */
    protected World resolveWorld(CommandSender sender, String name) {
        WorldSettings.Control control = control();
        if (name != null) {
            World world = Bukkit.getWorld(name);
            if (world == null) {
                throw fail("utility.world-not-found", placeholder("world", name));
            }
            if (!control.allows(world)) {
                throw fail(namespace + "world-disabled", placeholder("world", world.getName()));
            }
            return world;
        }
        if (sender instanceof Player player && control.allows(player.getWorld())) {
            return player.getWorld();
        }
        for (World world : Bukkit.getWorlds()) {
            if (control.allows(world)) {
                return world;
            }
        }
        throw fail(namespace + "no-world");
    }

    /**
     * The cooldown is shared by everyone per world; staff with the bypass permission ignore it.
     */
    protected void checkCooldown(CommandSender sender, World world) {
        if (sender.hasPermission(weather ? "core.weather.bypass-cooldown" : "core.time.bypass-cooldown")) {
            return;
        }
        long left = module.cooldownLeft(world.getName(), weather);
        if (left > 0) {
            throw fail(namespace + "cooldown", placeholder("time", Durations.format(left)),
                    placeholder("world", world.getName()));
        }
    }

    /**
     * Starts the world's cooldown, confirms to the sender and broadcasts the change (unless the sender is vanished).
     */
    protected void announce(CommandSender sender, World world, TagResolver resolvers) {
        module.startCooldown(world.getName(), weather);
        send(sender, namespace + "changed", resolvers);
        if (sender instanceof Player player && isVanished(player)) {
            return;
        }
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (!online.equals(sender)) {
                send(online, namespace + "broadcast", resolvers);
            }
        }
    }

    protected List<String> worldNames(String input) {
        List<String> names = new ArrayList<>();
        WorldSettings.Control control = control();
        for (World world : Bukkit.getWorlds()) {
            if (control.allows(world)) {
                names.add(world.getName());
            }
        }
        return filter(names, input);
    }

    protected static boolean isInteger(String raw) {
        try {
            Long.parseLong(raw);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean isVanished(Player player) {
        User user = plugin.userManager().getUser(player);
        return user != null && user.isVanish();
    }
}
