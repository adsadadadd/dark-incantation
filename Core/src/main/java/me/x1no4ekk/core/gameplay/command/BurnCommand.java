package me.x1no4ekk.core.gameplay.command;

import me.x1no4ekk.core.CorePlugin;
import me.x1no4ekk.core.command.BaseCommand;
import me.x1no4ekk.core.gameplay.GameplayModule;
import me.x1no4ekk.core.gameplay.GameplaySettings;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Stream;

/**
 * /burn &lt;player&gt; [seconds] — sets a player on fire.
 */
public final class BurnCommand extends BaseCommand {

    private final GameplayModule module;

    public BurnCommand(CorePlugin plugin, GameplayModule module) {
        super(plugin);
        this.module = module;
    }

    @Override
    protected void execute(CommandSender sender, String label, String[] args) {
        requirePermission(sender, "core.burn");
        if (args.length < 1 || args.length > 2) {
            throw usage("gameplay.burn.usage");
        }
        Player target = player(sender, args[0]);
        GameplaySettings.Burn config = module.settings().burn();
        int seconds = args.length == 2 ? parseInt(args[1], 1, config.maxSeconds()) : config.defaultSeconds();
        target.setFireTicks(seconds * 20);
        send(sender, "gameplay.burn.done", placeholder("player", target.getName()), placeholder("seconds", seconds));
    }

    @Override
    protected List<String> complete(CommandSender sender, String label, String[] args) {
        if (!sender.hasPermission("core.burn")) {
            return List.of();
        }
        if (args.length == 1) {
            return players(sender, args[0]);
        }
        if (args.length == 2) {
            return filter(Stream.of("5", "10", "30", "60"), args[1]);
        }
        return List.of();
    }
}
