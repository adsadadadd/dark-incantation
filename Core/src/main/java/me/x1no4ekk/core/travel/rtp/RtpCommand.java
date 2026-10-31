package me.x1no4ekk.core.travel.rtp;

import me.x1no4ekk.core.CorePlugin;
import me.x1no4ekk.core.command.BaseCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Locale;

/**
 * /rtp — opens the mode menu; /rtp &lt;mode&gt; starts a search directly.
 */
final class RtpCommand extends BaseCommand {

    private static final String PERMISSION = "core.rtp";

    private final RtpModule module;

    RtpCommand(CorePlugin plugin, RtpModule module) {
        super(plugin);
        this.module = module;
    }

    @Override
    protected void execute(CommandSender sender, String label, String[] args) {
        Player player = playerOnly(sender);
        requirePermission(sender, PERMISSION);
        RtpSettings config = module.settings();
        if (args.length == 0) {
            new RtpGui(module.manager(), config, player).open(player);
            return;
        }
        if (args.length != 1) {
            throw usage("travel.rtp.usage");
        }
        RtpSettings.RtpMode mode = config.modes().get(args[0].toLowerCase(Locale.ROOT));
        if (mode == null) {
            throw fail("travel.rtp.unknown-mode", placeholder("mode", args[0]),
                    placeholder("modes", String.join(", ", config.modes().keySet())));
        }
        module.manager().start(player, mode);
    }

    @Override
    protected List<String> complete(CommandSender sender, String label, String[] args) {
        if (args.length != 1 || !(sender instanceof Player player) || !sender.hasPermission(PERMISSION)) {
            return List.of();
        }
        return filter(module.settings().modes().values().stream()
                .filter(mode -> module.manager().canUse(player, mode))
                .map(RtpSettings.RtpMode::id), args[0]);
    }
}
