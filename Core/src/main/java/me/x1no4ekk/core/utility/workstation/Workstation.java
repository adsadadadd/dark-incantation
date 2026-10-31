package me.x1no4ekk.core.utility.workstation;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.MenuType;
import org.bukkit.inventory.view.builder.LocationInventoryViewBuilder;

/**
 * Virtual workstations opened without a block. {@link #command()} is both the command name and the settings key.
 */
public enum Workstation {

    WORKBENCH("workbench", MenuType.CRAFTING),
    ANVIL("anvil", MenuType.ANVIL),
    CARTOGRAPHY_TABLE("carttable", MenuType.CARTOGRAPHY_TABLE),
    ENCHANTING_TABLE("etable", MenuType.ENCHANTMENT),
    GRINDSTONE("grindstone", MenuType.GRINDSTONE),
    LOOM("loom", MenuType.LOOM),
    SMITHING_TABLE("smithtable", MenuType.SMITHING),
    STONECUTTER("stonecutter", MenuType.STONECUTTER);

    private final String command;
    private final MenuType.Typed<? extends InventoryView, ? extends LocationInventoryViewBuilder<? extends InventoryView>> menu;

    Workstation(String command,
                MenuType.Typed<? extends InventoryView, ? extends LocationInventoryViewBuilder<? extends InventoryView>> menu) {
        this.command = command;
        this.menu = menu;
    }

    public String command() {
        return command;
    }

    public String permission() {
        return "core." + command;
    }

    /**
     * Opens the menu at the player's position. Reachability is not checked because there is no block;
     * the position only affects sounds and the enchanting table's bookshelf bonus.
     */
    public void open(Player player, Component title) {
        InventoryView view = menu.builder()
                .title(title)
                .location(player.getLocation())
                .checkReachable(false)
                .build(player);
        player.openInventory(view);
    }
}
