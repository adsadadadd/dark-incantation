/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.entity.Player
 */
package ru.bestrax.essentials.base.managers;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.entity.Player;
import ru.bestrax.essentials.api.EssentialsApi;
import ru.bestrax.essentials.api.manager.EnderChestManager;
import ru.bestrax.essentials.base.EssentialsData;
import ru.bestrax.essentials.base.objects.TrustCustomEnderChest;
import ru.bestrax.essentials.utils.BukkitUtils;

public class TrustEnderChestManager
implements EnderChestManager {
    private final Map<String, TrustCustomEnderChest> inventories = new HashMap<String, TrustCustomEnderChest>();
    private final Map<String, TrustCustomEnderChest> oldInventories = new HashMap<String, TrustCustomEnderChest>();

    @Override
    public TrustCustomEnderChest getOpenedEnderChest(Player player) {
        return this.inventories.get(player.getName());
    }

    @Override
    public void openEnderChest(Player player, TrustCustomEnderChest enderChest) {
        String name = player.getName();
        this.oldInventories.put(name, enderChest);
        TrustCustomEnderChest oldInventory = this.inventories.get(name);
        this.inventories.put(name, enderChest);
        player.openInventory(enderChest.getInventory());
        this.oldInventories.remove(name);
    }

    @Override
    public void closeEnderChest(Player player) {
        String name = player.getName();
        TrustCustomEnderChest inventory = this.inventories.get(name);
        if (inventory == null || inventory == this.oldInventories.get(name)) {
            return;
        }
        this.inventories.remove(name);
        this.saveEnderChest(player, inventory);
    }

    @Override
    public void saveEnderChest(Player player, TrustCustomEnderChest enderChest) {
        BukkitUtils.runTaskAsync(() -> EssentialsData.saveEnderChestData(enderChest.getPlayerName(), enderChest.getInventory(), enderChest.getRows()), EssentialsApi.getInstance());
    }
}
