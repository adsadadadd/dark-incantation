package me.x1no4ekk.core.kit;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.Equippable;
import me.x1no4ekk.core.CorePlugin;
import me.x1no4ekk.core.config.Messages;
import me.x1no4ekk.core.hook.EconomyHook;
import me.x1no4ekk.core.user.User;
import me.x1no4ekk.core.util.Durations;
import me.x1no4ekk.core.util.Inventories;
import me.x1no4ekk.core.util.Text;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Claiming, buying and giving kits; cooldowns are cached in {@link User} and written through to core_kit_cooldowns.
 */
public final class KitService {

    static final String ADMIN_PERMISSION = "core.kit.admin";
    static final String BYPASS_COOLDOWN_PERMISSION = "core.kit.bypass-cooldown";

    private static final String UPSERT_COOLDOWN =
            "INSERT INTO core_kit_cooldowns (uuid, kit, last_used) VALUES (?, ?, ?) "
                    + "ON DUPLICATE KEY UPDATE last_used = VALUES(last_used)";
    private static final String DELETE_KIT_COOLDOWNS = "DELETE FROM core_kit_cooldowns WHERE kit = ?";

    private enum GiveResult { GIVEN, DROPPED, FULL }

    private final CorePlugin plugin;
    private final KitModule module;

    KitService(CorePlugin plugin, KitModule module) {
        this.plugin = plugin;
        this.module = module;
    }

    private Messages messages() {
        return plugin.messages();
    }

    static TagResolver kitPlaceholder(Kit kit) {
        return Placeholder.component("kit", Text.parse(kit.displayName()));
    }

    /**
     * Free-claim status of a kit for a player.
     */
    KitStatus.State state(Player player, User user, Kit kit, long now) {
        if (!kit.free()) {
            return new KitStatus.State(KitStatus.PURCHASE_ONLY, 0L);
        }
        if (kit.requiresPermission() && !player.hasPermission(kit.permission())) {
            return new KitStatus.State(KitStatus.NO_PERMISSION, 0L);
        }
        Long lastUsed = user.getKitLastUsed(kit.id());
        // A non-positive timestamp marks a cooldown reset by /kit delete (User has no removal method).
        if (lastUsed == null || lastUsed <= 0L || kit.cooldownSeconds() == 0L
                || player.hasPermission(BYPASS_COOLDOWN_PERMISSION)) {
            return KitStatus.State.AVAILABLE_STATE;
        }
        if (kit.isOneTime()) {
            return new KitStatus.State(KitStatus.USED, 0L);
        }
        long remaining = lastUsed + kit.cooldownSeconds() * 1000L - now;
        return remaining > 0 ? new KitStatus.State(KitStatus.COOLDOWN, remaining) : KitStatus.State.AVAILABLE_STATE;
    }

    /**
     * Free claim: permission, cooldown and inventory space are checked; messages are sent.
     *
     * @return {@code true} if the kit was given
     */
    boolean claim(Player player, Kit kit) {
        User user = plugin.userManager().getUser(player);
        if (user == null) {
            return fail(player, "general.data-not-loaded");
        }
        KitStatus.State state = state(player, user, kit, System.currentTimeMillis());
        TagResolver kitName = kitPlaceholder(kit);
        switch (state.status()) {
            case PURCHASE_ONLY -> {
                return failWithBuyHint(player, kit, "kit.purchase-only", kitName);
            }
            case NO_PERMISSION -> {
                return failWithBuyHint(player, kit, "kit.no-permission", kitName);
            }
            case USED -> {
                return failWithBuyHint(player, kit, "kit.one-time", kitName);
            }
            case COOLDOWN -> {
                return failWithBuyHint(player, kit, "kit.cooldown", kitName,
                        Placeholder.unparsed("time", Durations.format(state.remainingMillis())));
            }
            case AVAILABLE -> {
                // handled below
            }
        }
        if (kit.items().isEmpty()) {
            return fail(player, "kit.empty", kitName);
        }
        GiveResult result = giveItems(player, kit, module.settings().dropWhenFull());
        if (result == GiveResult.FULL) {
            return fail(player, "general.inventory-full");
        }
        markUsed(player.getUniqueId(), user, kit);
        messages().send(player, "kit.claimed", kitName);
        afterGive(player, result);
        return true;
    }

    /**
     * Purchase for the kit price. Ignores the claim permission and cooldown and does not start a cooldown.
     *
     * @return {@code true} if the kit was bought
     */
    boolean buy(Player player, Kit kit) {
        TagResolver kitName = kitPlaceholder(kit);
        if (!kit.isBuyable()) {
            return fail(player, "kit.not-for-sale", kitName);
        }
        if (kit.items().isEmpty()) {
            return fail(player, "kit.empty", kitName);
        }
        EconomyHook economy = plugin.hooks().economy();
        if (!economy.isAvailable()) {
            return fail(player, "general.economy-unavailable");
        }
        TagResolver price = Placeholder.unparsed("price", economy.format(kit.price()));
        if (!economy.has(player, kit.price())) {
            return fail(player, "general.not-enough-money", price);
        }
        boolean drop = module.settings().dropWhenFull();
        if (!drop && !Inventories.canFit(player, itemsForInventory(player, kit))) {
            return fail(player, "general.inventory-full");
        }
        if (!economy.withdraw(player, kit.price())) {
            return fail(player, "general.not-enough-money", price);
        }
        GiveResult result = giveItems(player, kit, true);
        messages().send(player, "kit.bought", kitName, price);
        afterGive(player, result);
        return true;
    }

