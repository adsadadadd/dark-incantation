package me.x1no4ekk.core.gameplay.command;

import me.x1no4ekk.core.CorePlugin;
import me.x1no4ekk.core.gameplay.GameplayModule;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * /feed [player] — fills hunger and saturation.
 */
public final class FeedCommand extends TargetCommand {

    public FeedCommand(CorePlugin plugin, GameplayModule module) {
        super(plugin, module, "core.feed", "gameplay.feed.usage");
    }

    @Override
    protected void apply(CommandSender sender, Player target, boolean self) {
        PlayerCare.feed(target);
        report(sender, target, self, "gameplay.feed.self", "gameplay.feed.other");
    }
}
