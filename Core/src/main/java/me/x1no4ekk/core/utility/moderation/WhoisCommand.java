package me.x1no4ekk.core.utility.moderation;

import me.x1no4ekk.core.CorePlugin;
import me.x1no4ekk.core.command.BaseCommand;
import me.x1no4ekk.core.hook.ChatHook;
import me.x1no4ekk.core.user.User;
import me.x1no4ekk.core.util.Numbers;
import me.x1no4ekk.core.utility.SenderRef;
import me.x1no4ekk.core.utility.UtilityText;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.net.InetSocketAddress;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

/**
 * {@code /whois <player>}: details of an online player, or the stored data of an offline one.
 * Players hidden from the sender by vanish are treated as offline.
 */
final class WhoisCommand extends BaseCommand {

    private static final String SELECT_OFFLINE = """
            SELECT u.name, u.nickname, u.fly, u.god, u.vanish, u.last_location, u.first_join, u.last_join,
                   s.kills, s.deaths, s.mob_kills
            FROM core_users u
            LEFT JOIN core_stats s ON s.uuid = u.uuid
            WHERE u.uuid = ?""";

    private record OfflineInfo(UUID uuid, String name, String nickname, boolean fly, boolean god, boolean vanish,
                               String lastLocation, long firstJoin, long lastJoin, int kills, int deaths, int mobKills) {
    }

    private final ModerationModule module;

    WhoisCommand(CorePlugin plugin, ModerationModule module) {
        super(plugin);
        this.module = module;
    }

    @Override
    protected void execute(CommandSender sender, String label, String[] args) {
        requirePermission(sender, "core.whois");
        if (args.length != 1) {
            throw usage("utility.whois.usage");
        }
        // Exact names only: a prefix match on an online player could hide an offline player with that exact name.
        Player online = Bukkit.getPlayerExact(args[0]);
        if (online != null && canSee(sender, online)) {
            showOnline(sender, online);
        } else {
            lookupOffline(sender, args[0]);
        }
    }

    private void showOnline(CommandSender sender, Player target) {
        User user = plugin.userManager().getUser(target);
        DateTimeFormatter dates = module.settings().whoisDates();
        Location location = target.getLocation();
        AttributeInstance maxHealth = target.getAttribute(Attribute.MAX_HEALTH);
        Component ip = sender.hasPermission("core.whois.ip")
                ? Component.text(address(target))
                : messages().get("utility.whois.ip-hidden");
        send(sender, "utility.whois.online", TagResolver.resolver(
                placeholder("player", target.getName()),
                Placeholder.component("display", target.displayName()),
                placeholder("uuid", target.getUniqueId()),
                Placeholder.component("ip", ip),
                placeholder("group", group(target)),
                Placeholder.component("gamemode", UtilityText.gameMode(messages(), target.getGameMode())),
                placeholder("health", Numbers.format(Math.ceil(target.getHealth() * 10.0) / 10.0)),
                placeholder("max-health", Numbers.format(maxHealth != null ? maxHealth.getValue() : 20.0)),
                placeholder("food", target.getFoodLevel()),
                placeholder("world", location.getWorld().getName()),
                placeholder("x", location.getBlockX()),
                placeholder("y", location.getBlockY()),
                placeholder("z", location.getBlockZ()),
                flag("fly", user != null && user.isFly()),
                flag("god", user != null && user.isGod()),
                flag("vanish", user != null && user.isVanish()),
                flag("op", target.isOp()),
                placeholder("first-join", user != null ? date(dates, user.getFirstJoin()) : "—"),
                placeholder("last-join", user != null ? date(dates, user.getLastJoin()) : "—"),
                placeholder("ping", target.getPing()),
                placeholder("kills", user != null ? user.getKills() : 0),
                placeholder("deaths", user != null ? user.getDeaths() : 0),
                placeholder("mob-kills", user != null ? user.getMobKills() : 0)));
    }

