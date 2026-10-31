package me.x1no4ekk.core.gameplay.command;

import me.x1no4ekk.core.CorePlugin;
import me.x1no4ekk.core.command.BaseCommand;
import me.x1no4ekk.core.gameplay.GameplayModule;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

/**
 * {@code /cmd [player]}: acts on the sender, or on another player with {@code <permission>.others}.
 */
abstract class TargetCommand extends BaseCommand {

    protected final GameplayModule module;
    private final String permission;
    private final String usageKey;

    protected TargetCommand(CorePlugin plugin, GameplayModule module, String permission, String usageKey) {
        super(plugin);
        this.module = module;
        this.permission = permission;
        this.usageKey = usageKey;
    }

    /**
     * @param self whether the sender acts on themselves
     */
    protected abstract void apply(CommandSender sender, Player target, boolean self);

    @Override
    protected final void execute(CommandSender sender, String label, String[] args) {
        requirePermission(sender, permission);
        if (args.length > 1 || args.length == 0 && !(sender instanceof Player)) {
            throw usage(usageKey);
        }
        Player target = args.length == 1 ? otherPlayer(sender, args[0], permission + ".others") : (Player) sender;
        apply(sender, target, target.equals(sender));
    }

    @Override
    protected List<String> complete(CommandSender sender, String label, String[] args) {
        if (args.length == 1 && sender.hasPermission(permission + ".others")) {
            return players(sender, args[0]);
        }
        return List.of();
    }

    /**
     * Sends {@code targetKey} to the target and, when acting on someone else, {@code senderKey} (with
     * {@code <player>}) to the sender.
     */
    protected void report(CommandSender sender, Player target, boolean self, String targetKey, String senderKey,
                          TagResolver... resolvers) {
        send(target, targetKey, resolvers);
        if (!self) {
            TagResolver[] withPlayer = Arrays.copyOf(resolvers, resolvers.length + 1);
            withPlayer[resolvers.length] = placeholder("player", target.getName());
            send(sender, senderKey, withPlayer);
        }
    }
}
