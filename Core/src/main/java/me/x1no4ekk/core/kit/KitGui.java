package me.x1no4ekk.core.kit;

import me.x1no4ekk.core.CorePlugin;
import me.x1no4ekk.core.config.MenuItem;
import me.x1no4ekk.core.gui.Gui;
import me.x1no4ekk.core.hook.EconomyHook;
import me.x1no4ekk.core.user.User;
import me.x1no4ekk.core.util.Durations;
import me.x1no4ekk.core.util.ItemBuilder;
import me.x1no4ekk.core.util.Text;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Kit menu: every kit with its free-claim status at the moment the menu was opened. A click opens the preview.
 * Holds only immutable kit records and the configuration snapshot.
 */
final class KitGui extends Gui {

    private KitGui(CorePlugin plugin, KitModule module, KitSettings settings, Player viewer, User user) {
        super(settings.menu().rows(), Text.parse(settings.menu().title()));
        KitSettings.KitMenu menu = settings.menu();
        if (menu.filler() != null) {
            ItemStack filler = menu.filler().build();
            for (int slot : menu.filler().slots()) {
                setItem(slot, filler);
            }
        }
        long now = System.currentTimeMillis();
        EconomyHook economy = plugin.hooks().economy();
        for (Map.Entry<String, Integer> entry : settings.kitSlots().entrySet()) {
            Kit kit = settings.kits().get(entry.getKey());
            KitStatus.State state = module.service().state(viewer, user, kit, now);
            setItem(entry.getValue(), icon(menu, kit, state, economy), (player, event) ->
                    Gui.nextTick(player, online -> KitContentGui.open(plugin, module, online, kit.id())));
        }
        MenuItem close = menu.close();
        if (close != null) {
            for (int slot : close.slots()) {
                setItem(slot, close.build(), (player, event) -> Gui.closeLater(player));
            }
        }
    }

    /**
     * Opens the kit menu, or reports that the player's data is not loaded yet.
     */
    static void open(CorePlugin plugin, KitModule module, Player player) {
        User user = plugin.userManager().getUser(player);
        if (user == null) {
            plugin.messages().send(player, "general.data-not-loaded");
            return;
        }
        new KitGui(plugin, module, module.settings(), player, user).open(player);
    }

    private static ItemStack icon(KitSettings.KitMenu menu, Kit kit, KitStatus.State state, EconomyHook economy) {
        TagResolver resolvers = resolvers(kit, state, economy);
        List<String> lore = new ArrayList<>(kit.iconLore());
        lore.addAll(menu.status().of(state.status()));
        if (kit.isBuyable()) {
            lore.addAll(menu.buyLore());
        }
        lore.addAll(menu.clickLore());
        return ItemBuilder.of(kit.iconMaterial())
                .name(kit.iconName(), resolvers)
                .lore(lore, resolvers)
                .glint(kit.iconGlint())
                .build();
    }

    /**
     * Placeholders shared by kit icons and preview buttons: {@code <kit> <time> <price> <cooldown>}.
     */
    static TagResolver resolvers(Kit kit, KitStatus.State state, EconomyHook economy) {
        return TagResolver.resolver(
                KitService.kitPlaceholder(kit),
                Placeholder.unparsed("time", Durations.format(state.remainingMillis())),
                Placeholder.unparsed("price", economy.format(kit.price())),
                Placeholder.unparsed("cooldown", kit.cooldownSeconds() > 0 ? Durations.formatSeconds(kit.cooldownSeconds()) : "—"));
    }
}
