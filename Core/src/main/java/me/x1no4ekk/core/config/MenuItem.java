package me.x1no4ekk.core.config;

import me.x1no4ekk.core.util.ItemBuilder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * A configurable menu icon:
 * <pre>
 * slot: 49          # or slots: [0-8, 45-53]
 * material: ARROW
 * amount: 1
 * name: '&lt;yellow&gt;Назад'
 * lore: []
 * glint: false
 * hide-tooltip: false
 * </pre>
 */
public record MenuItem(Material material, int amount, String name, List<String> lore, boolean glint,
                       boolean hideTooltip, List<Integer> slots) {

    /**
     * @param rows         menu height, used to validate slots
     * @param requireSlot  whether {@code slot}/{@code slots} must be present
     */
    public static MenuItem load(ConfigurationSection section, int rows, boolean requireSlot) {
        List<Integer> slots;
        if (section.isList("slots")) {
            slots = ConfigUtil.slots(section, "slots", rows);
        } else if (section.contains("slot")) {
            int slot = section.getInt("slot", -1);
            if (slot < 0 || slot >= rows * 9) {
                throw new IllegalArgumentException(ConfigUtil.path(section, "slot") + " must be inside the menu (0-" + (rows * 9 - 1) + ")");
            }
            slots = List.of(slot);
        } else {
            slots = List.of();
        }
        if (requireSlot && slots.isEmpty()) {
            throw new IllegalArgumentException("'" + ConfigUtil.path(section, "slot") + "' is required");
        }
        return new MenuItem(
                ConfigUtil.material(ConfigUtil.requireText(section, "material"), ConfigUtil.path(section, "material")),
                Math.clamp(section.getInt("amount", 1), 1, 99),
                section.getString("name", ""),
                List.copyOf(section.getStringList("lore")),
                section.getBoolean("glint", false),
                section.getBoolean("hide-tooltip", false),
                slots
        );
    }

    /**
     * Loads the child section {@code key} of {@code parent}.
     */
    public static MenuItem load(ConfigurationSection parent, String key, int rows, boolean requireSlot) {
        return load(ConfigUtil.section(parent, key), rows, requireSlot);
    }

    /**
     * @return the first configured slot, or {@code -1}
     */
    public int slot() {
        return slots.isEmpty() ? -1 : slots.getFirst();
    }

    public ItemStack build(TagResolver... resolvers) {
        return build(material, resolvers);
    }

    /**
     * Builds the icon with another material (e.g. a dynamic icon) but this item's texts.
     */
    public ItemStack build(Material override, TagResolver... resolvers) {
        ItemBuilder builder = ItemBuilder.of(override, amount)
                .name(name, resolvers)
                .lore(lore, resolvers)
                .glint(glint);
        if (hideTooltip) {
            builder.hideTooltip();
        }
        return builder.build();
    }
}
