package me.x1no4ekk.core.utility.world;

import me.x1no4ekk.core.CorePlugin;
import me.x1no4ekk.core.command.BaseCommand;
import me.x1no4ekk.core.util.Text;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * {@code /pweather} opens the menu; {@code /pweather <option|reset> [player]} sets the personal weather directly.
 */
final class PWeatherCommand extends BaseCommand {

    private final WorldModule module;

    PWeatherCommand(CorePlugin plugin, WorldModule module) {
        super(plugin);
        this.module = module;
    }

    @Override
    protected void execute(CommandSender sender, String label, String[] args) {
        requirePermission(sender, "core.pweather");
        WorldSettings.PersonalMenu<WorldSettings.WeatherOption> menu = module.settings().pweatherMenu();
        if (args.length == 0) {
            Player player = playerOnly(sender);
            new PWeatherGui(module, menu).open(player);
            menu.sound().play(player);
            return;
        }
        if (args.length > 2) {
            throw usage("utility.pweather.usage");
        }
        Player target = args.length == 2 ? otherPlayer(sender, args[1], "core.pweather.others") : playerOnly(sender);
        String value = args[0].toLowerCase(Locale.ROOT);
        Component name;
        if (value.equals("reset")) {
            module.resetPlayerWeather(target);
            name = messages().get("utility.pweather.server-weather");
        } else {
            WorldSettings.WeatherOption option = find(menu, value);
            if (option == null) {
                throw usage("utility.pweather.usage");
            }
            module.setPlayerWeather(target, option);
            name = Text.parse(option.label());
        }
        if (!target.equals(sender)) {
            send(sender, "utility.pweather.changed-other", Placeholder.component("player", target.displayName()),
                    Placeholder.component("weather", name));
        }
    }

    /**
     * By option key, then by weather type name ({@code clear}, {@code downfall}).
     */
    private static WorldSettings.WeatherOption find(WorldSettings.PersonalMenu<WorldSettings.WeatherOption> menu, String key) {
        for (WorldSettings.WeatherOption option : menu.options()) {
            if (option.key().equals(key)) {
                return option;
            }
        }
        for (WorldSettings.WeatherOption option : menu.options()) {
            if (option.type().name().equalsIgnoreCase(key)) {
                return option;
            }
        }
        return null;
    }

    @Override
    protected List<String> complete(CommandSender sender, String label, String[] args) {
        if (args.length == 1) {
            List<String> options = new ArrayList<>();
            for (WorldSettings.WeatherOption option : module.settings().pweatherMenu().options()) {
                options.add(option.key());
            }
            options.add("reset");
            return filter(options, args[0]);
        }
        if (args.length == 2 && sender.hasPermission("core.pweather.others")) {
            return players(sender, args[1]);
        }
        return List.of();
    }
}
