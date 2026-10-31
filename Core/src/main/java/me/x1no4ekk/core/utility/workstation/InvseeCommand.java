package me.x1no4ekk.core.utility.workstation;

import me.x1no4ekk.core.CorePlugin;
import me.x1no4ekk.core.command.BaseCommand;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * {@code /invsee <player>}: opens the target's live inventory, read-only without {@code core.invsee.modify}.
 */
final class InvseeCommand extends BaseCommand {

    static final String PERMISSION = "core.invsee";
    static final String MODIFY = "core.invsee.modify";
    static final String EXEMPT = "core.invsee.exempt";
    static final String EXEMPT_BYPASS = "core.invsee.exempt.bypass";

    InvseeCommand(CorePlugin plugin) {
        super(plugin);
    }

    @Override
    protected void execute(CommandSender sender, String label, String[] args) {
        Player viewer = playerOnly(sender);
        requirePermission(viewer, PERMISSION);
        if (args.length != 1) {
            throw usage("utility.invsee.usage");
        }
        Player target = player(viewer, args[0]);
        if (target.equals(viewer)) {
            throw fail("utility.invsee.self");
        }
        if (target.hasPermission(EXEMPT) && !viewer.hasPermission(EXEMPT_BYPASS)) {
            throw fail("utility.invsee.exempt", Placeholder.component("player", target.displayName()));
        }
        // Opening the live PlayerInventory keeps the view in sync with the target without copying items.
        viewer.openInventory(target.getInventory());
        send(viewer, viewer.hasPermission(MODIFY) ? "utility.invsee.opened-modify" : "utility.invsee.opened",
                Placeholder.component("player", target.displayName()));
    }

    @Override
    protected List<String> complete(CommandSender sender, String label, String[] args) {
        return args.length == 1 ? players(sender, args[0]) : List.of();
    }
}
