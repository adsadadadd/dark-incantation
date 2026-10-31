package me.x1no4ekk.core.utility.info;

import me.x1no4ekk.core.CorePlugin;
import me.x1no4ekk.core.command.BaseCommand;
import me.x1no4ekk.core.util.Text;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.function.Function;

/**
 * {@code /rules} and {@code /links}: MiniMessage lines from the settings (links via {@code <click:open_url:...>}).
 */
final class LinesCommand extends BaseCommand {

    private final InfoModule module;
    private final String name;
    private final Function<InfoSettings, List<String>> lines;

    LinesCommand(CorePlugin plugin, InfoModule module, String name, Function<InfoSettings, List<String>> lines) {
        super(plugin);
        this.module = module;
        this.name = name;
        this.lines = lines;
    }

    @Override
    protected void execute(CommandSender sender, String label, String[] args) {
        requirePermission(sender, "core." + name);
        List<String> text = lines.apply(module.settings());
        if (text.isEmpty()) {
            throw fail("utility." + name + ".empty");
        }
        for (String line : text) {
            sender.sendMessage(Text.parse(line, messages().prefixResolver()));
        }
    }
}
