package me.x1no4ekk.core.utility.world;

import me.x1no4ekk.core.CorePlugin;
import me.x1no4ekk.core.command.BaseCommand;
import me.x1no4ekk.core.util.Text;
import me.x1no4ekk.core.utility.UtilityText;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * {@code /ptime} opens the menu; {@code /ptime <option|ticks|reset> [player]} sets the personal time directly.
 */
final class PTimeCommand extends BaseCommand {

    private final WorldModule module;

    PTimeCommand(CorePlugin plugin, WorldModule module) {
        super(plugin);
        this.module = module;
    }

    @Override
    protected void execute(CommandSender sender, String label, String[] args) {
        requirePermission(sender, "core.ptime");
        WorldSettings.PersonalMenu<WorldSettings.TimeOption> menu = module.settings().ptimeMenu();
        if (args.length == 0) {
            Player player = playerOnly(sender);
            new PTimeGui(module, menu).open(player);
            menu.sound().play(player);
            return;
        }
        if (args.length > 2) {
            throw usage("utility.ptime.usage");
        }
        Player target = args.length == 2 ? otherPlayer(sender, args[1], "core.ptime.others") : playerOnly(sender);
        String value = args[0].toLowerCase(Locale.ROOT);
        Component name;
        if (value.equals("reset")) {
            module.resetPlayerTime(target);
            name = messages().get("utility.ptime.server-time");
        } else {
            WorldSettings.TimeOption option = find(menu, value);
            if (option != null) {
                module.setPlayerTime(target, option);
                name = Text.parse(option.label());
            } else {
                long ticks = parseInt(value, 0, 23_999);
                name = messages().get("utility.time.names.ticks", placeholder("ticks", ticks),
                        placeholder("clock", UtilityText.clock(ticks)));
                module.setPlayerTime(target, ticks, name);
            }
        }
        if (!target.equals(sender)) {
            send(sender, "utility.ptime.changed-other", Placeholder.component("player", target.displayName()),
                    Placeholder.component("time", name));
        }
    }

    private static WorldSettings.TimeOption find(WorldSettings.PersonalMenu<WorldSettings.TimeOption> menu, String key) {
        for (WorldSettings.TimeOption option : menu.options()) {
            if (option.key().equals(key)) {
                return option;
            }
        }
        return null;
    }

    @Override
    protected List<String> complete(CommandSender sender, String label, String[] args) {
        if (args.length == 1) {
            List<String> options = new ArrayList<>();
            for (WorldSettings.TimeOption option : module.settings().ptimeMenu().options()) {
                options.add(option.key());
            }
            options.add("reset");
            return filter(options, args[0]);
        }
        if (args.length == 2 && sender.hasPermission("core.ptime.others")) {
            return players(sender, args[1]);
        }
        return List.of();
    }
}
