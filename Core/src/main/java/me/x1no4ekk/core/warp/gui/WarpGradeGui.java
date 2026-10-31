package me.x1no4ekk.core.warp.gui;

import me.x1no4ekk.core.CorePlugin;
import me.x1no4ekk.core.config.MenuItem;
import me.x1no4ekk.core.gui.Gui;
import me.x1no4ekk.core.util.ItemBuilder;
import me.x1no4ekk.core.util.Text;
import me.x1no4ekk.core.warp.Warp;
import me.x1no4ekk.core.warp.WarpModule;
import me.x1no4ekk.core.warp.WarpPlaceholders;
import me.x1no4ekk.core.warp.WarpSettings;
import me.x1no4ekk.core.warp.WarpSort;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

/**
 * Rates a player warp from 1 to 5. Back returns to the players' warps on the same sort and page.
 */
public final class WarpGradeGui extends Gui {

    public WarpGradeGui(CorePlugin plugin, WarpModule module, Warp warp, WarpSort sort, int page) {
        this(plugin, module, module.settings(), warp, sort, page);
    }

    private WarpGradeGui(CorePlugin plugin, WarpModule module, WarpSettings config, Warp warp, WarpSort sort, int page) {
        super(config.grade().rows(), Text.parse(config.grade().title(), Placeholder.unparsed("warp", warp.name())));
        WarpSettings.GradeGui layout = config.grade();

        MenuItem info = layout.info();
        Material material = warp.icon() != null ? warp.icon()
                : config.players().ownerHead() && warp.owner() != null ? Material.PLAYER_HEAD : info.material();
        ItemStack infoItem = info.build(material,
                WarpPlaceholders.of(plugin, warp, module.manager().rating(warp.key()), config));
        if (material == Material.PLAYER_HEAD && warp.icon() == null && warp.owner() != null) {
            String ownerName = warp.ownerName();
            infoItem.editMeta(SkullMeta.class, meta -> meta.setPlayerProfile(Bukkit.createProfile(warp.owner(), ownerName)));
        }
        setItem(info.slot(), infoItem);

        for (int index = 0; index < layout.grades().size(); index++) {
            int grade = index + 1;
            MenuItem button = layout.grades().get(index);
            setItem(button.slot(), button.build(Placeholder.unparsed("grade", String.valueOf(grade)),
                    Placeholder.unparsed("warp", warp.name())), (player, event) -> {
                Gui.closeLater(player);
                module.manager().grade(player, warp, grade);
            });
        }
        setItem(layout.back().slot(), layout.back().build(), (player, event) ->
                Gui.nextTick(player, online -> PlayersWarpGui.create(plugin, module, sort, page).open(online)));
        if (layout.filler() != null) {
            fillEmpty(ItemBuilder.of(layout.filler()).hideTooltip().build());
        }
    }
}
