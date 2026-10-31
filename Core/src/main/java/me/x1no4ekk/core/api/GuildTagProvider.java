package me.x1no4ekk.core.api;

import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Supplies guild information for Core's chat format ({@code <guild_tag>}, {@code <guild_name>}).
 * <p>
 * Implemented by the Guilds plugin and registered in Bukkit's ServicesManager:
 * <pre>
 * Bukkit.getServicesManager().register(GuildTagProvider.class, provider, plugin, ServicePriority.Normal);
 * </pre>
 * Methods are called from the async chat thread and must be thread-safe and non-blocking (cache lookups only).
 */
public interface GuildTagProvider {

    /**
     * MiniMessage-formatted guild tag of the player (e.g. {@code <gold>[ABC]</gold>}), or {@code null} if not in a guild.
     */
    @Nullable
    String getGuildTag(UUID player);

    /**
     * Plain guild name, or {@code null} if not in a guild.
     */
    @Nullable
    String getGuildName(UUID player);
}
