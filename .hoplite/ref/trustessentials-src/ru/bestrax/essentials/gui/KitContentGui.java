/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Material
 *  org.bukkit.OfflinePlayer
 *  org.bukkit.configuration.file.FileConfiguration
 *  org.bukkit.entity.Player
 *  org.bukkit.inventory.ItemStack
 */
package ru.bestrax.essentials.gui;

import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ru.bestrax.essentials.api.EssentialsApi;
import ru.bestrax.essentials.api.Kit;
import ru.bestrax.essentials.api.User;
import ru.bestrax.essentials.api.VaultAPI;
import ru.bestrax.essentials.api.config.Config;
import ru.bestrax.essentials.api.inventory.GItem;
import ru.bestrax.essentials.api.inventory.type.MultiInventory;
import ru.bestrax.essentials.api.manager.CoinsManager;
import ru.bestrax.essentials.api.manager.UserManager;
import ru.bestrax.essentials.base.inventory.impl.types.UpdatableGui;
import ru.bestrax.essentials.utils.BukkitUtils;
import ru.bestrax.essentials.utils.ItemBuilder;
import ru.bestrax.essentials.utils.TimeUtils;

public class KitContentGui
extends UpdatableGui<MultiInventory> {
    private static final UserManager USER_MANAGER = EssentialsApi.getUserManager();
    private final Kit kit;

    public KitContentGui(Player player, Kit kit) {
        super(player);
        this.kit = kit;
        this.createInventory();
        this.setActions();
    }

    @Override
    protected void createInventory() {
        this.inventory = INVENTORY_API.createMultiInventory(this.player, EssentialsApi.getInstance().getLang().getString("GUI_NAME", new String[0]) + " \u25b8 " + EssentialsApi.getInstance().getLang().getString("KIT_GUI_NAME", new String[0]) + " \u25b8 " + EssentialsApi.getInstance().getLang().getString("KIT_CONTENT_GUI_NAME", "%name%", this.kit.getLocalizedName()), 6);
    }

    @Override
    protected void setStaticItems() {
    }

    @Override
    protected void setDynamicItems() {
        User user = USER_MANAGER.getUser(this.player);
        Config lang = EssentialsApi.getInstance().getLang();
        if (user != null) {
            FileConfiguration guiConfig = EssentialsApi.getInstance().getGuiConfiguration();
            int startSlot = guiConfig.getInt("kitContent.startSlot");
            int pageNum = 0;
            int slot = startSlot;
            INVENTORY_API.items((MultiInventory)this.inventory, pageNum, guiConfig.getStringList("kitContent.items"));
            for (ItemStack itemStack : this.kit.getItems()) {
                ((MultiInventory)this.inventory).setItem(pageNum, slot, new GItem(itemStack.clone()));
                boolean nextRow = false;
                for (String row : guiConfig.getStringList("kitContent.rows")) {
                    int newSlot;
                    int endSlot;
                    try {
                        endSlot = Integer.parseInt(row.split(":")[0]);
                        newSlot = Integer.parseInt(row.split(":")[1]);
                    }
                    catch (NumberFormatException e) {
                        continue;
                    }
                    if (slot != endSlot) continue;
                    if (newSlot == startSlot) {
                        slot = newSlot;
                        INVENTORY_API.items((MultiInventory)this.inventory, ++pageNum, guiConfig.getStringList("kitContent.items"));
                    } else {
                        slot = newSlot;
                    }
                    nextRow = true;
                }
                if (nextRow) continue;
                ++slot;
            }
            boolean access = false;
            String playerGroup = VaultAPI.getPrimaryGroup(this.player);
            ItemBuilder itemBuilder = ItemBuilder.builder(BukkitUtils.getMaterial("ENDER_EYE")).setName(lang.getString("KIT_GET_NAME", "%name%", this.kit.getLocalizedName()));
            if (this.kit.getDefaultGroups() != null && !this.kit.getDefaultGroups().contains(playerGroup)) {
                itemBuilder.setLore(lang.getStringList("KIT_GET_DISABLE_ONLY_LORE", "%group%", this.kit.getGroupPrefix()));
            } else if (this.kit.getDefaultGroups() == null && !VaultAPI.hasGroup(this.kit.getMinimalGroup(), this.player)) {
                itemBuilder.setLore(lang.getStringList("KIT_GET_DISABLE_LORE", "%group%", this.kit.getGroupPrefix()));
            } else if (user.isCooldown(this.kit)) {
                long time = (long)user.getCooldown(this.kit) * 1000L + System.currentTimeMillis();
                String timeBuilder = TimeUtils.getTimeInSeconds((time - System.currentTimeMillis()) / 1000L);
                itemBuilder.setLore(lang.getStringList("KIT_GET_COOLDOWN_LORE", "%time%", timeBuilder, "%group%", this.kit.getGroupPrefix()));
            } else {
                itemBuilder.setLore(lang.getStringList("KIT_GET_ENABLE_LORE", "%group%", this.kit.getGroupPrefix()));
                access = true;
            }
            boolean finalAccess = access;
            if (this.kit.isCanGet()) {
                ((MultiInventory)this.inventory).getInventories().forEach(inventory -> inventory.setItem(this.kit.isCanBuy() ? guiConfig.getInt("kitContent.getSlot") : guiConfig.getInt("kitContent.getWithoutBuySlot"), new GItem(itemBuilder.removeFlags().build(), (player1, clickType, i) -> {
                    if (!user.isCooldown(this.kit) && finalAccess) {
                        this.player.sendMessage(lang.getString("KIT_SELECT", "%name%", this.kit.getName()));
                        this.kit.giveKit(this.player);
                        user.addKit(this.kit);
                        this.player.playSound(this.player.getLocation(), BukkitUtils.getBlockBellSound(), 1.0f, 1.0f);
                        this.player.closeInventory();
                    } else {
                        this.player.playSound(this.player.getLocation(), BukkitUtils.getBlockBellSound(), 1.0f, 1.0f);
                    }
                })));
            }
            if (this.kit.isCanBuy()) {
                ((MultiInventory)this.inventory).setItem(this.kit.isCanGet() ? guiConfig.getInt("kitContent.buySlot") : guiConfig.getInt("kitContent.buyWithoutGetSlot"), new GItem(ItemBuilder.builder(Material.ENDER_PEARL).setName(lang.getString("KIT_BUY_NAME", "%name%", this.kit.getLocalizedName())).setLore(lang.getStringList("KIT_BUY_LORE", "%price%", String.valueOf(this.kit.getPrice()))).build(), (player1, clickType, i) -> {
                    CoinsManager coinsManager = EssentialsApi.getCoinsManager();
                    if (!coinsManager.hasCoins((OfflinePlayer)this.player, this.kit.getPrice())) {
                        this.player.sendMessage(lang.getString("KIT_NO_RUBLES", "%price%", String.valueOf(this.kit.getPrice())));
                        this.player.playSound(this.player.getLocation(), BukkitUtils.getBlockBellSound(), 1.0f, 1.0f);
                        return;
                    }
                    coinsManager.withdraw((OfflinePlayer)this.player, this.kit.getPrice());
                    this.player.sendMessage(lang.getString("KIT_BUY", "%name%", this.kit.getName(), "%price%", String.valueOf(this.kit.getPrice())));
                    this.kit.giveKit(this.player);
                    this.player.playSound(this.player.getLocation(), BukkitUtils.getBlockBellSound(), 1.0f, 1.0f);
                    this.player.closeInventory();
                }));
            }
            INVENTORY_API.backButton((MultiInventory)this.inventory, (player1, clickType, i) -> player1.chat("/kit"), guiConfig.getInt("kitContent.backSlot"));
            INVENTORY_API.pageButton((MultiInventory)this.inventory, guiConfig.getInt("kitContent.downSlot"), guiConfig.getInt("kitContent.upSlot"));
        }
    }
}
