package me.x1no4ekk.core.warp.gui;

import me.x1no4ekk.core.CorePlugin;
import me.x1no4ekk.core.gui.Gui;
import me.x1no4ekk.core.util.Text;
import me.x1no4ekk.core.warp.Warp;
import me.x1no4ekk.core.warp.WarpModule;
import me.x1no4ekk.core.warp.WarpSettings;
import me.x1no4ekk.core.warp.WarpSort;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * All player warps, sortable by rating, visits or age. Left click teleports, right click opens the grade menu.
 */
public final class PlayersWarpGui extends WarpListGui {

    private final WarpSettings.PlayersGui players;
    private final List<Warp> warps;
    private WarpSort sort;

    private PlayersWarpGui(CorePlugin plugin, WarpModule module, WarpSettings config, WarpSort sort, int page) {
        super(plugin, module, config, config.players().list(), Text.parse(config.players().list().title()),
                config.players().list().contentSlots());
        this.players = config.players();
        this.warps = List.copyOf(module.manager().playerWarps());
        this.sort = sort;
        setEntries(sorted());
        if (page > 0) {
            setPage(page);
        }
    }

    /**
     * @param sort initial order, or {@code null} for the configured default
     * @param page zero-based page to show
     */
    public static PlayersWarpGui create(CorePlugin plugin, WarpModule module, WarpSort sort, int page) {
        WarpSettings config = module.settings();
        return new PlayersWarpGui(plugin, module, config, sort == null ? config.players().defaultSort() : sort, page);
    }

    private List<Warp> sorted() {
        List<Warp> list = new ArrayList<>(warps);
        list.sort(sort.comparator(module.manager()::rating));
        return list;
    }

    @Override
    protected void decorate() {
        super.decorate();
        setItem(players.sort().slot(), players.sort().build(
                        Placeholder.unparsed("sort", players.sortNames().get(sort)),
                        Placeholder.unparsed("next", players.sortNames().get(sort.next()))),
                (player, event) -> {
                    sort = sort.next();
                    setEntries(sorted());
                });
    }

    @Override
    protected Material defaultMaterial(Warp warp) {
        return players.ownerHead() && warp.owner() != null ? Material.PLAYER_HEAD : super.defaultMaterial(warp);
    }

    @Override
    protected void onEntryClick(Player player, Warp warp, InventoryClickEvent event) {
        if (event.getClick().isRightClick()) {
            if (warp.isOwnedBy(player.getUniqueId())) {
                plugin.messages().send(player, "warp.grade-own");
                return;
            }
            WarpSort currentSort = sort;
            int currentPage = page();
            Gui.nextTick(player, online ->
                    new WarpGradeGui(plugin, module, warp, currentSort, currentPage).open(online));
            return;
        }
        if (event.getClick().isLeftClick()) {
            Gui.nextTick(player, online -> {
                online.closeInventory();
                module.manager().teleport(online, warp);
            });
        }
    }
}
