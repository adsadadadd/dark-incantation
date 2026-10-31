package me.x1no4ekk.core.gameplay.command;

import me.x1no4ekk.core.CorePlugin;
import me.x1no4ekk.core.command.BaseCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Color;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.CustomModelDataComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * /itemdb — technical information about the held item.
 */
public final class ItemDbCommand extends BaseCommand {

    public ItemDbCommand(CorePlugin plugin) {
        super(plugin);
    }

    @Override
    protected void execute(CommandSender sender, String label, String[] args) {
        Player player = playerOnly(sender);
        requirePermission(sender, "core.itemdb");
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.isEmpty()) {
            throw fail("general.no-item-in-hand");
        }
        ItemMeta meta = item.getItemMeta();
        String material = item.getType().getKey().asString();
        String giveString = material + meta.getAsComponentString();

        send(player, "gameplay.itemdb.info",
                Placeholder.component("item", item.effectiveName()),
                placeholder("material", material),
                placeholder("amount", item.getAmount()),
                placeholder("max_stack", item.getMaxStackSize()),
                Placeholder.component("durability", durability(item, meta)),
                Placeholder.component("custom_name", meta.hasCustomName() ? meta.customName() : none()),
                Placeholder.component("enchantments", enchantments(meta)),
                Placeholder.component("model_data", modelData(meta)),
                Placeholder.component("give", messages().get("gameplay.itemdb.copy")
                        .clickEvent(ClickEvent.copyToClipboard(giveString))
                        .hoverEvent(HoverEvent.showText(Component.text(giveString)))));
    }

    private Component durability(ItemStack item, ItemMeta meta) {
        if (!(meta instanceof Damageable damageable)) {
            return none();
        }
        int max = damageable.hasMaxDamage() ? damageable.getMaxDamage() : item.getType().getMaxDurability();
        if (max <= 0) {
            return none();
        }
        if (damageable.isUnbreakable()) {
            return messages().get("gameplay.itemdb.unbreakable");
        }
        return Component.text((max - damageable.getDamage()) + "/" + max);
    }

    private Component enchantments(ItemMeta meta) {
        Map<Enchantment, Integer> enchants = meta instanceof EnchantmentStorageMeta book
                ? book.getStoredEnchants() : meta.getEnchants();
        if (enchants.isEmpty()) {
            return none();
        }
        List<Component> parts = new ArrayList<>(enchants.size());
        enchants.forEach((enchantment, level) -> parts.add(enchantment.displayName(level)));
        return Component.join(JoinConfiguration.commas(true), parts);
    }

    private Component modelData(ItemMeta meta) {
        if (!meta.hasCustomModelDataComponent()) {
            return none();
        }
        CustomModelDataComponent data = meta.getCustomModelDataComponent();
        List<String> parts = new ArrayList<>(4);
        if (!data.getFloats().isEmpty()) {
            parts.add("floats=" + data.getFloats());
        }
        if (!data.getFlags().isEmpty()) {
            parts.add("flags=" + data.getFlags());
        }
        if (!data.getStrings().isEmpty()) {
            parts.add("strings=" + data.getStrings());
        }
        if (!data.getColors().isEmpty()) {
            List<String> colors = new ArrayList<>(data.getColors().size());
            for (Color color : data.getColors()) {
                colors.add(String.format(Locale.ROOT, "#%06X", color.asRGB()));
            }
            parts.add("colors=" + colors);
        }
        return parts.isEmpty() ? none() : Component.text(String.join(", ", parts));
    }

    private Component none() {
        return messages().get("gameplay.itemdb.none");
    }
}
