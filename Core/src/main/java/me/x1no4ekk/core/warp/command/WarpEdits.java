package me.x1no4ekk.core.warp.command;

import me.x1no4ekk.core.CorePlugin;
import me.x1no4ekk.core.command.CommandException;
import me.x1no4ekk.core.warp.Warp;
import me.x1no4ekk.core.warp.WarpModule;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

/**
 * Shared argument parsing and write-through for /editwarp and /serverwarp.
 */
final class WarpEdits {

    static final String RESET = "reset";

    private WarpEdits() {
    }

    /**
     * @param raw material name, {@code reset}, or {@code null} for the item in the main hand
     * @return the icon, or empty to reset to the default icon
     */
    static Optional<Material> icon(CommandSender sender, String raw) {
        if (raw == null) {
            if (!(sender instanceof Player player)) {
                throw new CommandException("general.players-only");
            }
            Material hand = player.getInventory().getItemInMainHand().getType();
            if (hand.isAir()) {
                throw new CommandException("general.no-item-in-hand");
            }
            return Optional.of(hand);
        }
        if (raw.equalsIgnoreCase(RESET)) {
            return Optional.empty();
        }
        Material material = Material.matchMaterial(raw);
        if (material == null || material.isAir() || !material.isItem()) {
            throw new CommandException("warp.invalid-icon", Placeholder.unparsed("icon", raw));
        }
        return Optional.of(material);
    }

    /**
     * Joins description words; {@code -} or nothing clears the description.
     */
    static String description(WarpModule module, String[] args, int from) {
        String text = from < args.length ? String.join(" ", Arrays.copyOfRange(args, from, args.length)).trim() : "";
        if (text.isEmpty() || text.equals("-")) {
            return null;
        }
        int max = module.settings().descriptionMaxLength();
        if (text.length() > max) {
            throw new CommandException("warp.description-too-long", Placeholder.unparsed("max", String.valueOf(max)));
        }
        return text;
    }

    /**
     * Checks that the player may move a warp to their position.
     */
    static void checkMove(WarpModule module, Player player, boolean ignoreDisabledWorlds) {
        if (player.getGameMode() == GameMode.SPECTATOR) {
            throw new CommandException("warp.spectator");
        }
        if (!ignoreDisabledWorlds && module.manager().isDisabledWorld(player.getLocation())) {
            throw new CommandException("warp.disabled-world");
        }
    }

    /**
     * Writes the change and reports a database failure to the sender.
     */
    static void apply(CorePlugin plugin, WarpModule module, CommandSender sender, Warp before, Warp after) {
        UUID senderId = sender instanceof Player player ? player.getUniqueId() : null;
        module.manager().update(before, after, error -> {
            CommandSender actor = senderId == null ? Bukkit.getConsoleSender() : Bukkit.getPlayer(senderId);
            if (actor != null) {
                plugin.messages().send(actor, "general.database-error");
            }
        });
    }
}
