package me.x1no4ekk.core.gameplay.command;

import me.x1no4ekk.core.CorePlugin;
import me.x1no4ekk.core.gameplay.GameplayModule;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

import java.util.Collection;
import java.util.List;

/**
 * /milk [player] — removes every potion effect, like drinking milk.
 */
public final class MilkCommand extends TargetCommand {

    public MilkCommand(CorePlugin plugin, GameplayModule module) {
        super(plugin, module, "core.milk", "gameplay.milk.usage");
    }

    @Override
    protected void apply(CommandSender sender, Player target, boolean self) {
        Collection<PotionEffect> effects = target.getActivePotionEffects();
        if (effects.isEmpty()) {
            throw self ? fail("gameplay.milk.nothing")
                    : fail("gameplay.milk.nothing-other", placeholder("player", target.getName()));
        }
        List<Component> names = effects.stream()
                .map(effect -> (Component) Component.translatable(effect.getType()))
                .toList();
        for (PotionEffect effect : effects) {
            target.removePotionEffect(effect.getType());
        }
        report(sender, target, self, "gameplay.milk.self", "gameplay.milk.other",
                Placeholder.component("effects", Component.join(JoinConfiguration.commas(true), names)));
    }
}
