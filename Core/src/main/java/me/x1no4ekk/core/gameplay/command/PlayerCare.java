package me.x1no4ekk.core.gameplay.command;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectTypeCategory;

/**
 * Health and hunger helpers shared by /heal, /feed and /god.
 */
final class PlayerCare {

    private static final int MAX_FOOD = 20;

    private PlayerCare() {
    }

    static void restoreHealth(Player player) {
        AttributeInstance maxHealth = player.getAttribute(Attribute.MAX_HEALTH);
        player.setHealth(maxHealth != null ? maxHealth.getValue() : 20.0);
        player.setFireTicks(0);
    }

    static void feed(Player player) {
        player.setFoodLevel(MAX_FOOD);
        player.setSaturation(MAX_FOOD);
        player.setExhaustion(0f);
    }

    static void removeHarmfulEffects(Player player) {
        for (PotionEffect effect : player.getActivePotionEffects()) {
            if (effect.getType().getCategory() == PotionEffectTypeCategory.HARMFUL) {
                player.removePotionEffect(effect.getType());
            }
        }
    }
}
