package me.x1no4ekk.core.chat.format;

import me.x1no4ekk.core.chat.ChatPermissions;
import me.x1no4ekk.core.hook.ChatHook;
import me.x1no4ekk.core.hook.PlaceholderHook;
import me.x1no4ekk.core.util.Text;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Text helpers shared by chat, private messages and broadcasts. Safe to call from the async chat thread.
 */
public final class ChatFormatter {

    /**
     * Styling tags only: no click, hover, insertion, keybind, selector, nbt, translatable or newline tags.
     */
    private static final MiniMessage SAFE = MiniMessage.builder()
            .tags(TagResolver.resolver(
                    StandardTags.color(),
                    StandardTags.decorations(),
                    StandardTags.gradient(),
                    StandardTags.rainbow(),
                    StandardTags.transition(),
                    StandardTags.reset()))
            .build();

    private ChatFormatter() {
    }

    /**
     * Text typed by a player: plain text, or safe styling (and optionally {@code &} codes) with {@code core.chat.color}.
     */
    public static Component playerText(CommandSender sender, String raw, boolean legacyCodes) {
        if (!sender.hasPermission(ChatPermissions.COLOR)) {
            return Component.text(raw);
        }
        return SAFE.deserialize(legacyCodes ? Text.legacyToMiniMessage(raw) : raw);
    }

    /**
     * Parses third-party MiniMessage (e.g. guild tags) allowing styling tags only.
     */
    public static Component safe(String input) {
        return input == null || input.isEmpty() ? Component.empty() : SAFE.deserialize(input);
    }

    public static Component prefix(ChatHook hook, Player player) {
        return hook.isAvailable() ? Text.parse(Text.legacyToMiniMessage(hook.getPrefix(player))) : Component.empty();
    }

    public static Component suffix(ChatHook hook, Player player) {
        return hook.isAvailable() ? Text.parse(Text.legacyToMiniMessage(hook.getSuffix(player))) : Component.empty();
    }

    /**
     * Applies PlaceholderAPI to an administrator template (never to player text) and converts legacy codes.
     */
    public static String template(OfflinePlayer player, String template) {
        return Text.legacyToMiniMessage(PlaceholderHook.apply(player, template));
    }

    /**
     * Wraps a component in opening style tags such as {@code <#ffaa00>} or {@code <gradient:#ff0000:#00ff00>}.
     */
    public static Component style(String openingTags, Component content) {
        if (openingTags == null || openingTags.isEmpty()) {
            return content;
        }
        return Text.parse(openingTags + "<content>", Placeholder.component("content", content));
    }
}
