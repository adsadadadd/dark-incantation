package me.x1no4ekk.core.warp.command;

import me.x1no4ekk.core.CorePlugin;
import me.x1no4ekk.core.command.BaseCommand;
import me.x1no4ekk.core.warp.WarpManager;
import me.x1no4ekk.core.warp.WarpModule;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * /setwarp &lt;name&gt; [description] — creates a personal warp at the sender's position.
 */
public final class SetWarpCommand extends BaseCommand {

    private static final String PERMISSION = "core.setwarp";

    private final WarpModule module;

    public SetWarpCommand(CorePlugin plugin, WarpModule module) {
        super(plugin);
        this.module = module;
    }

    @Override
    protected void execute(CommandSender sender, String label, String[] args) {
        Player player = playerOnly(sender);
        requirePermission(sender, PERMISSION);
        if (args.length == 0) {
            throw usage("warp.setwarp-usage");
        }
        String name = args[0];
        String description = args.length > 1 ? String.join(" ", Arrays.copyOfRange(args, 1, args.length)).trim() : null;
        if (description != null && description.length() > module.settings().descriptionMaxLength()) {
            throw fail("warp.description-too-long", placeholder("max", module.settings().descriptionMaxLength()));
        }
        UUID uuid = player.getUniqueId();
        WarpManager manager = module.manager();
        WarpManager.CreateResult result = manager.create(player, name, description, error -> {
            Player online = Bukkit.getPlayer(uuid);
            if (online != null) {
                send(online, WarpManager.isDuplicate(error) ? "warp.already-exists" : "general.database-error",
                        placeholder("warp", name));
            }
        });
        switch (result) {
            case CREATED -> send(player, "warp.created", placeholder("warp", name));
            case NOT_LOADED -> send(player, "warp.loading");
            case INVALID_NAME -> send(player, "warp.invalid-name");
            case ALREADY_EXISTS -> send(player, "warp.already-exists", placeholder("warp", name));
            case LIMIT_REACHED -> {
                int limit = manager.limit(player);
                send(player, limit == 0 ? "warp.no-warps-allowed" : "warp.limit", placeholder("limit", limit));
            }
            case DISABLED_WORLD -> send(player, "warp.disabled-world");
            case SPECTATOR -> send(player, "warp.spectator");
        }
    }

    @Override
    protected List<String> complete(CommandSender sender, String label, String[] args) {
        return List.of();
    }
}
