/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.OfflinePlayer
 *  org.bukkit.entity.Player
 *  org.bukkit.inventory.Inventory
 *  org.bukkit.inventory.InventoryHolder
 *  org.bukkit.inventory.ItemStack
 */
package ru.bestrax.essentials.base.objects;

import gnu.trove.TCollections;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import ru.bestrax.essentials.api.CustomEnderChest;
import ru.bestrax.essentials.api.EssentialsApi;
import ru.bestrax.essentials.api.config.Config;
import ru.bestrax.essentials.api.inventory.GItem;
import ru.bestrax.essentials.api.manager.CoinsManager;
import ru.bestrax.essentials.utils.BukkitUtils;
import ru.bestrax.essentials.utils.ItemBuilder;

public class TrustCustomEnderChest
implements CustomEnderChest {
    private Player player;
    private final String playerName;
    private final Inventory inventory;
    private final String name;
    private int rows;
    private final TIntObjectMap<GItem> immutableItems = TCollections.synchronizedMap(new TIntObjectHashMap());

    public TrustCustomEnderChest(String playerName, String name, int rows) {
        this.playerName = playerName;
        this.name = name.replace("%name%", playerName);
        this.rows = rows;
        this.inventory = Bukkit.createInventory((InventoryHolder)this, (int)(EssentialsApi.getInstance().getSettings().getBoolean("pumpingEnderChest") ? 54 : rows * 9), (String)this.name);
        if (EssentialsApi.getInstance().getSettings().getBoolean("pumpingEnderChest")) {
            this.updateBuyableRows();
        }
    }

    @Override
    public void setImmutableItem(int x, int y, GItem item) {
        CustomEnderChest.super.setImmutableItem(x, y, item);
    }

    @Override
    public TIntObjectMap<GItem> getImmutableItems() {
        return this.immutableItems;
    }

    public Inventory getInventory() {
        return this.inventory;
    }

    @Override
    public int getRows() {
        return this.rows;
    }

    public void setRows(int rows) {
        this.rows = rows;
    }

    @Override
    public String getName() {
        return this.name;
    }

    private Player getPlayer() {
        if (this.player == null) {
            this.player = Bukkit.getPlayer((String)this.playerName);
        }
        return this.player;
    }

    @Override
    public String getPlayerName() {
        return this.playerName;
    }

    private void updateBuyableRows() {
        for (int row = this.rows; row < 6; ++row) {
            int finalRow = row;
            Config settings = EssentialsApi.getInstance().getSettings();
            Config lang = EssentialsApi.getInstance().getLang();
            GItem gItem = new GItem(ItemBuilder.builder(new ItemStack(BukkitUtils.getMaterial("STAINED_GLASS_PANE"), 1, (short)settings.getInt("guiGlassColor"))).setName(lang.getString("ENDER_CHEST_BUY_ROW_NAME", "%row%", String.valueOf(row + 1))).setLore(lang.getStringList("ENDER_CHEST_BUY_ROW_LORE", "%price%", String.valueOf(settings.getInt("enderChestRowPrice")), "%row%", String.valueOf(row + 1))).build(), (player, clickType, i) -> {
                if (!player.getName().equalsIgnoreCase(this.playerName) && !player.hasPermission("trustessentials.enderchest.other.buyrow")) {
                    player.sendMessage(lang.getString("ENDER_CHEST_CANT_BUY_ROW", "%name%", this.playerName));
                    return;
                }
                CoinsManager coinsManager = EssentialsApi.getCoinsManager();
                if (!coinsManager.hasCoins((OfflinePlayer)player, settings.getInt("enderChestRowPrice"))) {
                    player.sendMessage(lang.getString("ENDER_CHEST_BUY_ROW_NO_RUBLES", "%price%", String.valueOf(settings.getInt("enderChestRowPrice"))));
                    return;
                }
                coinsManager.withdraw((OfflinePlayer)player, settings.getInt("enderChestRowPrice"));
                player.sendMessage(lang.getString("ENDER_CHEST_BUY_ROW", "%price%", String.valueOf(settings.getInt("enderChestRowPrice")), "%row%", String.valueOf(finalRow + 1)));
                for (int slot = this.rows * 9; slot < this.rows * 9 + 9; ++slot) {
                    this.removeImmutableItem(slot);
                }
                ++this.rows;
            });
            for (int i2 = row * 9; i2 < row * 9 + 9; ++i2) {
                this.setImmutableItem(i2, gItem);
            }
        }
    }

    @Override
    public void setImmutableItem(int slot, GItem item) {
        ItemStack stack = item.getItem();
        this.immutableItems.put(slot, item);
        this.inventory.setItem(slot, stack);
    }

    @Override
    public void removeImmutableItem(int slot) {
        GItem item = this.immutableItems.remove(slot);
        if (item == null) {
            return;
        }
        this.inventory.setItem(slot, null);
    }

    @Override
    public void fillContent(ItemStack[] content) {
        int i = 0;
        for (ItemStack item : content) {
            if (i >= this.rows * 9) break;
            this.inventory.setItem(i, item);
            ++i;
        }
    }
}
