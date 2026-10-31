package me.x1no4ekk.core.gameplay.command;

import me.x1no4ekk.core.CorePlugin;
import me.x1no4ekk.core.gameplay.Cooldowns;
import me.x1no4ekk.core.gameplay.GameplayModule;
import me.x1no4ekk.core.user.User;
import me.x1no4ekk.core.util.Durations;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * /fly [player] — toggles the persisted fly mode. Cannot be enabled during combat.
 */
public final class FlyCommand extends TargetCommand {

    private static final String BYPASS_COOLDOWN = "core.fly.others.bypass-cooldown";

    public FlyCommand(CorePlugin plugin, GameplayModule module) {
        super(plugin, module, "core.fly", "gameplay.fly.usage");
    }

    @Override
    protected void apply(CommandSender sender, Player target, boolean self) {
        User user = user(target);
        boolean enable = !user.isFly();
        UUID targetId = target.getUniqueId();
        if (enable && plugin.combatManager().isInCombat(targetId)) {
            throw self
                    ? fail("gameplay.fly.in-combat", placeholder("seconds", plugin.combatManager().secondsLeft(targetId)))
                    : fail("gameplay.fly.in-combat-other", placeholder("player", target.getName()));
        }
        if (!self && sender instanceof Player staff && !staff.hasPermission(BYPASS_COOLDOWN)) {
            Cooldowns cooldowns = module.flyOthersCooldowns();
            long left = cooldowns.remaining(staff.getUniqueId());
            if (left > 0) {
                throw fail("general.cooldown", placeholder("time", Durations.format(left)));
            }
            cooldowns.start(staff.getUniqueId(), module.settings().fly().othersCooldownMillis());
        }
        module.flight().setFly(target, user, enable);
        report(sender, target, self,
                enable ? "gameplay.fly.enabled" : "gameplay.fly.disabled",
                enable ? "gameplay.fly.enabled-other" : "gameplay.fly.disabled-other");
    }
}
