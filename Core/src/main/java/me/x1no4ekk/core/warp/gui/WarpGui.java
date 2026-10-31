package me.x1no4ekk.core.warp.gui;

import me.x1no4ekk.core.CorePlugin;
import me.x1no4ekk.core.gui.Gui;
import me.x1no4ekk.core.util.ItemBuilder;
import me.x1no4ekk.core.util.Text;
import me.x1no4ekk.core.warp.WarpManager;
import me.x1no4ekk.core.warp.WarpModule;
import me.x1no4ekk.core.warp.WarpSettings;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.entity.Player;

/**
 * Main /warp menu: system warps, own warps and players' warps.
 */
public final class WarpGui extends Gui {

    public WarpGui(CorePlugin plugin, WarpModule module, Player viewer) {
        this(plugin, module, module.settings(), viewer);
    }

    private WarpGui(CorePlugin plugin, WarpModule module, WarpSettings config, Player viewer) {
        super(config.main().rows(), Text.parse(config.main().title()));
        WarpSettings.MainGui layout = config.main();
        WarpManager manager = module.manager();
        int limit = manager.limit(viewer);

        setItem(layout.system().slot(), layout.system().build(
                        Placeholder.unparsed("count", String.valueOf(manager.systemWarps().size()))),
                (player, event) -> Gui.nextTick(player, online -> new SystemWarpGui(plugin, module).open(online)));
        setItem(layout.mine().slot(), layout.mine().build(
                        Placeholder.unparsed("count", String.valueOf(manager.countOwned(viewer.getUniqueId()))),
                        Placeholder.unparsed("limit", limit == Integer.MAX_VALUE ? "∞" : String.valueOf(limit))),
                (player, event) -> Gui.nextTick(player, online -> new MyWarpGui(plugin, module, online).open(online)));
        setItem(layout.players().slot(), layout.players().build(
                        Placeholder.unparsed("count", String.valueOf(manager.playerWarps().size()))),
                (player, event) -> Gui.nextTick(player, online ->
                        PlayersWarpGui.create(plugin, module, null, 0).open(online)));
        setItem(layout.close().slot(), layout.close().build(), (player, event) -> Gui.closeLater(player));
        if (layout.filler() != null) {
            fillEmpty(ItemBuilder.of(layout.filler()).hideTooltip().build());
        }
    }
}
