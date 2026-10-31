package me.x1no4ekk.core.utility.moderation;

import me.x1no4ekk.core.CorePlugin;
import me.x1no4ekk.core.command.BaseCommand;
import me.x1no4ekk.core.user.User;
import me.x1no4ekk.core.util.Text;
import me.x1no4ekk.core.utility.SenderRef;
import me.x1no4ekk.core.utility.UtilityText;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.stream.Stream;

/**
 * {@code /nick <name|off> [player]}: sets the nickname shown in chat and the tab list.
 */
final class NickCommand extends BaseCommand {

    private static final Set<String> RESET_WORDS = Set.of("off", "reset", "clear");
    /** Width of {@code core_users.nickname}. */
    private static final int MAX_STORED_LENGTH = 128;

    private final ModerationModule module;

    NickCommand(CorePlugin plugin, ModerationModule module) {
        super(plugin);
        this.module = module;
    }

    @Override
    protected void execute(CommandSender sender, String label, String[] args) {
        requirePermission(sender, "core.nick");
        if (args.length < 1 || args.length > 2) {
            throw usage("utility.nick.usage");
        }
        Player target = args.length == 2 ? otherPlayer(sender, args[1], "core.nick.others") : playerOnly(sender);
        User user = user(target);
        if (RESET_WORDS.contains(args[0].toLowerCase(Locale.ROOT))) {
            if (!NicknameService.hasNickname(user)) {
                throw fail("utility.nick.not-set");
            }
            user.setNickname(null);
            module.nicknames().apply(target, user);
            send(target, "utility.nick.reset");
            if (!target.equals(sender)) {
                send(sender, "utility.nick.reset-other", placeholder("player", target.getName()));
            }
            return;
        }
        String stored = sender.hasPermission("core.nick.color") ? args[0] : Text.escape(args[0]);
        String plain = UtilityText.plainStyled(stored);
        validate(target, stored, plain);
        if (!module.settings().nick().checkRegisteredNames() || plain.equalsIgnoreCase(target.getName())) {
            apply(sender, target, user, stored);
            return;
        }
        // A nickname must not impersonate any registered player, online or not.
        SenderRef senderRef = SenderRef.of(sender);
        UUID targetId = target.getUniqueId();
        String targetName = target.getName();
        plugin.userManager().findByName(plain).whenCompleteAsync((found, error) -> {
            CommandSender replyTo = senderRef.resolve();
            if (error != null) {
                plugin.getLogger().log(Level.SEVERE, "Could not check nickname '" + plain + "'", error);
                if (replyTo != null) {
                    send(replyTo, "general.database-error");
                }
                return;
            }
            if (found.isPresent() && !found.get().getKey().equals(targetId)) {
                if (replyTo != null) {
                    send(replyTo, "utility.nick.taken", placeholder("nick", plain));
                }
                return;
            }
            Player online = Bukkit.getPlayer(targetId);
            User loaded = plugin.userManager().getUser(targetId);
            if (online == null || loaded == null) {
                if (replyTo != null) {
                    send(replyTo, "general.player-only-online", placeholder("player", targetName));
                }
                return;
            }
            apply(replyTo, online, loaded, stored);
        }, plugin.mainThread());
    }

    private void validate(Player target, String stored, String plain) {
        ModerationSettings.Nick rules = module.settings().nick();
        if (stored.length() > MAX_STORED_LENGTH) {
            throw fail("utility.nick.too-much-formatting");
        }
        int length = plain.codePointCount(0, plain.length());
        if (length < rules.minLength() || length > rules.maxLength()) {
            throw fail("utility.nick.length", placeholder("min", rules.minLength()), placeholder("max", rules.maxLength()));
        }
        if (!rules.pattern().matcher(plain).matches()) {
            throw fail("utility.nick.invalid");
        }
        String lower = plain.toLowerCase(Locale.ROOT);
        for (String word : rules.forbidden()) {
            if (lower.contains(word)) {
                throw fail("utility.nick.forbidden");
            }
        }
        for (Player other : Bukkit.getOnlinePlayers()) {
            if (other.equals(target)) {
                continue;
            }
            User otherUser = plugin.userManager().getUser(other);
            boolean sameNick = otherUser != null && NicknameService.hasNickname(otherUser)
                    && UtilityText.plainStyled(otherUser.getNickname()).equalsIgnoreCase(plain);
            if (sameNick || other.getName().equalsIgnoreCase(plain)) {
                throw fail("utility.nick.taken", placeholder("nick", plain));
            }
        }
    }

    private void apply(CommandSender sender, Player target, User user, String stored) {
        user.setNickname(stored);
        module.nicknames().apply(target, user);
        send(target, "utility.nick.set", Placeholder.component("nick", target.displayName()));
        if (sender != null && !target.equals(sender)) {
            send(sender, "utility.nick.set-other", placeholder("player", target.getName()),
                    Placeholder.component("nick", target.displayName()));
        }
    }

    @Override
    protected List<String> complete(CommandSender sender, String label, String[] args) {
        if (args.length == 1) {
            return filter(Stream.of("off"), args[0]);
        }
        if (args.length == 2 && sender.hasPermission("core.nick.others")) {
            return players(sender, args[1]);
        }
        return List.of();
    }
}
