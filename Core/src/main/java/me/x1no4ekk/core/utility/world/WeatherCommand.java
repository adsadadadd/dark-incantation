package me.x1no4ekk.core.utility.world;

import me.x1no4ekk.core.CorePlugin;
import me.x1no4ekk.core.util.Durations;
import me.x1no4ekk.core.utility.UtilityText;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * {@code /sun}, {@code /rain}, {@code /storm} {@code [seconds] [world]} and
 * {@code /weather <clear|rain|thunder> [seconds] [world]}.
 */
final class WeatherCommand extends WorldControlCommand {

    enum Kind {
        CLEAR, RAIN, THUNDER;

        String key() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

    private static final Map<String, Kind> NAMES = Map.of(
            "clear", Kind.CLEAR, "sun", Kind.CLEAR,
            "rain", Kind.RAIN,
            "thunder", Kind.THUNDER, "storm", Kind.THUNDER);
    private static final int MAX_SECONDS = 1_000_000;

    /** Weather of the shortcut commands, {@code null} for {@code /weather}. */
    private final Kind fixed;
    private final String permission;

    WeatherCommand(CorePlugin plugin, WorldModule module, Kind fixed, String permission) {
        super(plugin, module, true);
        this.fixed = fixed;
        this.permission = permission;
    }

    @Override
    protected void execute(CommandSender sender, String label, String[] args) {
        requirePermission(sender, permission);
        Kind kind = fixed;
        int index = 0;
        if (kind == null) {
            if (args.length == 0) {
                showCurrent(sender);
                return;
            }
            kind = NAMES.get(args[0].toLowerCase(Locale.ROOT));
            if (kind == null) {
                throw usage("utility.weather.usage");
            }
            index = 1;
        }
        int seconds = module.settings().defaultWeatherSeconds();
        if (args.length > index && isInteger(args[index])) {
            seconds = parseInt(args[index], 1, MAX_SECONDS);
            index++;
        }
        if (args.length > index + 1) {
            throw fixed == null
                    ? usage("utility.weather.usage")
                    : fail("utility.weather.fixed-usage", placeholder("command", label));
        }
        World world = resolveWorld(sender, args.length > index ? args[index] : null);
        checkCooldown(sender, world);
        apply(world, kind, seconds * 20);
        Component duration = seconds > 0
                ? Component.text(Durations.formatSeconds(seconds))
                : messages().get("utility.weather.random-duration");
        announce(sender, world, TagResolver.resolver(
                Placeholder.component("weather", messages().get("utility.weather.names." + kind.key())),
                placeholder("world", world.getName()),
                Placeholder.component("duration", duration),
                Placeholder.component("player", UtilityText.senderName(messages(), sender))));
    }

    /**
     * Mirrors vanilla {@code /weather}: a duration of 0 lets the server pick a random one.
     * setStorm/setThundering reset the durations, so durations are set last.
     */
    private static void apply(World world, Kind kind, int ticks) {
        switch (kind) {
            case CLEAR -> {
                world.setStorm(false);
                world.setThundering(false);
                world.setClearWeatherDuration(ticks);
            }
            case RAIN -> {
                world.setStorm(true);
                world.setThundering(false);
                world.setWeatherDuration(ticks);
                world.setThunderDuration(ticks);
            }
            case THUNDER -> {
                world.setStorm(true);
                world.setThundering(true);
                world.setWeatherDuration(ticks);
                world.setThunderDuration(ticks);
            }
        }
    }

    private void showCurrent(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            throw usage("utility.weather.usage");
        }
        World world = player.getWorld();
        Kind current = world.isThundering() ? Kind.THUNDER : world.hasStorm() ? Kind.RAIN : Kind.CLEAR;
        send(sender, "utility.weather.current",
                placeholder("world", world.getName()),
                Placeholder.component("weather", messages().get("utility.weather.names." + current.key())));
    }

    @Override
    protected List<String> complete(CommandSender sender, String label, String[] args) {
        int index = fixed == null ? 1 : 0;
        if (fixed == null && args.length == 1) {
            return filter(List.of("clear", "rain", "thunder"), args[0]);
        }
        if (args.length == index + 1) {
            List<String> options = new ArrayList<>(worldNames(args[index]));
            options.addAll(filter(List.of("300", "600", "1200"), args[index]));
            return options;
        }
        if (args.length == index + 2 && isInteger(args[index])) {
            return worldNames(args[index + 1]);
        }
        return List.of();
    }
}
