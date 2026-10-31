package me.x1no4ekk.core.utility.world;

import me.x1no4ekk.core.config.MenuItem;
import me.x1no4ekk.core.gui.Gui;
import me.x1no4ekk.core.gui.GuiAction;
import me.x1no4ekk.core.util.Text;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.inventory.ItemStack;

/**
 * {@code /pweather} menu: choose a personal weather or return to the server weather.
 * Holds only the immutable menu layout.
 */
final class PWeatherGui extends Gui {

    PWeatherGui(WorldModule module, WorldSettings.PersonalMenu<WorldSettings.WeatherOption> menu) {
        super(menu.rows(), Text.parse(menu.title()));
        place(menu.filler(), null);
        for (WorldSettings.WeatherOption option : menu.options()) {
            ItemStack icon = option.item().build(Placeholder.parsed("weather", option.label()));
            for (int slot : option.item().slots()) {
                setItem(slot, icon, (player, event) -> {
                    module.setPlayerWeather(player, option);
                    if (menu.closeOnClick()) {
                        Gui.closeLater(player);
                    }
                });
            }
        }
        place(menu.reset(), (player, event) -> {
            module.resetPlayerWeather(player);
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
