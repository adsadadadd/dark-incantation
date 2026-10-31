package me.x1no4ekk.core.warp.command;

import me.x1no4ekk.core.CorePlugin;
import me.x1no4ekk.core.command.BaseCommand;
import me.x1no4ekk.core.warp.Warp;
import me.x1no4ekk.core.warp.WarpModule;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

/**
 * /delwarp &lt;name&gt; — deletes an own warp (any player warp with {@code core.warp.delete.others}).
 */
public final class DelWarpCommand extends BaseCommand {

    private static final String PERMISSION = "core.delwarp";
    private static final String OTHERS_PERMISSION = "core.warp.delete.others";

    private final WarpModule module;

    public DelWarpCommand(CorePlugin plugin, WarpModule module) {
        super(plugin);
        this.module = module;
    }

    @Override
    protected void execute(CommandSender sender, String label, String[] args) {
        requirePermission(sender, PERMISSION);
        if (args.length != 1) {
            throw usage("warp.delwarp-usage");
        }
        if (!module.manager().isLoaded()) {
            throw fail("warp.loading");
        }
        Warp warp = module.manager().get(args[0]);
        if (warp == null) {
            throw fail("warp.not-found", placeholder("warp", args[0]));
        }
        if (warp.system()) {
            throw fail("warp.system-delete");
        }
        UUID senderId = sender instanceof Player player ? player.getUniqueId() : null;
        if (!warp.isOwnedBy(senderId) && !sender.hasPermission(OTHERS_PERMISSION)) {
            throw fail("warp.not-owner", placeholder("warp", warp.name()));
        }
        module.manager().delete(warp, error -> {
            CommandSender actor = senderId == null ? Bukkit.getConsoleSender() : Bukkit.getPlayer(senderId);
            if (actor != null) {
                send(actor, "general.database-error");
            }
        });
        send(sender, "warp.deleted", placeholder("warp", warp.name()));
    }

    @Override
    protected List<String> complete(CommandSender sender, String label, String[] args) {
        if (args.length != 1 || !sender.hasPermission(PERMISSION)) {
            return List.of();
        }
        if (sender.hasPermission(OTHERS_PERMISSION)) {
            return WarpNames.matching(module, args[0], warp -> !warp.system());
        }
        if (!(sender instanceof Player player)) {
            return List.of();
        }
        UUID uuid = player.getUniqueId();
        return WarpNames.matching(module, args[0], warp -> !warp.system() && warp.isOwnedBy(uuid));
    }
}
