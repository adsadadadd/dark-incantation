package me.x1no4ekk.core.warp.gui;

import me.x1no4ekk.core.CorePlugin;
import me.x1no4ekk.core.gui.Gui;
import me.x1no4ekk.core.util.Text;
import me.x1no4ekk.core.warp.Warp;
import me.x1no4ekk.core.warp.WarpModule;
import me.x1no4ekk.core.warp.WarpSettings;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * System warps. Warps with a slot are pinned to it on every page; the others fill the content slots.
 */
public final class SystemWarpGui extends WarpListGui {

    private final List<Warp> pinned;

    public SystemWarpGui(CorePlugin plugin, WarpModule module) {
        this(plugin, module, module.settings(), module.manager().systemWarps());
    }

    private SystemWarpGui(CorePlugin plugin, WarpModule module, WarpSettings config, List<Warp> warps) {
        this(plugin, module, config, pinned(warps, config.system().rows()), warps);
    }

    private SystemWarpGui(CorePlugin plugin, WarpModule module, WarpSettings config, List<Warp> pinned, List<Warp> warps) {
        super(plugin, module, config, config.system(), Text.parse(config.system().title()),
                freeSlots(config.system(), pinned));
        this.pinned = pinned;
        List<Warp> rest = new ArrayList<>(warps);
        rest.removeAll(pinned);
        setEntries(rest);
    }

    private static List<Warp> pinned(List<Warp> warps, int rows) {
        Set<Integer> used = new HashSet<>();
        List<Warp> result = new ArrayList<>();
        for (Warp warp : warps) {
            if (warp.slot() >= 0 && warp.slot() < rows * 9 && used.add(warp.slot())) {
                result.add(warp);
            }
        }
        return List.copyOf(result);
    }

    private static List<Integer> freeSlots(WarpSettings.ListGui layout, List<Warp> pinned) {
        List<Integer> slots = new ArrayList<>(layout.contentSlots());
        for (Warp warp : pinned) {
            slots.remove(Integer.valueOf(warp.slot()));
        }
        return slots.isEmpty() ? layout.contentSlots() : slots;
    }

    @Override
    protected void decorate() {
        super.decorate();
        for (Warp warp : pinned) {
            setItem(warp.slot(), icon(warp), (player, event) -> onEntryClick(player, warp, event));
        }
    }

    @Override
    protected void renderEmpty() {
        if (pinned.isEmpty()) {
            super.renderEmpty();
        }
    }

    @Override
    protected void onEntryClick(Player player, Warp warp, InventoryClickEvent event) {
        Gui.nextTick(player, online -> {
            online.closeInventory();
            module.manager().teleport(online, warp);
        });
    }
}
