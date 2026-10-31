package me.x1no4ekk.core.gameplay.command;

import me.x1no4ekk.core.CorePlugin;
import me.x1no4ekk.core.command.BaseCommand;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static java.util.Map.entry;

/**
 * /gamemode &lt;mode&gt; [player] and the shortcuts /gms, /gmc, /gma, /gmsp [player].
 * Each mode needs {@code core.gamemode.<survival|creative|adventure|spectator>}.
 */
public final class GamemodeCommand extends BaseCommand {

    private static final String PERMISSION = "core.gamemode";
    private static final String OTHERS_PERMISSION = "core.gamemode.others";
    private static final Map<String, GameMode> ALIASES = Map.ofEntries(
            entry("0", GameMode.SURVIVAL), entry("s", GameMode.SURVIVAL), entry("survival", GameMode.SURVIVAL),
            entry("gms", GameMode.SURVIVAL), entry("в", GameMode.SURVIVAL), entry("выживание", GameMode.SURVIVAL),
            entry("1", GameMode.CREATIVE), entry("c", GameMode.CREATIVE), entry("creative", GameMode.CREATIVE),
            entry("gmc", GameMode.CREATIVE), entry("к", GameMode.CREATIVE), entry("креатив", GameMode.CREATIVE),
            entry("творческий", GameMode.CREATIVE),
            entry("2", GameMode.ADVENTURE), entry("a", GameMode.ADVENTURE), entry("adventure", GameMode.ADVENTURE),
            entry("gma", GameMode.ADVENTURE), entry("п", GameMode.ADVENTURE), entry("приключение", GameMode.ADVENTURE),
            entry("3", GameMode.SPECTATOR), entry("sp", GameMode.SPECTATOR), entry("spectator", GameMode.SPECTATOR),
            entry("gmsp", GameMode.SPECTATOR), entry("н", GameMode.SPECTATOR), entry("наблюдатель", GameMode.SPECTATOR),
            entry("спектатор", GameMode.SPECTATOR));
    private static final List<GameMode> MODES = List.of(GameMode.SURVIVAL, GameMode.CREATIVE, GameMode.ADVENTURE,
            GameMode.SPECTATOR);

    /**
     * {@code null} for /gamemode, otherwise the mode of the shortcut command.
     */
    private final GameMode fixedMode;

    public GamemodeCommand(CorePlugin plugin, GameMode fixedMode) {
        super(plugin);
        this.fixedMode = fixedMode;
    }

    @Override
    protected void execute(CommandSender sender, String label, String[] args) {
        requirePermission(sender, PERMISSION);
        GameMode mode;
        String targetName;
        if (fixedMode == null) {
            if (args.length < 1 || args.length > 2) {
                throw usage("gameplay.gamemode.usage");
            }
            mode = ALIASES.get(args[0].toLowerCase(Locale.ROOT));
            if (mode == null) {
                throw fail("gameplay.gamemode.unknown", placeholder("mode", args[0]));
            }
            targetName = args.length == 2 ? args[1] : null;
        } else {
            if (args.length > 1) {
                throw fail("gameplay.gamemode.short-usage", placeholder("command", label));
            }
            mode = fixedMode;
            targetName = args.length == 1 ? args[0] : null;
        }
        requirePermission(sender, permission(mode));

        Player target;
        if (targetName != null) {
            target = otherPlayer(sender, targetName, OTHERS_PERMISSION);
        } else if (sender instanceof Player player) {
            target = player;
        } else {
            throw fixedMode == null ? usage("gameplay.gamemode.usage")
                    : fail("gameplay.gamemode.short-usage", placeholder("command", label));
        }

        TagResolver modeName = modeName(mode);
        if (target.getGameMode() == mode) {
            throw target.equals(sender) ? fail("gameplay.gamemode.already", modeName)
                    : fail("gameplay.gamemode.already-other", placeholder("player", target.getName()), modeName);
        }
        target.setGameMode(mode);
        if (target.getGameMode() != mode) {
            throw fail("gameplay.gamemode.cancelled");
        }
        send(target, "gameplay.gamemode.changed", modeName);
        if (!target.equals(sender)) {
            send(sender, "gameplay.gamemode.changed-other", placeholder("player", target.getName()), modeName);
        }
    }

    private static String permission(GameMode mode) {
        return PERMISSION + "." + mode.name().toLowerCase(Locale.ROOT);
    }

    private TagResolver modeName(GameMode mode) {
        return Placeholder.component("mode", messages().get("gameplay.gamemode.names." + mode.name().toLowerCase(Locale.ROOT)));
    }

    @Override
    protected List<String> complete(CommandSender sender, String label, String[] args) {
        if (!sender.hasPermission(PERMISSION)) {
            return List.of();
        }
        if (fixedMode == null && args.length == 1) {
            List<String> options = new ArrayList<>();
            for (GameMode mode : MODES) {
                if (sender.hasPermission(permission(mode))) {
                    options.add(mode.name().toLowerCase(Locale.ROOT));
                }
            }
            return filter(options, args[0]);
        }
        int targetIndex = fixedMode == null ? 1 : 0;
        if (args.length == targetIndex + 1 && sender.hasPermission(OTHERS_PERMISSION)) {
            return players(sender, args[targetIndex]);
        }
        return List.of();
    }
}
