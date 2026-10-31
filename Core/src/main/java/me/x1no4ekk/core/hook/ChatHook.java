package me.x1no4ekk.core.hook;

import org.bukkit.entity.Player;

/**
 * Group, prefix and suffix of a player through Vault Chat/Permission (LuckPerms etc.).
 * All methods are safe to call from the async chat thread and return empty values when unavailable.
 */
public interface ChatHook {

    boolean isAvailable();

    /**
     * Primary permission group in lower case, or an empty string.
     */
    String getPrimaryGroup(Player player);

    /**
     * Raw prefix (may contain legacy {@code &} codes), or an empty string.
     */
    String getPrefix(Player player);

    /**
     * Raw suffix (may contain legacy {@code &} codes), or an empty string.
     */
    String getSuffix(Player player);
}
