package me.x1no4ekk.core.warp.command;

import me.x1no4ekk.core.CorePlugin;
import me.x1no4ekk.core.command.BaseCommand;
import me.x1no4ekk.core.warp.Warp;
import me.x1no4ekk.core.warp.WarpManager;
import me.x1no4ekk.core.warp.WarpModule;
import me.x1no4ekk.core.warp.gui.SystemWarpGui;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * /serverwarp — opens the system warps menu; administrators manage system warps with sub-commands:
 * create, delete, list, icon, slot, description, move.
 */
public final class ServerWarpCommand extends BaseCommand {

    private static final String PERMISSION = "core.serverwarp";
    private static final String ADMIN_PERMISSION = "core.serverwarp.admin";
    private static final List<String> ACTIONS = List.of("create", "delete", "list", "icon", "slot", "description", "move");
    private static final String NO_SLOT = "none";

    private final WarpModule module;

    public ServerWarpCommand(CorePlugin plugin, WarpModule module) {
        super(plugin);
        this.module = module;
    }

    @Override
    protected void execute(CommandSender sender, String label, String[] args) {
        requirePermission(sender, PERMISSION);
        if (!module.manager().isLoaded()) {
            throw fail("warp.loading");
        }
        if (args.length == 0) {
            Player player = playerOnly(sender);
            new SystemWarpGui(plugin, module).open(player);
            return;
        }
        requirePermission(sender, ADMIN_PERMISSION);
        String action = args[0].toLowerCase(Locale.ROOT);
        switch (action) {
            case "create" -> create(sender, args);
            case "list" -> list(sender);
            case "delete", "icon", "slot", "description", "move" -> {
                if (args.length < 2) {
                    throw usage("warp.serverwarp-usage");
                }
                Warp warp = module.manager().get(args[1]);
                if (warp == null || !warp.system()) {
                    throw fail("warp.system-not-found", placeholder("warp", args[1]));
                }
                switch (action) {
                    case "delete" -> delete(sender, warp);
                    case "icon" -> icon(sender, warp, args);
                    case "slot" -> slot(sender, warp, args);
                    case "description" -> description(sender, warp, args);
                    default -> move(sender, warp);
                }
            }
            default -> throw usage("warp.serverwarp-usage");
        }
    }

    private void create(CommandSender sender, String[] args) {
        Player player = playerOnly(sender);
        if (args.length < 2 || args.length > 4) {
            throw usage("warp.serverwarp-usage");
        }
        String name = args[1];
        Material icon = args.length >= 3 ? WarpEdits.icon(sender, args[2]).orElse(null) : null;
        int slot = args.length == 4 ? parseSlot(args[3]) : -1;
        UUID uuid = player.getUniqueId();
        WarpManager.CreateResult result = module.manager().createSystem(player, name, icon, slot, error -> {
            Player online = Bukkit.getPlayer(uuid);
            if (online != null) {
                send(online, WarpManager.isDuplicate(error) ? "warp.already-exists" : "general.database-error",
                        placeholder("warp", name));
            }
        });
        switch (result) {
            case CREATED -> send(player, "warp.system-created", placeholder("warp", name));
            case NOT_LOADED -> send(player, "warp.loading");
            case INVALID_NAME -> send(player, "warp.invalid-name");
            case ALREADY_EXISTS -> send(player, "warp.already-exists", placeholder("warp", name));
            case SPECTATOR -> send(player, "warp.spectator");
            case LIMIT_REACHED, DISABLED_WORLD -> send(player, "warp.disabled-world");
        }
    }

    private void list(CommandSender sender) {
        List<Warp> warps = module.manager().systemWarps();
        if (warps.isEmpty()) {
            throw fail("warp.system-list-empty");
        }
        String names = warps.stream()
                .map(warp -> warp.slot() >= 0 ? warp.name() + " (" + warp.slot() + ")" : warp.name())
                .collect(Collectors.joining(", "));
        send(sender, "warp.system-list", placeholder("count", warps.size()), placeholder("warps", names));
    }

    private void delete(CommandSender sender, Warp warp) {
        UUID senderId = sender instanceof Player player ? player.getUniqueId() : null;
        module.manager().delete(warp, error -> {
            CommandSender actor = senderId == null ? Bukkit.getConsoleSender() : Bukkit.getPlayer(senderId);
            if (actor != null) {
                send(actor, "general.database-error");
            }
        });
        send(sender, "warp.system-deleted", placeholder("warp", warp.name()));
    }

    private void icon(CommandSender sender, Warp warp, String[] args) {
        if (args.length > 3) {
            throw usage("warp.serverwarp-usage");
        }
        Optional<Material> icon = WarpEdits.icon(sender, args.length == 3 ? args[2] : null);
        WarpEdits.apply(plugin, module, sender, warp, warp.withIcon(icon.orElse(null)));
        send(sender, icon.isPresent() ? "warp.icon-set" : "warp.icon-reset", placeholder("warp", warp.name()),
                placeholder("icon", icon.map(material -> material.name().toLowerCase(Locale.ROOT)).orElse("")));
    }

    private void slot(CommandSender sender, Warp warp, String[] args) {
        if (args.length != 3) {
            throw usage("warp.serverwarp-usage");
        }
        int slot = parseSlot(args[2]);
        WarpEdits.apply(plugin, module, sender, warp, warp.withSlot(slot));
        if (slot < 0) {
            send(sender, "warp.slot-cleared", placeholder("warp", warp.name()));
        } else {
            send(sender, "warp.slot-set", placeholder("warp", warp.name()), placeholder("slot", slot));
        }
    }

    private void description(CommandSender sender, Warp warp, String[] args) {
        String description = WarpEdits.description(module, args, 2);
        WarpEdits.apply(plugin, module, sender, warp, warp.withDescription(description));
        send(sender, description == null ? "warp.description-cleared" : "warp.description-set",
                placeholder("warp", warp.name()));
    }

    private void move(CommandSender sender, Warp warp) {
        Player player = playerOnly(sender);
        WarpEdits.checkMove(module, player, true);
        WarpEdits.apply(plugin, module, sender, warp, warp.withLocation(player.getLocation()));
        send(sender, "warp.moved", placeholder("warp", warp.name()));
    }

    /**
     * @return a slot of the system warps menu, or -1 for {@code none}
     */
    private int parseSlot(String raw) {
        if (raw.equalsIgnoreCase(NO_SLOT)) {
            return -1;
        }
        return parseInt(raw, 0, module.settings().system().rows() * 9 - 1);
    }

    @Override
    protected List<String> complete(CommandSender sender, String label, String[] args) {
        if (!sender.hasPermission(ADMIN_PERMISSION)) {
            return List.of();
        }
        if (args.length == 1) {
            return filter(ACTIONS, args[0]);
        }
        String action = args[0].toLowerCase(Locale.ROOT);
        if (args.length == 2 && !action.equals("create") && !action.equals("list") && ACTIONS.contains(action)) {
            return WarpNames.matching(module, args[1], Warp::system);
        }
        if (args.length == 3 && action.equals("icon")) {
            return filter(List.of(WarpEdits.RESET), args[2]);
        }
        if ((args.length == 3 && action.equals("slot")) || (args.length == 4 && action.equals("create"))) {
            return filter(List.of(NO_SLOT), args[args.length - 1]);
        }
        return List.of();
    }
}
