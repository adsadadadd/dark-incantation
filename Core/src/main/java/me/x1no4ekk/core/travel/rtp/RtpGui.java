package me.x1no4ekk.core.travel.rtp;

import me.x1no4ekk.core.gui.Gui;
import me.x1no4ekk.core.util.Durations;
import me.x1no4ekk.core.util.ItemBuilder;
import me.x1no4ekk.core.util.Text;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.entity.Player;

/**
 * Random teleport mode picker. The status of every mode is a snapshot taken when the menu is opened.
 */
final class RtpGui extends Gui {

    RtpGui(RtpManager manager, RtpSettings config, Player viewer) {
        super(config.gui().rows(), Text.parse(config.gui().title()));
        RtpSettings.GuiSettings gui = config.gui();
        for (RtpSettings.RtpMode mode : config.modes().values()) {
            Component status;
            if (!manager.canUse(viewer, mode)) {
                status = Text.parse(gui.statusNoPermission());
            } else {
                long left = viewer.hasPermission(RtpManager.BYPASS_COOLDOWN_PERMISSION) ? 0L
                        : manager.cooldownLeft(viewer.getUniqueId(), mode.id());
                status = left > 0
                        ? Text.parse(gui.statusCooldown(), Placeholder.unparsed("time", Durations.format(left)))
                        : Text.parse(gui.statusAvailable());
            }
            TagResolver placeholders = TagResolver.resolver(
                    Placeholder.component("status", status),
                    Placeholder.unparsed("world", mode.world().isEmpty() ? "—" : mode.world()),
                    Placeholder.unparsed("min_radius", String.valueOf(mode.minRadius())),
                    Placeholder.unparsed("radius", String.valueOf(mode.maxRadius())),
                    Placeholder.unparsed("cooldown", Durations.format(mode.cooldownMillis())));
            for (int slot : mode.icon().slots()) {
                setItem(slot, mode.icon().build(placeholders), (player, event) -> Gui.nextTick(player, online -> {
                    online.closeInventory();
                    manager.start(online, mode);
                }));
            }
        }
        setItem(gui.close().slot(), gui.close().build(), (player, event) -> Gui.closeLater(player));
        if (gui.filler() != null) {
            fillEmpty(ItemBuilder.of(gui.filler()).hideTooltip().build());
        }
    }
}
