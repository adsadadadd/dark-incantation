package me.x1no4ekk.core.utility;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Command sender captured for an asynchronous reply: players by UUID, every other sender as the console.
 */
public record SenderRef(UUID playerId) {

    public static SenderRef of(CommandSender sender) {
        return new SenderRef(sender instanceof Player player ? player.getUniqueId() : null);
    }

    /**
     * @return the sender, or {@code null} if the player has left; main thread only
     */
    public CommandSender resolve() {
        return playerId == null ? Bukkit.getConsoleSender() : Bukkit.getPlayer(playerId);
    }
}
