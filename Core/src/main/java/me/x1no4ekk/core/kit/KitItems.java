package me.x1no4ekk.core.kit;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import me.x1no4ekk.core.config.ConfigUtil;
import me.x1no4ekk.core.util.Text;
import net.kyori.adventure.key.InvalidKeyException;
import net.kyori.adventure.key.Key;
import org.bukkit.Material;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Kit item parsing. A list entry is either a serialized {@link ItemStack} (written by {@code /kit create}) or a
 * hand-written map:
 * <pre>
 * - material: DIAMOND_SWORD
 *   amount: 1
 *   name: '&lt;aqua&gt;Меч'
 *   lore: ['&lt;gray&gt;Описание']
 *   enchantments: {sharpness: 5, unbreaking: 3}
 * </pre>
 */
final class KitItems {

    private KitItems() {
    }

    static List<ItemStack> parse(List<?> raw, String path) {
        List<ItemStack> items = new ArrayList<>(raw.size());
        for (int i = 0; i < raw.size(); i++) {
            Object entry = raw.get(i);
            String entryPath = path + "[" + i + "]";
            ItemStack item = switch (entry) {
                case ItemStack stack -> stack.clone();
                case Map<?, ?> map -> fromMap(map, entryPath);
                case null, default -> throw new IllegalArgumentException(entryPath
                        + ": expected an item (map with 'material' or a serialized ItemStack)");
            };
            if (item.isEmpty()) {
                throw new IllegalArgumentException(entryPath + ": the item is empty or its material is unknown");
            }
            items.add(item);
        }
        return List.copyOf(items);
    }

    private static ItemStack fromMap(Map<?, ?> map, String path) {
        Object rawMaterial = map.get("material");
        Material material = ConfigUtil.material(rawMaterial == null ? null : rawMaterial.toString(), path + ".material");
        int amount = 1;
        Object rawAmount = map.get("amount");
        if (rawAmount != null) {
            try {
                amount = Integer.parseInt(rawAmount.toString().trim());
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(path + ".amount: '" + rawAmount + "' is not a number");
            }
            if (amount < 1 || amount > 99) {
                throw new IllegalArgumentException(path + ".amount must be between 1 and 99");
            }
        }
        ItemStack item = ItemStack.of(material, amount);

        Object name = map.get("name");
        Object lore = map.get("lore");
        if (name != null || lore instanceof List<?>) {
            item.editMeta(meta -> {
                if (name != null) {
                    meta.displayName(Text.item(name.toString()));
                }
                if (lore instanceof List<?> lines && !lines.isEmpty()) {
                    meta.lore(Text.itemLines(lines.stream().map(String::valueOf).toList()));
                }
            });
        }

        if (map.get("enchantments") instanceof Map<?, ?> enchantments) {
            Registry<Enchantment> registry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT);
            for (Map.Entry<?, ?> entry : enchantments.entrySet()) {
                String key = String.valueOf(entry.getKey()).trim().toLowerCase(Locale.ROOT);
                String enchantPath = path + ".enchantments." + key;
                Enchantment enchantment;
                try {
                    enchantment = registry.get(Key.key(key));
                } catch (InvalidKeyException e) {
                    enchantment = null;
                }
                if (enchantment == null) {
                    throw new IllegalArgumentException(enchantPath + ": unknown enchantment");
                }
                int level;
                try {
                    level = Integer.parseInt(String.valueOf(entry.getValue()).trim());
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException(enchantPath + ": level must be a number");
                }
                if (level < 1 || level > 255) {
                    throw new IllegalArgumentException(enchantPath + ": level must be between 1 and 255");
                }
                Enchantment finalEnchantment = enchantment;
                if (item.getItemMeta() instanceof EnchantmentStorageMeta) {
                    item.editMeta(EnchantmentStorageMeta.class, meta -> meta.addStoredEnchant(finalEnchantment, level, true));
                } else {
                    item.addUnsafeEnchantment(finalEnchantment, level);
                }
            }
        } else if (map.containsKey("enchantments") && map.get("enchantments") != null) {
            throw new IllegalArgumentException(path + ".enchantments must be a map like {sharpness: 5}");
        }
        return item;
    }
}
