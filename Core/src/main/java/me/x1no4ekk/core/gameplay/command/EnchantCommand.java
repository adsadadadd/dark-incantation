package me.x1no4ekk.core.gameplay.command;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import me.x1no4ekk.core.CorePlugin;
import me.x1no4ekk.core.command.BaseCommand;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.IntStream;

/**
 * /enchant &lt;enchantment&gt; [level] — enchants the held item (enchanted books get a stored enchantment).
 * Level 0 removes the enchantment; levels above the maximum, unsuitable items and conflicts need
 * {@code core.enchant.unsafe}.
 */
public final class EnchantCommand extends BaseCommand {

    private static final String UNSAFE = "core.enchant.unsafe";
    private static final int MAX_LEVEL = 255;

    public EnchantCommand(CorePlugin plugin) {
        super(plugin);
    }

    private static Registry<Enchantment> registry() {
        return RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT);
    }

    @Override
    protected void execute(CommandSender sender, String label, String[] args) {
        Player player = playerOnly(sender);
        requirePermission(sender, "core.enchant");
        if (args.length < 1 || args.length > 2) {
            throw usage("gameplay.enchant.usage");
        }
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.isEmpty()) {
            throw fail("general.no-item-in-hand");
        }
        NamespacedKey key = NamespacedKey.fromString(args[0].toLowerCase(Locale.ROOT));
        Enchantment enchantment = key == null ? null : registry().get(key);
        if (enchantment == null) {
            throw fail("gameplay.enchant.unknown", placeholder("enchantment", args[0]));
        }
        int level = args.length == 2 ? parseInt(args[1], 0, MAX_LEVEL) : 1;
        ItemMeta meta = item.getItemMeta();
        boolean book = meta instanceof EnchantmentStorageMeta;
        TagResolver name = Placeholder.component("enchantment", enchantment.description());

        if (level == 0) {
            boolean removed = meta instanceof EnchantmentStorageMeta storage
                    ? storage.removeStoredEnchant(enchantment) : meta.removeEnchant(enchantment);
            if (!removed) {
                throw fail("gameplay.enchant.not-present", name);
            }
            item.setItemMeta(meta);
            player.getInventory().setItemInMainHand(item);
            send(player, "gameplay.enchant.removed", name);
            return;
        }

        if (!sender.hasPermission(UNSAFE)) {
            if (level > enchantment.getMaxLevel()) {
                throw fail("gameplay.enchant.max-level", name, placeholder("max", enchantment.getMaxLevel()));
            }
            if (!book && !enchantment.canEnchantItem(item)) {
                throw fail("gameplay.enchant.unsuitable", name);
            }
            Map<Enchantment, Integer> current = meta instanceof EnchantmentStorageMeta storage
                    ? storage.getStoredEnchants() : meta.getEnchants();
            for (Enchantment other : current.keySet()) {
                if (!other.equals(enchantment) && enchantment.conflictsWith(other)) {
                    throw fail("gameplay.enchant.conflict", name, Placeholder.component("other", other.description()));
                }
            }
        }
        if (meta instanceof EnchantmentStorageMeta storage) {
            storage.addStoredEnchant(enchantment, level, true);
        } else {
            meta.addEnchant(enchantment, level, true);
        }
        item.setItemMeta(meta);
        player.getInventory().setItemInMainHand(item);
        send(player, "gameplay.enchant.applied", Placeholder.component("enchantment", enchantment.displayName(level)));
    }

    @Override
    protected List<String> complete(CommandSender sender, String label, String[] args) {
        if (!sender.hasPermission("core.enchant")) {
            return List.of();
        }
        if (args.length == 1) {
            List<String> keys = new ArrayList<>();
            registry().keyStream().forEach(key -> keys.add(
                    NamespacedKey.MINECRAFT.equals(key.getNamespace()) ? key.getKey() : key.asString()));
            keys.sort(null);
            return filter(keys, args[0]);
        }
        if (args.length == 2) {
            NamespacedKey key = NamespacedKey.fromString(args[0].toLowerCase(Locale.ROOT));
            Enchantment enchantment = key == null ? null : registry().get(key);
            int max = enchantment == null ? 5 : enchantment.getMaxLevel();
            return filter(IntStream.rangeClosed(0, max).mapToObj(String::valueOf), args[1]);
        }
        return List.of();
    }
}
