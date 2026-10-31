package me.x1no4ekk.core.utility.world;

import me.x1no4ekk.core.config.MenuItem;
import me.x1no4ekk.core.gui.Gui;
import me.x1no4ekk.core.gui.GuiAction;
import me.x1no4ekk.core.util.Text;
import me.x1no4ekk.core.utility.UtilityText;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.inventory.ItemStack;

/**
 * {@code /ptime} menu: choose a personal time of day or return to the server time.
 * Holds only the immutable menu layout.
 */
final class PTimeGui extends Gui {

    PTimeGui(WorldModule module, WorldSettings.PersonalMenu<WorldSettings.TimeOption> menu) {
        super(menu.rows(), Text.parse(menu.title()));
        place(menu.filler(), null);
        for (WorldSettings.TimeOption option : menu.options()) {
            ItemStack icon = option.item().build(
                    Placeholder.unparsed("ticks", String.valueOf(option.ticks())),
                    Placeholder.unparsed("clock", UtilityText.clock(option.ticks())),
                    Placeholder.parsed("time", option.label()));
            for (int slot : option.item().slots()) {
                setItem(slot, icon, (player, event) -> {
                    module.setPlayerTime(player, option);
                    if (menu.closeOnClick()) {
                        Gui.closeLater(player);
                    }
                });
            }
        }
        place(menu.reset(), (player, event) -> {
            module.resetPlayerTime(player);
            if (menu.closeOnClick()) {
                Gui.closeLater(player);
            }
        });
        place(menu.close(), (player, event) -> Gui.closeLater(player));
    }

    private void place(MenuItem item, GuiAction action) {
        if (item == null) {
            return;
        }
        ItemStack icon = item.build();
        for (int slot : item.slots()) {
            setItem(slot, icon, action);
        }
    }
}
