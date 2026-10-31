package me.x1no4ekk.core.chat.broadcast;

import me.x1no4ekk.core.CorePlugin;
import me.x1no4ekk.core.chat.BroadcastModule;
import me.x1no4ekk.core.chat.ChatPermissions;
import me.x1no4ekk.core.chat.CooldownMap;
import me.x1no4ekk.core.chat.format.ChatFormatter;
import me.x1no4ekk.core.command.BaseCommand;
import me.x1no4ekk.core.hook.ChatHook;
import me.x1no4ekk.core.hook.EconomyHook;
import me.x1no4ekk.core.user.User;
import me.x1no4ekk.core.util.Durations;
import me.x1no4ekk.core.util.Text;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * /broadcast, /adbroadcast, /buybroadcast, /sellbroadcast, /streambroadcast, /videobroadcast &lt;message&gt;:
 * a server-wide announcement with its own format, permission, cooldown and price.
 */
public final class BroadcastCommand extends BaseCommand {

    private final BroadcastModule module;
    private final String typeId;

    public BroadcastCommand(CorePlugin plugin, BroadcastModule module, String typeId) {
        super(plugin);
        this.module = module;
        this.typeId = typeId;
    }

    @Override
    protected void execute(CommandSender sender, String label, String[] args) {
        BroadcastSettings settings = module.settings();
        BroadcastType type = settings.types().get(typeId);
        requirePermission(sender, type.permission());
        String text = String.join(" ", args).strip();
        if (text.isEmpty()) {
            throw usage(type.usageKey());
        }
        Component message = message(sender, type, settings, args, text);

        Player player = sender instanceof Player p ? p : null;
        CooldownMap cooldowns = module.cooldowns(typeId);
        boolean bypassCooldown = player == null || player.hasPermission(ChatPermissions.BROADCAST_BYPASS_COOLDOWN);
        long left = bypassCooldown ? 0L : cooldowns.remaining(player.getUniqueId());
        if (left > 0L) {
            throw fail("general.cooldown", placeholder("time", Durations.format(left)));
        }
        if (player != null && type.price() > 0 && !player.hasPermission(ChatPermissions.BROADCAST_BYPASS_PRICE)) {
            charge(player, type.price());
        }

        deliver(sender, player, render(sender, player, type, settings, message), type);
        if (player != null && !bypassCooldown) {
            cooldowns.start(player.getUniqueId(), type.cooldownMillis());
        }
    }

    private Component message(CommandSender sender, BroadcastType type, BroadcastSettings settings, String[] args, String text) {
        if (!type.linkOnly()) {
            return ChatFormatter.playerText(sender, text, settings.legacyColors());
        }
        String url = args.length == 1 ? link(args[0], type.allowedLinks()) : null;
        if (url == null) {
            throw fail("broadcast.invalid-link", placeholder("domains", String.join(", ", type.allowedLinks())));
        }
        return Component.text(url)
                .clickEvent(ClickEvent.openUrl(url))
                .hoverEvent(HoverEvent.showText(messages().get("broadcast.link-hover")));
    }

    private void charge(Player player, double price) {
        EconomyHook economy = plugin.hooks().economy();
        if (!economy.isAvailable()) {
            throw fail("general.economy-unavailable");
        }
        TagResolver formatted = placeholder("price", economy.format(price));
        if (!economy.has(player, price) || !economy.withdraw(player, price)) {
            throw fail("general.not-enough-money", formatted);
        }
        send(player, "broadcast.paid", formatted);
    }

    private Component render(CommandSender sender, Player player, BroadcastType type, BroadcastSettings settings, Component message) {
        TagResolver content = Placeholder.component("message", message);
        if (player == null) {
            return Text.parse(Text.legacyToMiniMessage(type.format()), content,
                    Placeholder.component("player", Text.parse(settings.consoleName())),
                    Placeholder.unparsed("name", sender.getName()),
                    Placeholder.component("prefix", Component.empty()),
                    Placeholder.component("suffix", Component.empty()));
        }
        ChatHook hook = plugin.hooks().chat();
        return Text.parse(ChatFormatter.template(player, type.format()), content,
                Placeholder.component("player", player.displayName()),
                Placeholder.unparsed("name", player.getName()),
                Placeholder.component("prefix", ChatFormatter.prefix(hook, player)),
                Placeholder.component("suffix", ChatFormatter.suffix(hook, player)));
    }

    /**
     * Everyone receives the broadcast except players ignoring the sender (unless the sender is exempt).
     */
    private void deliver(CommandSender sender, Player player, Component line, BroadcastType type) {
        UUID senderId = player == null ? null : player.getUniqueId();
        boolean exempt = sender.hasPermission(ChatPermissions.IGNORE_EXEMPT);
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (senderId != null && !exempt && !online.equals(player)) {
                User user = plugin.userManager().getUser(online);
                if (user != null && user.isIgnoring(senderId)) {
                    continue;
                }
            }
            online.sendMessage(line);
            type.sound().play(online);
        }
        Bukkit.getConsoleSender().sendMessage(line);
    }

    /**
     * @return the normalised http(s) link if its host is one of the allowed domains, otherwise {@code null}
     */
    static String link(String raw, List<String> allowedDomains) {
        String candidate = raw.contains("://") ? raw : "https://" + raw;
        URI uri;
        try {
            uri = new URI(candidate);
        } catch (URISyntaxException e) {
            return null;
        }
        String scheme = uri.getScheme();
        String host = uri.getHost();
        if (scheme == null || host == null
                || !(scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https"))) {
            return null;
        }
        String lowerHost = host.toLowerCase(Locale.ROOT);
        for (String domain : allowedDomains) {
            if (lowerHost.equals(domain) || lowerHost.endsWith("." + domain)) {
                return uri.toString();
            }
        }
        return null;
    }

    @Override
    protected List<String> complete(CommandSender sender, String label, String[] args) {
        BroadcastType type = module.settings().types().get(typeId);
        if (args.length == 0 || type.linkOnly() || !sender.hasPermission(type.permission())) {
            return List.of();
        }
        return players(sender, args[args.length - 1]);
    }
}
