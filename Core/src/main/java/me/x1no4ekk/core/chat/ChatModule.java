package me.x1no4ekk.core.chat;

import me.x1no4ekk.core.CorePlugin;
import me.x1no4ekk.core.api.GuildTagProvider;
import me.x1no4ekk.core.chat.command.ChannelCommand;
import me.x1no4ekk.core.chat.command.ChatCommand;
import me.x1no4ekk.core.chat.command.ChatToggleCommand;
import me.x1no4ekk.core.chat.format.ChatFormatter;
import me.x1no4ekk.core.chat.format.ChatGroup;
import me.x1no4ekk.core.chat.format.ChatListener;
import me.x1no4ekk.core.chat.format.ChatPlaceholders;
import me.x1no4ekk.core.chat.format.ChatSettings;
import me.x1no4ekk.core.chat.format.JoinQuitListener;
import me.x1no4ekk.core.config.ConfigFiles;
import me.x1no4ekk.core.module.CoreModule;
import me.x1no4ekk.core.util.Text;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;

import java.util.UUID;

/**
 * Public chat (global/local, groups, cooldowns), join/quit messages, /chat, /chattoggle, /donatechat, /staffchat.
 */
public final class ChatModule implements CoreModule {

    private final CorePlugin plugin;
    private final ChatSessions sessions = new ChatSessions();
    private final CooldownMap chatCooldowns = new CooldownMap();
    private final CooldownMap donateCooldowns = new CooldownMap();
    private final CooldownMap staffCooldowns = new CooldownMap();

    private volatile ChatSettings settings;
    private volatile GuildTagProvider guildTags;
    private volatile boolean muted;

    public ChatModule(CorePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public String id() {
        return "chat";
    }

    @Override
    public Runnable loadConfig(ConfigFiles files) throws Exception {
        ChatSettings loaded = ChatSettings.load(files.get(ChatYaml.FILE));
        return () -> this.settings = loaded;
    }

    @Override
    public void enable() {
        refreshGuildTags();
        for (Player player : Bukkit.getOnlinePlayers()) {
            sessions.start(player, false);
        }
        PluginManager pluginManager = Bukkit.getPluginManager();
        pluginManager.registerEvents(new ChatListener(plugin, this), plugin);
        pluginManager.registerEvents(new JoinQuitListener(plugin, this), plugin);
        plugin.registerCommand("chat", new ChatCommand(plugin, this));
        plugin.registerCommand("chattoggle", new ChatToggleCommand(plugin));
        plugin.registerCommand("donatechat", new ChannelCommand(plugin, this, ChannelCommand.Channel.DONATE));
        plugin.registerCommand("staffchat", new ChannelCommand(plugin, this, ChannelCommand.Channel.STAFF));
    }

    @Override
    public void disable() {
        sessions.clear();
        chatCooldowns.clear();
        donateCooldowns.clear();
        staffCooldowns.clear();
        muted = false;
        guildTags = null;
    }

    public ChatSettings settings() {
        return settings;
    }

    public ChatSessions sessions() {
        return sessions;
    }

    public CooldownMap chatCooldowns() {
        return chatCooldowns;
    }

    public CooldownMap donateCooldowns() {
        return donateCooldowns;
    }

    public CooldownMap staffCooldowns() {
        return staffCooldowns;
    }

    public boolean isMuted() {
        return muted;
    }

    public void setMuted(boolean muted) {
        this.muted = muted;
    }

    /**
     * Guild provider of the Guilds plugin, or {@code null}.
     */
    public GuildTagProvider guildTags() {
        return guildTags;
    }

    public void refreshGuildTags() {
        this.guildTags = Bukkit.getServicesManager().load(GuildTagProvider.class);
    }

    /**
     * @return milliseconds of playtime the player still needs before using the chat, or 0
     */
    public long playtimeLeft(Player player) {
        long required = settings.minimalPlaytimeTicks();
        if (required <= 0L || player.hasPermission(ChatPermissions.BYPASS_PLAYTIME)) {
            return 0L;
        }
        return sessions.playtimeLeft(player.getUniqueId(), required);
    }

    /**
     * Renders a donate/staff chat line with the sender placeholders and the formatted message.
     */
    public Component renderChannel(CommandSender sender, String format, String text) {
        ChatSettings current = settings;
        if (sender instanceof Player player) {
            ChatGroup group = current.groupOf(player, plugin.hooks().chat());
            Component message = ChatFormatter.style(group.messageColor(),
                    ChatFormatter.playerText(player, text, current.legacyColors()));
            return Text.parse(ChatFormatter.template(player, format),
                    ChatPlaceholders.player(current, plugin.hooks().chat(), guildTags, player, group, true),
                    Placeholder.component("message", message));
        }
        return Text.parse(Text.legacyToMiniMessage(format), ChatPlaceholders.console(current),
                Placeholder.component("message", ChatFormatter.playerText(sender, text, current.legacyColors())));
    }

    /**
     * Drops the per-player state of a player who left.
     */
    public void forget(UUID uuid) {
        sessions.end(uuid);
        chatCooldowns.remove(uuid);
        donateCooldowns.remove(uuid);
        staffCooldowns.remove(uuid);
    }
}
