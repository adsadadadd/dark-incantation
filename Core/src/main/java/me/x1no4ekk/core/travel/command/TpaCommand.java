package me.x1no4ekk.core.travel.command;

import me.x1no4ekk.core.CorePlugin;
import me.x1no4ekk.core.command.BaseCommand;
import me.x1no4ekk.core.travel.TpaManager;
import me.x1no4ekk.core.travel.TravelModule;
import me.x1no4ekk.core.util.Durations;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * /tpa &lt;player&gt; (go to the player) and /tpahere &lt;player&gt; (call the player), with clickable answers.
 */
public final class TpaCommand extends BaseCommand {

    private static final String BYPASS_COOLDOWN_PERMISSION = "core.tpa.bypass-cooldown";

    private final TravelModule module;
    private final TpaManager.Type type;
    private final String permission;
    private final String keys;

    public TpaCommand(CorePlugin plugin, TravelModule module, TpaManager.Type type) {
        super(plugin);
        this.module = module;
        this.type = type;
        this.permission = type == TpaManager.Type.TO_TARGET ? "core.tpa" : "core.tpahere";
        this.keys = type == TpaManager.Type.TO_TARGET ? "travel.tpa." : "travel.tpahere.";
    }

    @Override
    protected void execute(CommandSender sender, String label, String[] args) {
        Player player = playerOnly(sender);
        requirePermission(sender, permission);
        if (args.length != 1) {
            throw usage(keys + "usage");
        }
        Player target = player(sender, args[0]);
        if (target.equals(player)) {
            throw fail("travel.tpa.self");
        }
        if (plugin.userManager().getUser(target) == null) {
            throw fail("travel.target-not-loaded", placeholder("player", target.getName()));
        }
        if (!TeleportToggles.accepts(plugin, sender, target, true)) {
            throw fail("travel.tp-disabled", placeholder("player", target.getName()));
        }
        TpaManager tpa = module.tpa();
        if (tpa.hasRequest(player.getUniqueId(), target.getUniqueId())) {
            throw fail("travel.tpa.already-sent", placeholder("player", target.getName()));
        }
        long cooldown = player.hasPermission(BYPASS_COOLDOWN_PERMISSION) ? 0L : tpa.cooldownLeft(player.getUniqueId());
        if (cooldown > 0) {
            throw fail("general.cooldown", placeholder("time", Durations.format(cooldown)));
        }

        tpa.add(player, target, type);
        long seconds = module.settings().tpaTimeoutMillis() / 1000L;
        send(player, keys + "sent", placeholder("player", target.getName()), placeholder("seconds", seconds));
        String name = player.getName();
        send(target, keys + "received",
                placeholder("player", name),
                placeholder("seconds", seconds),
                TagResolver.resolver("accept", Tag.styling(ClickEvent.runCommand("/tpaccept " + name))),
                TagResolver.resolver("deny", Tag.styling(ClickEvent.runCommand("/tpdeny " + name))));
        module.settings().tpaSound().play(target);
    }

    @Override
    protected List<String> complete(CommandSender sender, String label, String[] args) {
        return args.length == 1 && sender.hasPermission(permission) ? players(sender, args[0]) : List.of();
    }
}
