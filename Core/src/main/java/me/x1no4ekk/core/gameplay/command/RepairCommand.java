package me.x1no4ekk.core.gameplay.command;

import me.x1no4ekk.core.CorePlugin;
import me.x1no4ekk.core.command.BaseCommand;
import me.x1no4ekk.core.gameplay.GameplayModule;
import me.x1no4ekk.core.hook.EconomyHook;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.Damageable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

/**
 * /repair [hand|all] — repairs the held item or the whole inventory (armour and off hand included).
 * Optionally paid via the economy hook; {@code core.repair.free} repairs for free.
 */
public final class RepairCommand extends BaseCommand {

    private final GameplayModule module;

    public RepairCommand(CorePlugin plugin, GameplayModule module) {
        super(plugin);
        this.module = module;
    }

    @Override
    protected void execute(CommandSender sender, String label, String[] args) {
        Player player = playerOnly(sender);
        requirePermission(sender, "core.repair");
        if (args.length > 1) {
            throw usage("gameplay.repair.usage");
        }
        String mode = args.length == 0 ? "hand" : args[0].toLowerCase(Locale.ROOT);
        boolean all = switch (mode) {
            case "hand" -> false;
            case "all" -> true;
            default -> throw usage("gameplay.repair.usage");
        };
        if (all) {
            requirePermission(sender, "core.repair.all");
        }

        PlayerInventory inventory = player.getInventory();
        List<Integer> slots = new ArrayList<>();
        if (all) {
            for (int slot = 0; slot < inventory.getSize(); slot++) {
                if (isDamaged(inventory.getItem(slot))) {
                    slots.add(slot);
                }
            }
            if (slots.isEmpty()) {
                throw fail("gameplay.repair.nothing");
            }
        } else {
            ItemStack hand = inventory.getItemInMainHand();
            if (hand.isEmpty()) {
                throw fail("general.no-item-in-hand");
            }
            if (!(hand.getItemMeta() instanceof Damageable meta) || maxDamage(hand, meta) <= 0) {
                throw fail("gameplay.repair.not-repairable");
            }
            if (!meta.hasDamage() || meta.getDamage() <= 0) {
                throw fail("gameplay.repair.not-damaged");
            }
            slots.add(inventory.getHeldItemSlot());
        }

        double cost = all ? module.settings().repair().allCost() : module.settings().repair().cost();
        boolean paid = cost > 0 && !player.hasPermission("core.repair.free");
        EconomyHook economy = plugin.hooks().economy();
        if (paid) {
            if (!economy.isAvailable()) {
                throw fail("general.economy-unavailable");
            }
            if (!economy.withdraw(player, cost)) {
                throw fail("general.not-enough-money", placeholder("price", economy.format(cost)));
            }
        }

        List<Component> names = new ArrayList<>(slots.size());
        for (int slot : slots) {
            ItemStack item = inventory.getItem(slot);
            if (item != null && item.editMeta(Damageable.class, meta -> meta.setDamage(0))) {
                inventory.setItem(slot, item);
                names.add(item.effectiveName());
            }
        }
        TagResolver items = Placeholder.component("items", Component.join(JoinConfiguration.commas(true), names));
        if (paid) {
            send(player, "gameplay.repair.repaired-paid", items, placeholder("price", economy.format(cost)));
        } else {
            send(player, "gameplay.repair.repaired", items);
        }
    }

    private static boolean isDamaged(ItemStack item) {
        return item != null && !item.isEmpty() && item.getItemMeta() instanceof Damageable meta
                && maxDamage(item, meta) > 0 && meta.hasDamage() && meta.getDamage() > 0;
    }

    private static int maxDamage(ItemStack item, Damageable meta) {
        return meta.hasMaxDamage() ? meta.getMaxDamage() : item.getType().getMaxDurability();
    }

    @Override
    protected List<String> complete(CommandSender sender, String label, String[] args) {
        if (args.length != 1 || !sender.hasPermission("core.repair")) {
            return List.of();
        }
        return filter(sender.hasPermission("core.repair.all") ? Stream.of("hand", "all") : Stream.of("hand"), args[0]);
    }
}
