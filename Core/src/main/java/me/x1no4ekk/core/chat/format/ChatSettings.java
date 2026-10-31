package me.x1no4ekk.core.chat.format;

import me.x1no4ekk.core.chat.ChatPermissions;
import me.x1no4ekk.core.chat.ChatYaml;
import me.x1no4ekk.core.config.ConfigUtil;
import me.x1no4ekk.core.hook.ChatHook;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * {@code chat} and {@code join-quit} sections of chat.yml.
 */
public record ChatSettings(
        boolean localChat,
        String globalSymbol,
        double localRadius,
        boolean notifyNoListeners,
        String globalFormat,
        String localFormat,
        String guildTagFormat,
        NameInteraction nameInteraction,
        boolean legacyColors,
        long cooldownMillis,
        long joinCooldownMillis,
        long minimalPlaytimeTicks,
        int clearLines,
        String consoleName,
        List<ChatGroup> groups,
        ChannelSettings donateChat,
        ChannelSettings staffChat,
        JoinQuitSettings joinQuit
) {

    public static ChatSettings load(ConfigurationSection root) {
        ConfigurationSection s = ConfigUtil.section(root, "chat");
        boolean localChat = s.getBoolean("local-chat", true);
        String symbol = s.getString("global-symbol", "!");
        double radius = ConfigUtil.nonNegativeDouble(s, "local-radius", 200);
        if (localChat && (symbol == null || symbol.isBlank())) {
            throw new IllegalArgumentException(ConfigUtil.path(s, "global-symbol") + " must not be empty when local-chat is enabled");
        }
        if (localChat && radius <= 0) {
            throw new IllegalArgumentException(ConfigUtil.path(s, "local-radius") + " must be positive when local-chat is enabled");
        }

        ConfigurationSection formats = ConfigUtil.section(s, "formats");
        List<ChatGroup> groups = new ArrayList<>();
        ConfigurationSection groupSection = ConfigUtil.sectionOrEmpty(s, "groups");
        for (String key : groupSection.getKeys(false)) {
            groups.add(ChatGroup.load(key, ConfigUtil.section(groupSection, key)));
        }

        return new ChatSettings(
                localChat,
                symbol == null ? "" : symbol.strip(),
                radius,
                s.getBoolean("notify-no-listeners", true),
                ConfigUtil.requireText(formats, "global"),
                ConfigUtil.requireText(formats, "local"),
                s.getString("guild-tag-format", "<tag> "),
                NameInteraction.load(s),
                s.getBoolean("legacy-colors", true),
                ChatYaml.secondsToMillis(s, "cooldown", 3),
                ChatYaml.secondsToMillis(s, "join-cooldown", 10),
                TimeUnit.SECONDS.toMillis(ConfigUtil.nonNegative(s, "minimal-playtime", 0)) / 50L,
                (int) Math.clamp(ConfigUtil.nonNegative(s, "clear-lines", 100), 1, 500),
                s.getString("console-name", "<red>Консоль"),
                List.copyOf(groups),
                ChannelSettings.load(s, "donate-chat"),
                ChannelSettings.load(s, "staff-chat"),
                JoinQuitSettings.load(root));
    }

    /**
     * The first group from the top whose name is the Vault primary group or whose permission the player has.
     */
    public ChatGroup groupOf(Player player, ChatHook hook) {
        if (groups.isEmpty()) {
            return ChatGroup.NONE;
        }
        String primary = hook.isAvailable() ? hook.getPrimaryGroup(player) : "";
        for (ChatGroup group : groups) {
            if (group.name().equals(primary) || player.hasPermission(ChatPermissions.GROUP_PREFIX + group.name())) {
                return group;
            }
        }
        return ChatGroup.NONE;
    }
}
