/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.configuration.file.FileConfiguration
 *  org.bukkit.entity.Player
 */
package ru.bestrax.essentials.gui;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import ru.bestrax.essentials.api.EssentialsApi;
import ru.bestrax.essentials.api.Kit;
import ru.bestrax.essentials.api.User;
import ru.bestrax.essentials.api.VaultAPI;
import ru.bestrax.essentials.api.config.Config;
import ru.bestrax.essentials.api.inventory.GItem;
import ru.bestrax.essentials.api.inventory.type.MultiInventory;
import ru.bestrax.essentials.api.manager.UserManager;
import ru.bestrax.essentials.base.inventory.impl.types.UpdatableGui;
import ru.bestrax.essentials.gui.KitContentGui;
import ru.bestrax.essentials.utils.BukkitUtils;
import ru.bestrax.essentials.utils.ItemBuilder;
import ru.bestrax.essentials.utils.TimeUtils;

public class KitGui
extends UpdatableGui<MultiInventory> {
    private static final UserManager USER_MANAGER = EssentialsApi.getUserManager();

    public KitGui(Player player) {
        super(player);
        this.createInventory();
        this.setActions();
    }

    @Override
    protected void createInventory() {
        this.inventory = EssentialsApi.getInventoryAPI().createMultiInventory(this.player, EssentialsApi.getInstance().getLang().getString("GUI_NAME", new String[0]) + " \u25b8 " + EssentialsApi.getInstance().getLang().getString("KIT_GUI_NAME", new String[0]), 6);
    }

    @Override
    protected void setStaticItems() {
    }

    @Override
    public void setDynamicItems() {
        User user = USER_MANAGER.getUser(this.player);
        if (user == null) {
            return;
        }
        FileConfiguration guiConfig = EssentialsApi.getInstance().getGuiConfiguration();
        Config lang = EssentialsApi.getInstance().getLang();
        int pageNum = 0;
        INVENTORY_API.items((MultiInventory)this.inventory, pageNum, guiConfig.getStringList("kit.items"));
        for (Kit kit : EssentialsApi.getKitManager().getKits().values()) {
            if (((MultiInventory)this.inventory).getInventories().isEmpty()) continue;
            ItemBuilder itemBuilder = ItemBuilder.builder(kit.getIcon().clone()).setName(lang.getString("KIT_NAME", "%name%", kit.getLocalizedName()));
            if (kit.getDefaultGroups() != null && !kit.getDefaultGroups().contains(VaultAPI.getPrimaryGroup(user.getPlayer()))) {
                itemBuilder.setLore(lang.getStringList("KIT_DISABLE_ONLY_LORE", "%group%", kit.getGroupPrefix(), "%price%", String.valueOf(kit.getPrice())));
            } else if (kit.getDefaultGroups() == null && !VaultAPI.hasGroup(kit.getMinimalGroup(), user.getPlayer())) {
                itemBuilder.setLore(lang.getStringList("KIT_DISABLE_LORE", "%group%", kit.getGroupPrefix(), "%price%", String.valueOf(kit.getPrice())));
            } else if (user.isCooldown(kit)) {
                long time = (long)user.getCooldown(kit) * 1000L + System.currentTimeMillis();
                String timeBuilder = TimeUtils.getTimeInSeconds((time - System.currentTimeMillis()) / 1000L);
                itemBuilder.setLore(lang.getStringList("KIT_COOLDOWN_LORE", "%time%", timeBuilder, "%group%", kit.getGroupPrefix(), "%price%", String.valueOf(kit.getPrice())));
            } else {
                itemBuilder.setLore(lang.getStringList("KIT_ENABLE_LORE", "%group%", kit.getGroupPrefix(), "%price%", String.valueOf(kit.getPrice())));
            }
            int slot = kit.getGuiSlot();
            if (slot >= guiConfig.getInt("kit.endSlot")) {
                slot -= guiConfig.getInt("kit.endSlot");
                INVENTORY_API.items((MultiInventory)this.inventory, ++pageNum, guiConfig.getStringList("kit.items"));
            }
            ((MultiInventory)this.inventory).setItem(pageNum, slot, new GItem(itemBuilder.removeFlags().build(), (player, clickType, i) -> {
                player.playSound(player.getLocation(), BukkitUtils.getBlockBellSound(), 1.0f, 1.0f);
                new KitContentGui(player, kit).open();
            }));
        }
        INVENTORY_API.backButton((MultiInventory)this.inventory, (player1, clickType, i) -> player1.chat("/menu"), guiConfig.getInt("kit.backSlot"));
        if (guiConfig.contains("kit.downSlot") && guiConfig.contains("kit.upSlot")) {
            INVENTORY_API.pageButton((MultiInventory)this.getInventory(), guiConfig.getInt("kit.downSlot"), guiConfig.getInt("kit.upSlot"));
        }
    }
}
