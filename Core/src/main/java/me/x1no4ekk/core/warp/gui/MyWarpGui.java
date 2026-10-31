package me.x1no4ekk.core.warp.gui;

import me.x1no4ekk.core.CorePlugin;
import me.x1no4ekk.core.gui.Gui;
import me.x1no4ekk.core.util.Text;
import me.x1no4ekk.core.warp.Warp;
import me.x1no4ekk.core.warp.WarpModule;
import me.x1no4ekk.core.warp.WarpSettings;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.UUID;

/**
 * The viewer's own warps: click teleports, shift + right click deletes.
 */
public final class MyWarpGui extends WarpListGui {

    public MyWarpGui(CorePlugin plugin, WarpModule module, Player viewer) {
        this(plugin, module, module.settings(), viewer.getUniqueId());
    }

    private MyWarpGui(CorePlugin plugin, WarpModule module, WarpSettings config, UUID viewer) {
        super(plugin, module, config, config.mine(), Text.parse(config.mine().title()), config.mine().contentSlots());
        setEntries(module.manager().ownedBy(viewer));
    }

    @Override
    protected void onEntryClick(Player player, Warp warp, InventoryClickEvent event) {
        if (event.getClick() == ClickType.SHIFT_RIGHT) {
            UUID uuid = player.getUniqueId();
            Warp current = module.manager().get(warp.name());
            if (current == null || !current.isOwnedBy(uuid)) {
                plugin.messages().send(player, "warp.not-found", Placeholder.unparsed("warp", warp.name()));
                return;
            }
            module.manager().delete(current, error -> {
                Player online = Bukkit.getPlayer(uuid);
                if (online != null) {
                    plugin.messages().send(online, "general.database-error");
                }
            });
            plugin.messages().send(player, "warp.deleted", Placeholder.unparsed("warp", warp.name()));
            Gui.nextTick(player, online -> new MyWarpGui(plugin, module, online).open(online));
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
