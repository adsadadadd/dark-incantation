package me.x1no4ekk.core.kit;

import me.x1no4ekk.core.CorePlugin;
import me.x1no4ekk.core.config.MenuItem;
import me.x1no4ekk.core.gui.Gui;
import me.x1no4ekk.core.gui.PaginatedGui;
import me.x1no4ekk.core.user.User;
import me.x1no4ekk.core.util.ItemBuilder;
import me.x1no4ekk.core.util.Text;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Kit preview: the kit's items over several pages plus the get/buy/back buttons.
 * The kit is re-resolved by id on every button click, so a reload in between is respected.
 */
final class KitContentGui extends PaginatedGui<ItemStack> {

    private final CorePlugin plugin;
    private final KitModule module;
    private final KitSettings.ContentMenu menu;
    private final Kit kit;
    private final KitStatus.State state;

    @FunctionalInterface
    private interface KitAction {
        boolean run(KitService service, Player player, Kit kit);
    }

    private KitContentGui(CorePlugin plugin, KitModule module, KitSettings.ContentMenu menu, Kit kit, KitStatus.State state) {
        super(menu.rows(), Text.parse(menu.title(), KitService.kitPlaceholder(kit)), menu.contentSlots());
        this.plugin = plugin;
        this.module = module;
        this.menu = menu;
        this.kit = kit;
        this.state = state;
        setEntries(kit.items());
    }

    static void open(CorePlugin plugin, KitModule module, Player player, String kitId) {
        User user = plugin.userManager().getUser(player);
        if (user == null) {
            plugin.messages().send(player, "general.data-not-loaded");
            return;
        }
        KitSettings settings = module.settings();
        Kit kit = settings.kit(kitId);
        if (kit == null) {
            plugin.messages().send(player, "kit.not-found", Placeholder.unparsed("name", kitId));
            return;
        }
        KitStatus.State state = module.service().state(player, user, kit, System.currentTimeMillis());
        new KitContentGui(plugin, module, settings.content(), kit, state).open(player);
    }

    @Override
    protected ItemStack icon(ItemStack entry) {
        return entry.clone();
    }

    @Override
    protected void onEntryClick(Player player, ItemStack entry, InventoryClickEvent event) {
        // Preview only.
    }

    @Override
    protected MenuItem previousButton() {
        return menu.previousPage();
    }

    @Override
    protected MenuItem nextButton() {
        return menu.nextPage();
    }

    @Override
    protected void decorate() {
        if (menu.filler() != null) {
            ItemStack filler = menu.filler().build();
            for (int slot : menu.filler().slots()) {
                setItem(slot, filler);
            }
        }
    }

    @Override
    protected void afterRender() {
        TagResolver resolvers = KitGui.resolvers(kit, state, plugin.hooks().economy());
        boolean both = kit.free() && kit.isBuyable();
        if (kit.free()) {
            MenuItem get = menu.get();
            ItemStack icon = ItemBuilder.of(get.material(), get.amount())
                    .name(get.name(), resolvers)
                    .lore(menu.getStatus().of(state.status()), resolvers)
                    .glint(get.glint())
                    .build();
            setItem(both ? get.slot() : menu.getAloneSlot(), icon,
                    (player, event) -> act(player, KitService::claim));
        }
        if (kit.isBuyable()) {
            setItem(both ? menu.buy().slot() : menu.buyAloneSlot(), menu.buy().build(resolvers),
                    (player, event) -> act(player, KitService::buy));
        }
        MenuItem back = menu.back();
        if (back != null) {
            setItem(back.slot(), back.build(resolvers),
                    (player, event) -> Gui.nextTick(player, online -> KitGui.open(plugin, module, online)));
        }
    }

    /**
     * Runs a claim/buy on the next tick and closes the menu if it succeeded.
     */
    private void act(Player clicker, KitAction action) {
        String kitId = kit.id();
        Gui.nextTick(clicker, player -> {
            Kit current = module.settings().kit(kitId);
            if (current == null) {
                plugin.messages().send(player, "kit.not-found", Placeholder.unparsed("name", kitId));
                player.closeInventory();
                return;
            }
            if (action.run(module.service(), player, current)) {
                player.closeInventory();
            }
        });
    }
}
