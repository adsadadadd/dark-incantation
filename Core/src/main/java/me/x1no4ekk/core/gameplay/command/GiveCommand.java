package me.x1no4ekk.core.gameplay.command;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import me.x1no4ekk.core.CorePlugin;
import me.x1no4ekk.core.command.BaseCommand;
import me.x1no4ekk.core.gameplay.GameplayModule;
import me.x1no4ekk.core.util.Inventories;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

/**
 * /give &lt;player&gt; &lt;item&gt; [amount] — the item uses vanilla syntax including data components, e.g.
 * {@code diamond_sword[enchantments={sharpness:5}]}. Without an amount a full stack is given.
 */
public final class GiveCommand extends BaseCommand {

    private final GameplayModule module;

    public GiveCommand(CorePlugin plugin, GameplayModule module) {
        super(plugin);
        this.module = module;
    }

    @Override
    protected void execute(CommandSender sender, String label, String[] args) {
        requirePermission(sender, "core.give");
        if (args.length < 2) {
            throw usage("gameplay.give.usage");
        }
        Player target = player(sender, args[0]);
        int maxAmount = module.settings().giveMaxAmount();
        // Components may contain spaces, so everything between the player and a trailing number is the item.
        int itemEnd = args.length;
        int amount = -1;
        if (args.length >= 3 && isInteger(args[args.length - 1])) {
            amount = parseInt(args[args.length - 1], 1, maxAmount);
            itemEnd--;
        }
        String itemInput = String.join(" ", Arrays.copyOfRange(args, 1, itemEnd));

        ItemStack prototype;
        try {
            prototype = Bukkit.getItemFactory().createItemStack(itemInput);
        } catch (IllegalArgumentException e) {
            throw fail("gameplay.give.invalid-item", placeholder("item", itemInput), placeholder("error", String.valueOf(e.getMessage())));
        }
        if (prototype.isEmpty()) {
            throw fail("gameplay.give.invalid-item", placeholder("item", itemInput), placeholder("error", "air"));
        }
        int stackSize = prototype.getMaxStackSize();
        if (amount < 0) {
            amount = Math.min(stackSize, maxAmount);
        }

        List<ItemStack> stacks = new ArrayList<>();
        for (int left = amount; left > 0; left -= stackSize) {
            stacks.add(prototype.asQuantity(Math.min(stackSize, left)));
        }
        boolean dropped = Inventories.giveOrDrop(target, stacks);

        TagResolver item = Placeholder.component("item", prototype.effectiveName());
        TagResolver count = placeholder("amount", amount);
        if (!target.equals(sender)) {
            send(sender, "gameplay.give.given", placeholder("player", target.getName()), item, count);
        }
        send(target, "gameplay.give.received", item, count);
        if (dropped) {
            send(target, "gameplay.dropped");
        }
    }

    private static boolean isInteger(String raw) {
        if (raw.isEmpty()) {
            return false;
        }
        for (int i = 0; i < raw.length(); i++) {
            if (!Character.isDigit(raw.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected List<String> complete(CommandSender sender, String label, String[] args) {
        if (!sender.hasPermission("core.give")) {
            return List.of();
        }
        if (args.length == 1) {
            return players(sender, args[0]);
        }
        if (args.length == 2) {
            if (args[1].indexOf('[') >= 0) {
                return List.of();
            }
            return filter(RegistryAccess.registryAccess().getRegistry(RegistryKey.ITEM).keyStream()
                    .filter(key -> !key.getKey().equals("air"))
                    .map(key -> NamespacedKey.MINECRAFT.equals(key.getNamespace()) ? key.getKey() : key.asString())
                    .sorted(), args[1]);
        }
        if (args.length == 3 && args[1].indexOf('[') < 0) {
            return filter(Stream.of("1", "16", "32", "64"), args[2]);
        }
        return List.of();
    }
}
