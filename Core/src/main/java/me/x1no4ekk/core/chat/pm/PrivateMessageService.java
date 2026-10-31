package me.x1no4ekk.core.chat.pm;

import me.x1no4ekk.core.CorePlugin;
import me.x1no4ekk.core.chat.ChatModule;
import me.x1no4ekk.core.chat.ChatPermissions;
import me.x1no4ekk.core.chat.CooldownMap;
import me.x1no4ekk.core.chat.PrivateMessageModule;
import me.x1no4ekk.core.chat.format.ChatFormatter;
import me.x1no4ekk.core.command.CommandException;
import me.x1no4ekk.core.hook.ChatHook;
import me.x1no4ekk.core.user.User;
import me.x1no4ekk.core.user.UserManager;
import me.x1no4ekk.core.util.Durations;
import me.x1no4ekk.core.util.Text;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Delivers private messages: checks, formats, social spy, reply targets and the cooldown. Main thread only.
 */
public final class PrivateMessageService {

    private final CorePlugin plugin;
    private final PrivateMessageModule module;
    /** Last conversation partner of each online player, for /reply. */
    private final Map<UUID, UUID> replies = new HashMap<>();
    private final CooldownMap cooldowns = new CooldownMap();

    public PrivateMessageService(CorePlugin plugin, PrivateMessageModule module) {
        this.plugin = plugin;
        this.module = module;
    }

    /**
     * Sends {@code text} from one player to another.
     *
     * @throws CommandException with the reason if the message may not be sent
     */
    public void send(Player from, Player to, String text) {
        if (from.equals(to)) {
            throw new CommandException("pm.self");
        }
        PrivateMessageSettings settings = module.settings();
        UserManager users = plugin.userManager();
        if (users.getUser(from) == null) {
            throw new CommandException("general.data-not-loaded");
        }
        User target = users.getUser(to);
        TagResolver targetName = Placeholder.unparsed("player", to.getName());
        if (target == null) {
            throw new CommandException("general.player-not-found", targetName);
        }
        UUID fromId = from.getUniqueId();
        UUID toId = to.getUniqueId();
        // Answering someone who wrote to you is allowed even if they disabled private messages.
        boolean answering = fromId.equals(replies.get(toId));
        if (!target.isMsgToggle() && !answering && !from.hasPermission(ChatPermissions.MSG_BYPASS_TOGGLE)) {
            throw new CommandException("pm.disabled", targetName);
        }
        if (target.isIgnoring(fromId) && !from.hasPermission(ChatPermissions.IGNORE_EXEMPT)) {
            throw new CommandException("pm.ignored", targetName);
        }
        ChatModule chat = plugin.module(ChatModule.class);
        long playtimeLeft = chat == null ? 0L : chat.playtimeLeft(from);
        if (playtimeLeft > 0L) {
            throw new CommandException("chat.minimal-playtime", time(playtimeLeft));
        }
        boolean bypassCooldown = from.hasPermission(ChatPermissions.MSG_BYPASS_COOLDOWN);
        long cooldownLeft = bypassCooldown ? 0L : cooldowns.remaining(fromId);
        if (cooldownLeft > 0L) {
            throw new CommandException("pm.cooldown", time(cooldownLeft));
        }

        ChatHook hook = plugin.hooks().chat();
        TagResolver placeholders = TagResolver.resolver(
                Placeholder.component("sender", from.displayName()),
                Placeholder.unparsed("sender_name", from.getName()),
                Placeholder.component("sender_prefix", ChatFormatter.prefix(hook, from)),
                Placeholder.component("target", to.displayName()),
                Placeholder.unparsed("target_name", to.getName()),
                Placeholder.component("target_prefix", ChatFormatter.prefix(hook, to)),
                Placeholder.component("message", ChatFormatter.playerText(from, text, settings.legacyColors())));

        from.sendMessage(line(settings.senderFormat(), from, placeholders, settings.senderHover(),
                settings.clickToReply() ? to.getName() : null));
        to.sendMessage(line(settings.receiverFormat(), from, placeholders, settings.receiverHover(),
                settings.clickToReply() ? from.getName() : null));
        settings.sound().play(to);

        Component spyLine = Text.parse(ChatFormatter.template(from, settings.socialSpyFormat()), placeholders);
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online.equals(from) || online.equals(to)) {
                continue;
            }
            User user = users.getUser(online);
            if (user != null && user.isSocialSpy() && online.hasPermission(ChatPermissions.SOCIAL_SPY)) {
                online.sendMessage(spyLine);
            }
        }
        if (settings.logToConsole()) {
            Bukkit.getConsoleSender().sendMessage(spyLine);
        }

        replies.put(fromId, toId);
        replies.put(toId, fromId);
        if (!bypassCooldown) {
            cooldowns.start(fromId, settings.cooldownMillis());
        }
    }

    /**
     * PlaceholderAPI placeholders in PM formats are resolved for the sender.
     */
    private static Component line(String format, Player sender, TagResolver placeholders, String hover, String replyTo) {
        Component line = Text.parse(ChatFormatter.template(sender, format), placeholders);
        if (!hover.isEmpty()) {
            line = line.hoverEvent(HoverEvent.showText(Text.parse(hover, placeholders)));
        }
        if (replyTo != null) {
            line = line.clickEvent(ClickEvent.suggestCommand("/msg " + replyTo + " "));
        }
        return line;
    }

    private static TagResolver time(long millis) {
        return Placeholder.unparsed("time", Durations.format(millis));
    }

    /**
     * @return the last conversation partner, or {@code null}
     */
    public UUID replyTarget(UUID uuid) {
        return replies.get(uuid);
    }

    /**
     * Forgets a player who left, including reply entries pointing to them.
     */
    public void quit(UUID uuid) {
        replies.remove(uuid);
        replies.values().removeIf(uuid::equals);
        cooldowns.remove(uuid);
    }

    public void clear() {
        replies.clear();
        cooldowns.clear();
    }
}
