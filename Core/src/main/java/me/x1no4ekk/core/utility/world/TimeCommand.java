package me.x1no4ekk.core.utility.world;

import me.x1no4ekk.core.CorePlugin;
import me.x1no4ekk.core.utility.UtilityText;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * {@code /day [world]}, {@code /night [world]} and
 * {@code /time [set] <day|night|noon|midnight|ticks> [world]}, {@code /time add <ticks> [world]}.
 */
final class TimeCommand extends WorldControlCommand {

    static final Map<String, Long> PRESETS = presets();
    private static final int MAX_TICKS = 24_000;

    /** {@code day}/{@code night} for the shortcut commands, {@code null} for {@code /time}. */
    private final String preset;

    TimeCommand(CorePlugin plugin, WorldModule module, String preset) {
        super(plugin, module, false);
        this.preset = preset;
    }

    private static Map<String, Long> presets() {
        Map<String, Long> presets = new LinkedHashMap<>();
        presets.put("day", 1_000L);
        presets.put("noon", 6_000L);
        presets.put("night", 13_000L);
        presets.put("midnight", 18_000L);
        return Collections.unmodifiableMap(presets);
    }

    @Override
    protected void execute(CommandSender sender, String label, String[] args) {
        requirePermission(sender, "core." + (preset != null ? preset : "time"));
        if (preset != null) {
            if (args.length > 1) {
                throw fail("utility.time.fixed-usage", placeholder("command", label));
            }
            World world = resolveWorld(sender, args.length == 1 ? args[0] : null);
            change(sender, world, false, PRESETS.get(preset), presetName(preset));
            return;
        }
        if (args.length == 0) {
            showCurrent(sender);
            return;
        }
        int index = args[0].equalsIgnoreCase("set") ? 1 : 0;
        if (args.length <= index) {
            throw usage("utility.time.usage");
        }
        String action = args[index].toLowerCase(Locale.ROOT);
        boolean add = action.equals("add");
        long ticks;
        Component name;
        int worldIndex;
        if (add) {
            if (index == 1 || args.length < 2) {
                throw usage("utility.time.usage");
            }
            ticks = parseInt(args[1], 1, MAX_TICKS);
            name = messages().get("utility.time.names.added", placeholder("ticks", ticks));
            worldIndex = 2;
        } else if (PRESETS.containsKey(action)) {
            ticks = PRESETS.get(action);
            name = presetName(action);
            worldIndex = index + 1;
        } else {
            ticks = parseInt(action, 0, MAX_TICKS - 1);
            name = messages().get("utility.time.names.ticks", placeholder("ticks", ticks),
                    placeholder("clock", UtilityText.clock(ticks)));
            worldIndex = index + 1;
        }
        if (args.length > worldIndex + 1) {
            throw usage("utility.time.usage");
        }
        World world = resolveWorld(sender, args.length > worldIndex ? args[worldIndex] : null);
        change(sender, world, add, ticks, name);
    }

    private void change(CommandSender sender, World world, boolean add, long ticks, Component name) {
        checkCooldown(sender, world);
        if (add) {
            world.setFullTime(world.getFullTime() + ticks);
        } else {
            world.setTime(ticks);
        }
        announce(sender, world, TagResolver.resolver(
                Placeholder.component("time", name),
                placeholder("world", world.getName()),
                placeholder("clock", UtilityText.clock(world.getTime())),
                Placeholder.component("player", UtilityText.senderName(messages(), sender))));
    }

    private void showCurrent(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            throw usage("utility.time.usage");
        }
        World world = player.getWorld();
        long ticks = world.getTime();
        boolean day = ticks < 13_000L || ticks >= 23_000L;
        send(sender, "utility.time.current",
                placeholder("world", world.getName()),
                placeholder("ticks", ticks),
                placeholder("clock", UtilityText.clock(ticks)),
                Placeholder.component("phase", messages().get(day ? "utility.time.names.day" : "utility.time.names.night")));
    }

    private Component presetName(String key) {
        return messages().get("utility.time.names." + key);
    }

    @Override
    protected List<String> complete(CommandSender sender, String label, String[] args) {
        if (preset != null) {
            return args.length == 1 ? worldNames(args[0]) : List.of();
        }
        if (args.length == 1) {
            List<String> options = new ArrayList<>(PRESETS.keySet());
            options.add("add");
            options.add("set");
            return filter(options, args[0]);
        }
        boolean set = args[0].equalsIgnoreCase("set");
        boolean add = args[0].equalsIgnoreCase("add");
        if (args.length == 2) {
            if (set) {
                return filter(PRESETS.keySet(), args[1]);
            }
            return add ? filter(List.of("1000", "6000", "12000"), args[1]) : worldNames(args[1]);
        }
        if (args.length == 3 && (set || add)) {
            return worldNames(args[2]);
        }
        return List.of();
    }
}