    private void lookupOffline(CommandSender sender, String name) {
        SenderRef senderRef = SenderRef.of(sender);
        plugin.userManager().findByName(name)
                .thenCompose(found -> found.isEmpty()
                        ? CompletableFuture.<OfflineInfo>completedFuture(null)
                        : plugin.database().query(connection -> load(connection, found.get().getKey())))
                .whenCompleteAsync((info, error) -> {
                    if (error != null) {
                        plugin.getLogger().log(Level.SEVERE, "Could not load /whois data of " + name, error);
                    }
                    CommandSender viewer = senderRef.resolve();
                    if (viewer == null) {
                        return;
                    }
                    if (error != null) {
                        send(viewer, "general.database-error");
                    } else if (info == null) {
                        send(viewer, "general.player-not-found", placeholder("player", name));
                    } else {
                        showOffline(viewer, info);
                    }
                }, plugin.mainThread());
    }

    private static OfflineInfo load(Connection connection, UUID uuid) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(SELECT_OFFLINE)) {
            statement.setString(1, uuid.toString());
            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return new OfflineInfo(uuid, rs.getString("name"), rs.getString("nickname"),
                        rs.getBoolean("fly"), rs.getBoolean("god"), rs.getBoolean("vanish"),
                        rs.getString("last_location"), rs.getLong("first_join"), rs.getLong("last_join"),
                        rs.getInt("kills"), rs.getInt("deaths"), rs.getInt("mob_kills"));
            }
        }
    }

    private void showOffline(CommandSender sender, OfflineInfo info) {
        DateTimeFormatter dates = module.settings().whoisDates();
        Component display = info.nickname() != null && !info.nickname().isBlank()
                ? UtilityText.styled(info.nickname())
                : Component.text(info.name());
        send(sender, "utility.whois.offline", TagResolver.resolver(
                placeholder("player", info.name()),
                Placeholder.component("display", display),
                placeholder("uuid", info.uuid()),
                placeholder("first-join", date(dates, info.firstJoin())),
                placeholder("last-join", date(dates, info.lastJoin())),
                Placeholder.component("location", storedLocation(info.lastLocation())),
                flag("fly", info.fly()),
                flag("god", info.god()),
                flag("vanish", info.vanish()),
                placeholder("kills", info.kills()),
                placeholder("deaths", info.deaths()),
                placeholder("mob-kills", info.mobKills())));
    }

    /**
     * {@code world;x;y;z;yaw;pitch} as "world x y z", without requiring the world to be loaded.
     */
    private Component storedLocation(String serialized) {
        if (serialized != null) {
            String[] parts = serialized.split(";");
            if (parts.length >= 4) {
                try {
                    return Component.text(parts[0] + " " + (long) Math.floor(Double.parseDouble(parts[1]))
                            + " " + (long) Math.floor(Double.parseDouble(parts[2]))
                            + " " + (long) Math.floor(Double.parseDouble(parts[3])));
                } catch (NumberFormatException ignored) {
                    // malformed value: shown as missing
                }
            }
        }
        return messages().get("utility.whois.no-location");
    }

    private TagResolver flag(String key, boolean value) {
        return Placeholder.component(key, messages().get(value ? "utility.whois.yes" : "utility.whois.no"));
    }

    private String group(Player player) {
        ChatHook chat = plugin.hooks().chat();
        String group = chat.isAvailable() ? chat.getPrimaryGroup(player) : "";
        return group == null || group.isEmpty() ? "—" : group;
    }

    private static String address(Player player) {
        InetSocketAddress address = player.getAddress();
        if (address == null) {
            return "—";
        }
        return address.getAddress() != null ? address.getAddress().getHostAddress() : address.getHostString();
    }

    private static String date(DateTimeFormatter formatter, long millis) {
        return millis <= 0 ? "—" : formatter.format(Instant.ofEpochMilli(millis));
    }

    @Override
    protected List<String> complete(CommandSender sender, String label, String[] args) {
        return args.length == 1 ? players(sender, args[0]) : List.of();
    }
}
