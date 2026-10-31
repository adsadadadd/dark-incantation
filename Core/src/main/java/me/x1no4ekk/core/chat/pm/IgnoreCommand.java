package me.x1no4ekk.core.chat.pm;

import me.x1no4ekk.core.CorePlugin;
import me.x1no4ekk.core.chat.ChatPermissions;
import me.x1no4ekk.core.chat.PrivateMessageModule;
import me.x1no4ekk.core.command.BaseCommand;
import me.x1no4ekk.core.command.CommandException;
import me.x1no4ekk.core.user.User;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.logging.Level;

/**
 * /ignore &lt;player&gt; — toggles ignoring a player (online or offline); /ignore list — shows the ignore list.
 * The {@link User} cache changes at once; core_ignores is written asynchronously and reverted on failure.
 */
public final class IgnoreCommand extends BaseCommand {

    private static final String LIST = "list";

    private final PrivateMessageModule module;

    public IgnoreCommand(CorePlugin plugin, PrivateMessageModule module) {
        super(plugin);
        this.module = module;
    }

    @Override
    protected void execute(CommandSender sender, String label, String[] args) {
        Player player = playerOnly(sender);
        User user = user(player);
        if (args.length != 1) {
            throw usage("pm.ignore-usage");
        }
        if (args[0].equalsIgnoreCase(LIST)) {
            list(player, user);
            return;
        }
        Player online = visiblePlayer(player, args[0]);
        if (online != null) {
            toggle(player, user, online.getUniqueId(), online.getName());
            return;
        }
        String name = args[0];
        UUID ownerId = player.getUniqueId();
        plugin.userManager().findByName(name).whenCompleteAsync((result, error) -> {
            Player owner = Bukkit.getPlayer(ownerId);
            User ownerUser = plugin.userManager().getUser(ownerId);
            if (owner == null || ownerUser == null) {
                return;
            }
            if (error != null) {
                plugin.getLogger().log(Level.SEVERE, "Could not look up player " + name, error);
                send(owner, "general.database-error");
                return;
            }
            if (result.isEmpty()) {
                send(owner, "general.player-not-found", placeholder("player", name));
                return;
            }
            try {
                toggle(owner, ownerUser, result.get().getKey(), result.get().getValue());
            } catch (CommandException e) {
                send(owner, e.messageKey(), e.resolvers());
            }
        }, plugin.mainThread());
    }

    private Player visiblePlayer(Player viewer, String name) {
        try {
            return player(viewer, name);
        } catch (CommandException e) {
            return null;
        }
    }

    private void toggle(Player owner, User user, UUID targetId, String targetName) {
        UUID ownerId = owner.getUniqueId();
        if (targetId.equals(ownerId)) {
            throw fail("pm.ignore-self");
        }
        TagResolver name = placeholder("player", targetName);
        if (user.isIgnoring(targetId)) {
            user.removeIgnored(targetId);
            send(owner, "pm.ignore-removed", name);
            persist(module.ignores().remove(ownerId, targetId), ownerId, current -> current.addIgnored(targetId));
            return;
        }
        Player target = Bukkit.getPlayer(targetId);
        if (target != null && target.hasPermission(ChatPermissions.IGNORE_EXEMPT)) {
            throw fail("pm.ignore-exempt", name);
        }
        int limit = module.settings().ignoreLimit();
        if (limit > 0 && user.getIgnored().size() >= limit) {
            throw fail("pm.ignore-limit", placeholder("limit", limit));
        }
        user.addIgnored(targetId);
        send(owner, "pm.ignore-added", name);
        persist(module.ignores().add(ownerId, targetId), ownerId, current -> current.removeIgnored(targetId));
    }

    private void persist(CompletableFuture<Void> write, UUID ownerId, Consumer<User> revert) {
        write.whenCompleteAsync((unused, error) -> {
            if (error == null) {
                return;
            }
            plugin.getLogger().log(Level.SEVERE, "Could not update core_ignores of " + ownerId, error);
            User current = plugin.userManager().getUser(ownerId);
            if (current != null) {
                revert.accept(current);
            }
            Player owner = Bukkit.getPlayer(ownerId);
            if (owner != null) {
                send(owner, "general.database-error");
            }
        }, plugin.mainThread());
    }

    private void list(Player player, User user) {
        Set<UUID> ids = Set.copyOf(user.getIgnored());
        if (ids.isEmpty()) {
            send(player, "pm.ignore-list-empty");
            return;
        }
        UUID ownerId = player.getUniqueId();
        module.ignores().names(ids).whenCompleteAsync((names, error) -> {
            Player owner = Bukkit.getPlayer(ownerId);
            if (owner == null) {
                return;
            }
            if (error != null) {
                plugin.getLogger().log(Level.SEVERE, "Could not load ignore list names of " + ownerId, error);
                send(owner, "general.database-error");
                return;
            }
            send(owner, "pm.ignore-list", placeholder("count", ids.size()),
                    Placeholder.component("players", entries(ids, names)));
        }, plugin.mainThread());
    }

    private Component entries(Set<UUID> ids, Map<UUID, String> names) {
        List<String> sorted = ids.stream()
                .map(id -> names.getOrDefault(id, id.toString()))
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
        List<Component> entries = new ArrayList<>(sorted.size());
        for (String name : sorted) {
            TagResolver player = placeholder("player", name);
            entries.add(messages().get("pm.ignore-list-entry", player)
                    .hoverEvent(HoverEvent.showText(messages().get("pm.ignore-list-hover", player)))
                    .clickEvent(ClickEvent.runCommand("/ignore " + name)));
        }
        return Component.join(JoinConfiguration.separator(messages().get("pm.ignore-list-separator")), entries);
    }

    @Override
    protected List<String> complete(CommandSender sender, String label, String[] args) {
        if (args.length != 1) {
            return List.of();
        }
        List<String> options = new ArrayList<>(filter(List.of(LIST), args[0]));
        for (String name : players(sender, args[0])) {
            if (!name.equals(sender.getName())) {
                options.add(name);
            }
        }
        return options;
    }
}
