package me.x1no4ekk.core.utility.moderation;

import me.x1no4ekk.core.CorePlugin;
import me.x1no4ekk.core.command.BaseCommand;
import me.x1no4ekk.core.user.User;
import me.x1no4ekk.core.util.Text;
import me.x1no4ekk.core.utility.UtilityText;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * {@code /realname <nickname>}: real names of online players whose nickname matches.
 */
final class RealNameCommand extends BaseCommand {

    private static final int MAX_RESULTS = 10;

    RealNameCommand(CorePlugin plugin) {
        super(plugin);
    }

    @Override
    protected void execute(CommandSender sender, String label, String[] args) {
        requirePermission(sender, "core.realname");
        if (args.length != 1) {
            throw usage("utility.realname.usage");
        }
        String query = normalize(args[0]);
        if (query.isEmpty()) {
            throw usage("utility.realname.usage");
        }
        List<Player> exact = new ArrayList<>();
        List<Player> partial = new ArrayList<>();
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (!canSee(sender, online)) {
                continue;
            }
            String display = normalize(Text.plain(online.displayName()));
            String nickname = nickname(online);
            if (display.equals(query) || query.equals(nickname)) {
                exact.add(online);
            } else if (display.contains(query) || (nickname != null && nickname.contains(query))) {
                partial.add(online);
            }
        }
        List<Player> matches = exact.isEmpty() ? partial : exact;
        if (matches.isEmpty()) {
            throw fail("utility.realname.not-found", placeholder("nick", args[0]));
        }
        matches.sort(Comparator.comparing(Player::getName, String.CASE_INSENSITIVE_ORDER));
        send(sender, "utility.realname.header", placeholder("nick", args[0]));
        for (Player match : matches.subList(0, Math.min(MAX_RESULTS, matches.size()))) {
            send(sender, "utility.realname.entry",
                    Placeholder.component("display", match.displayName()),
                    placeholder("player", match.getName()));
        }
    }

    private String nickname(Player player) {
        User user = plugin.userManager().getUser(player);
        return user != null && NicknameService.hasNickname(user)
                ? normalize(UtilityText.plainStyled(user.getNickname()))
                : null;
    }

    /**
     * Lower case without the decorative "~" that nickname formats commonly add.
     */
    private static String normalize(String input) {
        String result = input.trim().toLowerCase(Locale.ROOT);
        while (result.startsWith("~")) {
            result = result.substring(1);
        }
        return result;
    }

    @Override
    protected List<String> complete(CommandSender sender, String label, String[] args) {
        if (args.length != 1) {
            return List.of();
        }
        List<String> nicknames = new ArrayList<>();
        for (Player online : Bukkit.getOnlinePlayers()) {
            User user = plugin.userManager().getUser(online);
            if (canSee(sender, online) && user != null && NicknameService.hasNickname(user)) {
                nicknames.add(UtilityText.plainStyled(user.getNickname()));
            }
        }
        return filter(nicknames, args[0]);
    }
}
