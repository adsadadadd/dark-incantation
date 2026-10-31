package me.x1no4ekk.core.warp.command;

import me.x1no4ekk.core.CorePlugin;
import me.x1no4ekk.core.command.BaseCommand;
import me.x1no4ekk.core.warp.Warp;
import me.x1no4ekk.core.warp.WarpModule;
import me.x1no4ekk.core.warp.gui.WarpGui;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * /warp — opens the warp menu; /warp &lt;name&gt; teleports to a warp.
 */
public final class WarpCommand extends BaseCommand {

    private static final String PERMISSION = "core.warp";

    private final WarpModule module;

    public WarpCommand(CorePlugin plugin, WarpModule module) {
        super(plugin);
        this.module = module;
    }

    @Override
    protected void execute(CommandSender sender, String label, String[] args) {
        Player player = playerOnly(sender);
        requirePermission(sender, PERMISSION);
        if (!module.manager().isLoaded()) {
            throw fail("warp.loading");
        }
        if (args.length == 0) {
            new WarpGui(plugin, module, player).open(player);
            return;
        }
        if (args.length != 1) {
            throw usage("warp.warp-usage");
        }
        Warp warp = module.manager().get(args[0]);
        if (warp == null) {
            throw fail("warp.not-found", placeholder("warp", args[0]));
        }
        module.manager().teleport(player, warp);
    }

    @Override
    protected List<String> complete(CommandSender sender, String label, String[] args) {
        if (args.length != 1 || !sender.hasPermission(PERMISSION)) {
            return List.of();
        }
        return WarpNames.all(module, args[0]);
    }
}
