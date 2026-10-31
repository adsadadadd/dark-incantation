package me.x1no4ekk.core.warp.command;

import me.x1no4ekk.core.CorePlugin;
import me.x1no4ekk.core.command.BaseCommand;
import me.x1no4ekk.core.warp.Warp;
import me.x1no4ekk.core.warp.WarpModule;
import me.x1no4ekk.core.warp.WarpPlaceholders;
import org.bukkit.command.CommandSender;

import java.util.List;

/**
 * /warpinfo &lt;name&gt; — owner, coordinates, visits, rating, creation date and description of a warp.
 */
public final class WarpInfoCommand extends BaseCommand {

    private static final String PERMISSION = "core.warpinfo";

    private final WarpModule module;

    public WarpInfoCommand(CorePlugin plugin, WarpModule module) {
        super(plugin);
        this.module = module;
    }

    @Override
    protected void execute(CommandSender sender, String label, String[] args) {
        requirePermission(sender, PERMISSION);
        if (args.length != 1) {
            throw usage("warp.warpinfo-usage");
        }
        if (!module.manager().isLoaded()) {
            throw fail("warp.loading");
        }
        Warp warp = module.manager().get(args[0]);
        if (warp == null) {
            throw fail("warp.not-found", placeholder("warp", args[0]));
        }
        send(sender, warp.system() ? "warp.info-system" : "warp.info",
                WarpPlaceholders.of(plugin, warp, module.manager().rating(warp.key()), module.settings()));
    }

    @Override
    protected List<String> complete(CommandSender sender, String label, String[] args) {
        return args.length == 1 && sender.hasPermission(PERMISSION) ? WarpNames.all(module, args[0]) : List.of();
    }
}