    /**
     * Administrative give: no permission, price or cooldown checks; leftovers are dropped.
     */
    void give(CommandSender sender, Player target, Kit kit) {
        TagResolver kitName = kitPlaceholder(kit);
        GiveResult result = giveItems(target, kit, true);
        messages().send(sender, "kit.given", kitName, Placeholder.unparsed("player", target.getName()));
        if (!target.equals(sender)) {
            messages().send(target, "kit.received", kitName);
        }
        afterGive(target, result);
    }

    /**
     * Gives every start kit to a player who joined for the first time.
     */
    void giveStartKits(Player player, User user) {
        for (Kit kit : module.settings().kits().values()) {
            if (!kit.start() || kit.items().isEmpty()) {
                continue;
            }
            GiveResult result = giveItems(player, kit, true);
            if (kit.free() && kit.cooldownSeconds() != 0L) {
                markUsed(player.getUniqueId(), user, kit);
            }
            messages().send(player, "kit.start-received", kitPlaceholder(kit));
            if (result == GiveResult.DROPPED) {
                messages().send(player, "kit.dropped");
            }
        }
    }

    /**
     * Resets cooldowns of a deleted kit in MySQL and in the cache of online players.
     */
    void forgetKit(String kitId) {
        for (User user : plugin.userManager().getUsers()) {
            if (user.getKitLastUsed(kitId) != null) {
                user.setKitLastUsed(kitId, 0L);
            }
        }
        plugin.database().update(connection -> {
            try (PreparedStatement statement = connection.prepareStatement(DELETE_KIT_COOLDOWNS)) {
                statement.setString(1, kitId);
                statement.executeUpdate();
            }
        }).whenComplete((ignored, error) -> {
            if (error != null) {
                plugin.getLogger().log(Level.SEVERE, "Failed to delete cooldowns of kit '" + kitId + "'", error);
            }
        });
    }

    private void markUsed(UUID uuid, User user, Kit kit) {
        long now = System.currentTimeMillis();
        user.setKitLastUsed(kit.id(), now);
        String kitId = kit.id();
        plugin.database().update(connection -> {
            try (PreparedStatement statement = connection.prepareStatement(UPSERT_COOLDOWN)) {
                statement.setString(1, uuid.toString());
                statement.setString(2, kitId);
                statement.setLong(3, now);
                statement.executeUpdate();
            }
        }).whenCompleteAsync((ignored, error) -> {
            if (error != null) {
                plugin.getLogger().log(Level.SEVERE, "Failed to save cooldown of kit '" + kitId + "' for " + uuid, error);
                Player online = Bukkit.getPlayer(uuid);
                if (online != null) {
                    messages().send(online, "general.database-error");
                }
            }
        }, plugin.mainThread());
    }

    private void afterGive(Player player, GiveResult result) {
        if (result == GiveResult.DROPPED) {
            messages().send(player, "kit.dropped");
        }
        module.settings().claimSound().play(player);
    }

    private boolean fail(Player player, String key, TagResolver... resolvers) {
        messages().send(player, key, resolvers);
        module.settings().errorSound().play(player);
        return false;
    }

    private boolean failWithBuyHint(Player player, Kit kit, String key, TagResolver... resolvers) {
        fail(player, key, resolvers);
        if (kit.isBuyable() && plugin.hooks().economy().isAvailable()) {
            messages().send(player, "kit.buy-hint",
                    Placeholder.unparsed("price", plugin.hooks().economy().format(kit.price())),
                    Placeholder.unparsed("name", kit.id()));
        }
        return false;
    }

    /**
     * Items that will go to the storage slots (armor pieces that will be equipped are excluded).
     */
    private List<ItemStack> itemsForInventory(Player player, Kit kit) {
        List<ItemStack> rest = new ArrayList<>();
        splitEquipment(player, kit, rest);
        return rest;
    }

    private Map<EquipmentSlot, ItemStack> splitEquipment(Player player, Kit kit, List<ItemStack> rest) {
        Map<EquipmentSlot, ItemStack> equip = new EnumMap<>(EquipmentSlot.class);
        PlayerInventory inventory = player.getInventory();
        for (ItemStack item : kit.copyItems()) {
            EquipmentSlot slot = kit.autoEquip() ? armorSlot(item) : null;
            ItemStack current = slot == null ? null : inventory.getItem(slot);
            if (slot != null && !equip.containsKey(slot) && (current == null || current.isEmpty())) {
                equip.put(slot, item);
            } else {
                rest.add(item);
            }
        }
        return equip;
    }

    private GiveResult giveItems(Player player, Kit kit, boolean allowDrop) {
        List<ItemStack> rest = new ArrayList<>();
        Map<EquipmentSlot, ItemStack> equip = splitEquipment(player, kit, rest);
        if (!allowDrop && !Inventories.canFit(player, rest)) {
            return GiveResult.FULL;
        }
        PlayerInventory inventory = player.getInventory();
        equip.forEach(inventory::setItem);
        return Inventories.giveOrDrop(player, rest) ? GiveResult.DROPPED : GiveResult.GIVEN;
    }

    private static EquipmentSlot armorSlot(ItemStack item) {
        Equippable equippable = item.getData(DataComponentTypes.EQUIPPABLE);
        if (equippable == null) {
            return null;
        }
        EquipmentSlot slot = equippable.slot();
        return switch (slot) {
            case HEAD, CHEST, LEGS, FEET -> slot;
            default -> null;
        };
    }
}
