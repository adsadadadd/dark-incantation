package me.x1no4ekk.core.utility.workstation;

import me.x1no4ekk.core.CorePlugin;
import me.x1no4ekk.core.command.BaseCommand;
import me.x1no4ekk.core.util.Text;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * {@code /workbench}, {@code /anvil} ... {@code [player]}: opens a virtual workstation.
 */
final class WorkstationCommand extends BaseCommand {

    private final WorkstationModule module;
    private final Workstation workstation;

    WorkstationCommand(CorePlugin plugin, WorkstationModule module, Workstation workstation) {
        super(plugin);
        this.module = module;
        this.workstation = workstation;
    }

    @Override
    protected void execute(CommandSender sender, String label, String[] args) {
        requirePermission(sender, workstation.permission());
        if (args.length > 1) {
            throw fail("utility.workstation.usage", placeholder("command", label));
        }
        Player target = args.length == 0
                ? playerOnly(sender)
                : otherPlayer(sender, args[0], workstation.permission() + ".others");
        workstation.open(target, Text.parse(module.settings().title(workstation)));
        if (!target.equals(sender)) {
            send(sender, "utility.workstation.opened-other",
                    Placeholder.component("player", target.displayName()),
                    placeholder("command", label));
        }
    }

    @Override
    protected List<String> complete(CommandSender sender, String label, String[] args) {
        if (args.length == 1 && sender.hasPermission(workstation.permission() + ".others")) {
            return players(sender, args[0]);
        }
        return List.of();
    }
}
