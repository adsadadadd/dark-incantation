package me.x1no4ekk.core.warp.command;

import me.x1no4ekk.core.CorePlugin;
import me.x1no4ekk.core.command.BaseCommand;
import me.x1no4ekk.core.warp.Warp;
import me.x1no4ekk.core.warp.WarpModule;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

/**
 * /editwarp &lt;warp&gt; &lt;description|icon|move&gt; [value] — edits an own player warp.
 */
public final class EditWarpCommand extends BaseCommand {

    private static final String PERMISSION = "core.editwarp";
    private static final String OTHERS_PERMISSION = "core.editwarp.others";
    private static final List<String> ACTIONS = List.of("description", "icon", "move");

    private final WarpModule module;

    public EditWarpCommand(CorePlugin plugin, WarpModule module) {
        super(plugin);
        this.module = module;
    }

    @Override
    protected void execute(CommandSender sender, String label, String[] args) {
        requirePermission(sender, PERMISSION);
        if (args.length < 2) {
            throw usage("warp.editwarp-usage");
        }
        if (!module.manager().isLoaded()) {
            throw fail("warp.loading");
        }
        Warp warp = module.manager().get(args[0]);
        if (warp == null) {
            throw fail("warp.not-found", placeholder("warp", args[0]));
        }
        if (warp.system()) {
            throw fail("warp.system-edit");
        }
        UUID senderId = sender instanceof Player player ? player.getUniqueId() : null;
        if (!warp.isOwnedBy(senderId) && !sender.hasPermission(OTHERS_PERMISSION)) {
            throw fail("warp.not-owner", placeholder("warp", warp.name()));
        }
        switch (args[1].toLowerCase(Locale.ROOT)) {
            case "description" -> {
                String description = WarpEdits.description(module, args, 2);
                WarpEdits.apply(plugin, module, sender, warp, warp.withDescription(description));
                send(sender, description == null ? "warp.description-cleared" : "warp.description-set",
                        placeholder("warp", warp.name()));
            }
            case "icon" -> {
                if (args.length > 3) {
                    throw usage("warp.editwarp-usage");
                }
                Optional<Material> icon = WarpEdits.icon(sender, args.length == 3 ? args[2] : null);
                WarpEdits.apply(plugin, module, sender, warp, warp.withIcon(icon.orElse(null)));
                send(sender, icon.isPresent() ? "warp.icon-set" : "warp.icon-reset", placeholder("warp", warp.name()),
                        placeholder("icon", icon.map(material -> material.name().toLowerCase(Locale.ROOT)).orElse("")));
            }
            case "move" -> {
                Player player = playerOnly(sender);
                WarpEdits.checkMove(module, player, false);
                WarpEdits.apply(plugin, module, sender, warp, warp.withLocation(player.getLocation()));
                send(sender, "warp.moved", placeholder("warp", warp.name()));
            }
            default -> throw usage("warp.editwarp-usage");
        }
    }

    @Override
    protected List<String> complete(CommandSender sender, String label, String[] args) {
        if (!sender.hasPermission(PERMISSION)) {
            return List.of();
        }
        if (args.length == 1) {
            if (sender.hasPermission(OTHERS_PERMISSION)) {
                return WarpNames.matching(module, args[0], warp -> !warp.system());
            }
            if (!(sender instanceof Player player)) {
                return List.of();
            }
            UUID uuid = player.getUniqueId();
            return WarpNames.matching(module, args[0], warp -> !warp.system() && warp.isOwnedBy(uuid));
        }
        if (args.length == 2) {
            return filter(ACTIONS, args[1]);
        }
        if (args.length == 3 && args[1].equalsIgnoreCase("icon")) {
            return filter(List.of(WarpEdits.RESET), args[2]);
        }
        return List.of();
    }
}
