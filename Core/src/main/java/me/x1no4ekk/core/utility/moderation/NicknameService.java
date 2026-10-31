package me.x1no4ekk.core.utility.moderation;

import me.x1no4ekk.core.CorePlugin;
import me.x1no4ekk.core.user.User;
import me.x1no4ekk.core.util.Text;
import me.x1no4ekk.core.utility.UtilityText;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * Applies {@link User#getNickname()} (MiniMessage, colours and decorations only) to the display and tab-list names.
 */
final class NicknameService {

    private final CorePlugin plugin;
    private final ModerationModule module;

    NicknameService(CorePlugin plugin, ModerationModule module) {
        this.plugin = plugin;
        this.module = module;
    }

    static boolean hasNickname(User user) {
        String nickname = user.getNickname();
        return nickname != null && !nickname.isBlank();
    }

    void apply(Player player, User user) {
        if (!hasNickname(user)) {
            player.displayName(null);
            player.playerListName(null);
            return;
        }
        Component display = Text.parse(module.settings().nick().displayFormat(),
                Placeholder.component("nickname", UtilityText.styled(user.getNickname())));
        player.displayName(display);
        player.playerListName(module.settings().nick().tabList() ? display : null);
    }

    /**
     * Re-applies every online nickname, e.g. after the display format was reloaded.
     */
    void applyAll() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            User user = plugin.userManager().getUser(player);
            if (user != null) {
                apply(player, user);
            }
        }
    }

    /**
     * Restores the real names (module disable).
     */
    void resetAll() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            User user = plugin.userManager().getUser(player);
            if (user != null && hasNickname(user)) {
                player.displayName(null);
                player.playerListName(null);
            }
        }
    }
}
