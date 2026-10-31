package me.x1no4ekk.core.gameplay.command;

import me.x1no4ekk.core.CorePlugin;
import me.x1no4ekk.core.command.BaseCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Stream;

/**
 * /speed &lt;1-10|reset&gt; [player] (walk speed, or fly speed while flying) and /flyspeed &lt;1-10|reset&gt; [player].
 * Levels map like Essentials: 1 is the vanilla speed (walk 0.2, fly 0.1), 10 is the maximum 1.0.
 */
public final class SpeedCommand extends BaseCommand {

    private static final float DEFAULT_WALK = 0.2f;
    private static final float DEFAULT_FLY = 0.1f;
    private static final double MAX_LEVEL = 10.0;
    private static final Set<String> RESET = Set.of("reset", "default", "сброс");

    private final boolean flyOnly;
    private final String permission;

    public SpeedCommand(CorePlugin plugin, boolean flyOnly) {
        super(plugin);
        this.flyOnly = flyOnly;
        this.permission = flyOnly ? "core.flyspeed" : "core.speed";
    }

    @Override
    protected void execute(CommandSender sender, String label, String[] args) {
        requirePermission(sender, permission);
        if (args.length < 1 || args.length > 2) {
            throw usage("gameplay.speed.usage");
        }
        String value = args[0];
        String targetName = args.length == 2 ? args[1] : null;
        // The old plugin used "/speed <player> <level>"; accept that order too.
        if (args.length == 2 && !isSpeedValue(args[0]) && isSpeedValue(args[1])) {
            value = args[1];
            targetName = args[0];
        }
        Player target;
        if (targetName != null) {
            target = otherPlayer(sender, targetName, permission + ".others");
        } else if (sender instanceof Player player) {
            target = player;
        } else {
            throw usage("gameplay.speed.usage");
        }

        boolean fly = flyOnly || target.isFlying();
        float vanilla = fly ? DEFAULT_FLY : DEFAULT_WALK;
        double level = RESET.contains(value.toLowerCase(Locale.ROOT)) ? 1.0 : parseDouble(value, 0.0, MAX_LEVEL);
        float speed = level < 1.0
                ? (float) (vanilla * level)
                : (float) (vanilla + (level - 1.0) / (MAX_LEVEL - 1.0) * (1.0f - vanilla));
        if (fly) {
            target.setFlySpeed(speed);
        } else {
            target.setWalkSpeed(speed);
        }

        String shown = level == Math.rint(level) ? String.valueOf((long) level) : String.valueOf(level);
        String kind = fly ? "fly" : "walk";
        send(target, "gameplay.speed." + kind, placeholder("speed", shown));
        if (!target.equals(sender)) {
            send(sender, "gameplay.speed." + kind + "-other", placeholder("player", target.getName()),
                    placeholder("speed", shown));
        }
    }

    private static boolean isSpeedValue(String raw) {
        if (RESET.contains(raw.toLowerCase(Locale.ROOT))) {
            return true;
        }
        try {
            double value = Double.parseDouble(raw.replace(',', '.'));
            return value >= 0.0 && value <= MAX_LEVEL;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Override
    protected List<String> complete(CommandSender sender, String label, String[] args) {
        if (!sender.hasPermission(permission)) {
            return List.of();
        }
        if (args.length == 1) {
            return filter(Stream.of("1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "reset"), args[0]);
        }
        if (args.length == 2 && sender.hasPermission(permission + ".others")) {
            return players(sender, args[1]);
        }
        return List.of();
    }
}
