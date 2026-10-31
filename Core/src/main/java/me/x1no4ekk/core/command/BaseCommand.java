package me.x1no4ekk.core.command;

import me.x1no4ekk.core.CorePlugin;
import me.x1no4ekk.core.config.Messages;
import me.x1no4ekk.core.user.User;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.stream.Stream;

/**
 * Base executor with flow-control helpers. Helpers throw {@link CommandException}, which is turned into a
 * message, so command bodies stay linear:
 * <pre>
 * Player player = playerOnly(sender);
 * Player target = args.length &gt; 0 ? otherPlayer(sender, args[0], "core.fly.others") : player;
 * </pre>
 * Executors are stateless singletons: never store players or per-call data in fields.
 */
public abstract class BaseCommand implements TabExecutor {

    protected final CorePlugin plugin;

    protected BaseCommand(CorePlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Command body. {@code label} is the alias that was typed.
     */
    protected abstract void execute(CommandSender sender, String label, String[] args);

    /**
     * Tab completion; by default nothing.
     */
    protected List<String> complete(CommandSender sender, String label, String[] args) {
        return List.of();
    }

    @Override
    public final boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        try {
            execute(sender, label, args);
        } catch (CommandException e) {
            messages().send(sender, e.messageKey(), e.resolvers());
        } catch (RuntimeException e) {
            plugin.getLogger().log(Level.SEVERE, "Error while executing /" + label, e);
            messages().send(sender, "general.command-error");
        }
        return true;
    }

    @Override
    public final List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        try {
            List<String> result = complete(sender, alias, args);
            return result == null ? List.of() : result;
        } catch (CommandException e) {
            return List.of();
        }
    }

    // ---------------------------------------------------------------- messages

    protected Messages messages() {
        return plugin.messages();
    }

    protected void send(CommandSender sender, String key, TagResolver... resolvers) {
        messages().send(sender, key, resolvers);
    }

    /**
     * Aborts the command with the given message.
     */
    protected static CommandException fail(String key, TagResolver... resolvers) {
        return new CommandException(key, resolvers);
    }

    protected static TagResolver placeholder(String key, Object value) {
        return Placeholder.unparsed(key, String.valueOf(value));
    }

    // ---------------------------------------------------------------- senders & permissions

    /**
     * @return the sender as a player, or aborts with {@code general.players-only}
     */
    protected Player playerOnly(CommandSender sender) {
        if (sender instanceof Player player) {
            return player;
        }
        throw fail("general.players-only");
    }

    protected void requirePermission(CommandSender sender, String permission) {
        if (!sender.hasPermission(permission)) {
            throw fail("general.no-permission");
        }
    }

    /**
     * Cached data of an online player, or aborts with {@code general.data-not-loaded}.
     */
    protected User user(Player player) {
        User user = plugin.userManager().getUser(player);
        if (user == null) {
            throw fail("general.data-not-loaded");
        }
        return user;
    }

    /**
     * Aborts with the usage message (a list in messages.yml).
     */
    protected static CommandException usage(String key) {
        return fail(key);
    }

    // ---------------------------------------------------------------- players

    /**
     * Whether {@code viewer} may see {@code target} (vanished players are hidden via {@code Player#hidePlayer}).
     * The console sees everyone.
     */
    protected static boolean canSee(CommandSender viewer, Player target) {
        return !(viewer instanceof Player player) || player.equals(target) || player.canSee(target);
    }

    /**
     * Finds an online player visible to {@code viewer}: exact name first, then a unique name prefix.
     *
     * @return the player, or aborts with {@code general.player-not-found}
     */
    protected Player player(CommandSender viewer, String name) {
        Player exact = Bukkit.getPlayerExact(name);
        if (exact != null && canSee(viewer, exact)) {
            return exact;
        }
        String prefix = name.toLowerCase(Locale.ROOT);
        Player match = null;
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online.getName().toLowerCase(Locale.ROOT).startsWith(prefix) && canSee(viewer, online)) {
                if (match != null) {
                    match = null; // ambiguous
                    break;
                }
                match = online;
            }
        }
        if (match == null) {
            throw fail("general.player-not-found", placeholder("player", name));
        }
        return match;
    }

    /**
     * Resolves a target player; if it is not the sender, {@code othersPermission} is required.
     */
    protected Player otherPlayer(CommandSender sender, String name, String othersPermission) {
        Player target = player(sender, name);
        if (!target.equals(sender)) {
            requirePermission(sender, othersPermission);
        }
        return target;
    }

    // ---------------------------------------------------------------- numbers

    protected static int parseInt(String raw, int min, int max) {
        int value;
        try {
            value = Integer.parseInt(raw);
        } catch (NumberFormatException e) {
            throw fail("general.invalid-number", placeholder("value", raw));
        }
        if (value < min || value > max) {
            throw fail("general.number-range", placeholder("value", raw), placeholder("min", min), placeholder("max", max));
        }
        return value;
    }

    protected static double parseDouble(String raw, double min, double max) {
        double value;
        try {
            value = Double.parseDouble(raw.replace(',', '.'));
        } catch (NumberFormatException e) {
            throw fail("general.invalid-number", placeholder("value", raw));
        }
        if (Double.isNaN(value) || Double.isInfinite(value) || value < min || value > max) {
            throw fail("general.number-range", placeholder("value", raw),
                    placeholder("min", trim(min)), placeholder("max", trim(max)));
        }
        return value;
    }

    private static String trim(double value) {
        return value == Math.rint(value) ? String.valueOf((long) value) : String.valueOf(value);
    }

    // ---------------------------------------------------------------- tab completion

    /**
     * Names of online players visible to {@code viewer} that start with {@code input}.
     */
    protected static List<String> players(CommandSender viewer, String input) {
        String prefix = input.toLowerCase(Locale.ROOT);
        List<String> result = new ArrayList<>();
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (canSee(viewer, online) && online.getName().toLowerCase(Locale.ROOT).startsWith(prefix)) {
                result.add(online.getName());
            }
        }
        return result;
    }

    protected static List<String> filter(Collection<String> options, String input) {
        return filter(options.stream(), input);
    }

    protected static List<String> filter(Stream<String> options, String input) {
        String prefix = input.toLowerCase(Locale.ROOT);
        return options.filter(option -> option.toLowerCase(Locale.ROOT).startsWith(prefix)).toList();
    }
}
