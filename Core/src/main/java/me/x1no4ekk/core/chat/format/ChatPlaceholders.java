package me.x1no4ekk.core.chat.format;

import me.x1no4ekk.core.api.GuildTagProvider;
import me.x1no4ekk.core.hook.ChatHook;
import me.x1no4ekk.core.hook.PlaceholderHook;
import me.x1no4ekk.core.util.Text;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Sender placeholders shared by every chat format: {@code <prefix> <suffix> <player> <name> <world> <guild_tag>
 * <guild_name> <additional_suffix>}. Safe to build on the async chat thread.
 */
public final class ChatPlaceholders {

    private ChatPlaceholders() {
    }

    /**
     * @param guilds          guild provider, or {@code null} if no Guilds plugin is installed
     * @param interactiveName whether the name gets the configured hover text and click action
     */
    public static TagResolver player(ChatSettings settings, ChatHook hook, GuildTagProvider guilds, Player player,
                                     ChatGroup group, boolean interactiveName) {
        UUID uuid = player.getUniqueId();
        String guildTag = guilds == null ? null : guilds.getGuildTag(uuid);
        String guildName = guilds == null ? null : guilds.getGuildName(uuid);
        String plainGuildName = guildName == null ? "" : guildName;
        Component guildComponent = guildTag == null || guildTag.isEmpty() ? Component.empty()
                : Text.parse(settings.guildTagFormat(),
                Placeholder.component("tag", ChatFormatter.safe(guildTag)),
                Placeholder.unparsed("guild_name", plainGuildName));
        Component prefix = group.prefix() != null ? Text.parse(group.prefix()) : ChatFormatter.prefix(hook, player);

        TagResolver common = TagResolver.resolver(
                Placeholder.unparsed("name", player.getName()),
                Placeholder.unparsed("world", player.getWorld().getName()),
                Placeholder.unparsed("guild_name", plainGuildName),
                Placeholder.component("guild_tag", guildComponent),
                Placeholder.component("prefix", prefix),
                Placeholder.component("suffix", ChatFormatter.suffix(hook, player)),
                Placeholder.component("additional_suffix", Text.parse(group.additionalSuffix())));

        Component name = ChatFormatter.style(group.nameColor(), player.displayName());
        if (interactiveName) {
            name = interact(settings.nameInteraction(), player, name, common);
        }
        return TagResolver.resolver(common, Placeholder.component("player", name));
    }

    /**
     * Placeholders of the console as a sender.
     */
    public static TagResolver console(ChatSettings settings) {
        return TagResolver.resolver(
                Placeholder.component("player", Text.parse(settings.consoleName())),
                Placeholder.unparsed("name", "Console"),
                Placeholder.unparsed("world", ""),
                Placeholder.unparsed("guild_name", ""),
                Placeholder.component("guild_tag", Component.empty()),
                Placeholder.component("prefix", Component.empty()),
                Placeholder.component("suffix", Component.empty()),
                Placeholder.component("additional_suffix", Component.empty()));
    }

    private static Component interact(NameInteraction interaction, Player player, Component name, TagResolver common) {
        Component result = name;
        if (interaction.hover()) {
            Component hover = Text.parse(ChatFormatter.template(player, interaction.hoverText()), common,
                    Placeholder.component("player", name));
            result = result.hoverEvent(HoverEvent.showText(hover));
        }
        if (interaction.click() != NameInteraction.ClickAction.NONE) {
            String value = PlaceholderHook.apply(player, interaction.clickValue().replace("<name>", player.getName()));
            result = result.clickEvent(interaction.click().create(value));
        }
        return result;
    }
}
