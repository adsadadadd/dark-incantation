package me.x1no4ekk.core.warp.gui;

import me.x1no4ekk.core.CorePlugin;
import me.x1no4ekk.core.config.MenuItem;
import me.x1no4ekk.core.gui.Gui;
import me.x1no4ekk.core.gui.PaginatedGui;
import me.x1no4ekk.core.util.ItemBuilder;
import me.x1no4ekk.core.warp.Warp;
import me.x1no4ekk.core.warp.WarpModule;
import me.x1no4ekk.core.warp.WarpPlaceholders;
import me.x1no4ekk.core.warp.WarpSettings;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Shared layout of the warp lists: filler, back button, empty marker, page buttons and warp icons.
 * Entries are immutable {@link Warp} snapshots taken when the menu is opened.
 */
abstract class WarpListGui extends PaginatedGui<Warp> {

    protected final CorePlugin plugin;
    protected final WarpModule module;
    protected final WarpSettings config;
    protected final WarpSettings.ListGui layout;
    private final Set<Integer> contentSlots;

    WarpListGui(CorePlugin plugin, WarpModule module, WarpSettings config, WarpSettings.ListGui layout,
                Component title, List<Integer> contentSlots) {
        super(layout.rows(), title, contentSlots);
        this.plugin = plugin;
        this.module = module;
        this.config = config;
        this.layout = layout;
        this.contentSlots = new HashSet<>(contentSlots);
    }

    @Override
    protected void decorate() {
        if (layout.filler() != null) {
            ItemStack filler = ItemBuilder.of(layout.filler()).hideTooltip().build();
            for (int slot = 0; slot < inventory.getSize(); slot++) {
                if (!contentSlots.contains(slot)) {
                    setItem(slot, filler);
                }
            }
        }
        setItem(layout.back().slot(), layout.back().build(), (player, event) ->
                Gui.nextTick(player, online -> new WarpGui(plugin, module, online).open(online)));
    }

    @Override
    protected void renderEmpty() {
        setItem(layout.empty().slot(), layout.empty().build());
    }

    @Override
    protected ItemStack icon(Warp warp) {
        MenuItem template = layout.entry();
        Material material = warp.icon() != null ? warp.icon() : defaultMaterial(warp);
        ItemStack item = template.build(material,
                WarpPlaceholders.of(plugin, warp, module.manager().rating(warp.key()), config));
        if (material == Material.PLAYER_HEAD && warp.icon() == null && warp.owner() != null) {
            String ownerName = warp.ownerName();
            item.editMeta(SkullMeta.class, meta -> meta.setPlayerProfile(Bukkit.createProfile(warp.owner(), ownerName)));
        }
        return item;
    }

    /**
     * Icon material of a warp without a custom icon.
     */
    protected Material defaultMaterial(Warp warp) {
        return layout.entry().material();
    }

    @Override
    protected MenuItem previousButton() {
        return layout.previous();
    }

    @Override
    protected MenuItem nextButton() {
        return layout.next();
    }
}
