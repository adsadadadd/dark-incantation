package me.x1no4ekk.core.chat.format;

import io.papermc.paper.chat.ChatRenderer;
import io.papermc.paper.event.player.AsyncChatEvent;
import me.x1no4ekk.core.CorePlugin;
import me.x1no4ekk.core.api.GuildTagProvider;
import me.x1no4ekk.core.chat.ChatModule;
import me.x1no4ekk.core.chat.ChatPermissions;
import me.x1no4ekk.core.user.User;
import me.x1no4ekk.core.user.UserManager;
import me.x1no4ekk.core.util.Durations;
import me.x1no4ekk.core.util.Text;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServiceRegisterEvent;
import org.bukkit.event.server.ServiceUnregisterEvent;

import java.util.UUID;

/**
 * Public chat: checks, global/local channel, format and viewer filtering. Runs on the async chat thread and only
 * reads thread-safe state (volatile settings, {@link User} fields, concurrent maps); the chat statistic is sampled
 * at join by {@link me.x1no4ekk.core.chat.ChatSessions}.
 */
public final class ChatListener implements Listener {

    private final CorePlugin plugin;
    private final ChatModule module;

    public ChatListener(CorePlugin plugin, ChatModule module) {
        this.plugin = plugin;
        this.module = module;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onChat(AsyncChatEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        ChatSettings settings = module.settings();
        if (plugin.userManager().getUser(uuid) == null) {
            deny(event, player, "general.data-not-loaded");
            return;
        }
        String raw = Text.plain(event.message()).strip();
        if (raw.isEmpty()) {
            event.setCancelled(true);
            return;
        }
        if (module.isMuted() && !player.hasPermission(ChatPermissions.MUTE_BYPASS)) {
            deny(event, player, "chat.muted");
            return;
        }
        boolean bypassCooldown = player.hasPermission(ChatPermissions.BYPASS_COOLDOWN);
        long joinLeft = bypassCooldown ? 0L : module.sessions().joinCooldownLeft(uuid, settings.joinCooldownMillis());
        if (joinLeft > 0L) {
            deny(event, player, "chat.join-cooldown", time(joinLeft));
            return;
        }
        long playtimeLeft = module.playtimeLeft(player);
        if (playtimeLeft > 0L) {
            deny(event, player, "chat.minimal-playtime", time(playtimeLeft));
            return;
        }
        long cooldownLeft = bypassCooldown ? 0L : module.chatCooldowns().remaining(uuid);
        if (cooldownLeft > 0L) {
            deny(event, player, "chat.cooldown", time(cooldownLeft));
            return;
        }

        boolean global = !settings.localChat();
        String text = raw;
        if (settings.localChat() && raw.startsWith(settings.globalSymbol())) {
            global = true;
            text = raw.substring(settings.globalSymbol().length()).strip();
            if (text.isEmpty()) {
                deny(event, player, "chat.empty-global", Placeholder.unparsed("symbol", settings.globalSymbol()));
                return;
            }
        }

        ChatGroup group = settings.groupOf(player, plugin.hooks().chat());
        Component message = ChatFormatter.style(group.messageColor(),
                ChatFormatter.playerText(player, text, settings.legacyColors()));
        TagResolver placeholders = ChatPlaceholders.player(settings, plugin.hooks().chat(), module.guildTags(),
                player, group, true);
        String template = ChatFormatter.template(player, global ? settings.globalFormat() : settings.localFormat());

        event.message(message);
        event.renderer(ChatRenderer.viewerUnaware((source, displayName, rendered) ->
                Text.parse(template, placeholders, Placeholder.component("message", rendered))));
        filterViewers(event, player, settings, global);
        if (!bypassCooldown) {
            module.chatCooldowns().start(uuid, settings.cooldownMillis());
        }
        if (!global && settings.notifyNoListeners() && nobodyHears(event, player)) {
            // Deferred so the hint appears after the player's own message.
            Bukkit.getScheduler().runTask(plugin, () -> {
                Player online = Bukkit.getPlayer(uuid);
                if (online != null) {
                    plugin.messages().send(online, "chat.no-one-heard", Placeholder.unparsed("symbol", module.settings().globalSymbol()));
                }
            });
        }
    }

    private void filterViewers(AsyncChatEvent event, Player sender, ChatSettings settings, boolean global) {
        UUID senderId = sender.getUniqueId();
        boolean exempt = sender.hasPermission(ChatPermissions.IGNORE_EXEMPT);
        World world = sender.getWorld();
        Location origin = sender.getLocation();
        double radiusSquared = settings.localRadius() * settings.localRadius();
        UserManager users = plugin.userManager();
        event.viewers().removeIf(audience -> {
            if (!(audience instanceof Player viewer) || viewer.getUniqueId().equals(senderId)) {
                return false; // the console and the sender always see the message
            }
            User user = users.getUser(viewer.getUniqueId());
            if (user != null && (!user.isChatToggle() || (!exempt && user.isIgnoring(senderId)))) {
                return true;
            }
            return !global && (!viewer.getWorld().equals(world) || viewer.getLocation().distanceSquared(origin) > radiusSquared);
        });
    }

    private static boolean nobodyHears(AsyncChatEvent event, Player sender) {
        for (Audience audience : event.viewers()) {
            if (audience instanceof Player viewer && !viewer.getUniqueId().equals(sender.getUniqueId()) && sender.canSee(viewer)) {
                return false;
            }
        }
        return true;
    }

    private void deny(AsyncChatEvent event, Player player, String key, TagResolver... resolvers) {
        event.setCancelled(true);
        plugin.messages().send(player, key, resolvers);
    }

    private static TagResolver time(long millis) {
        return Placeholder.unparsed("time", Durations.format(millis));
    }

    @EventHandler
    public void onServiceRegister(ServiceRegisterEvent event) {
        if (event.getProvider().getService() == GuildTagProvider.class) {
            module.refreshGuildTags();
        }
    }

    @EventHandler
    public void onServiceUnregister(ServiceUnregisterEvent event) {
        if (event.getProvider().getService() == GuildTagProvider.class) {
            module.refreshGuildTags();
        }
    }
}
