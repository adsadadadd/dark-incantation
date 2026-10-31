/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.entity.Player
 *  org.bukkit.event.EventHandler
 *  org.bukkit.event.EventPriority
 *  org.bukkit.event.Listener
 *  org.bukkit.event.player.PlayerJoinEvent
 *  org.bukkit.inventory.Inventory
 *  org.bukkit.inventory.ItemStack
 *  org.bukkit.inventory.PlayerInventory
 */
package ru.bestrax.essentials.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import ru.bestrax.essentials.api.EssentialsApi;
import ru.bestrax.essentials.api.Kit;
import ru.bestrax.essentials.api.User;

public class KitListener
implements Listener {
    @EventHandler(priority=EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        User user = EssentialsApi.getUserManager().getUser(player);
        if (user != null && user.isOnline() && user.isFirstJoin()) {
            PlayerInventory inventory = player.getInventory();
            for (Kit kit : EssentialsApi.getKitManager().getKits().values()) {
                if (!kit.isStart()) continue;
                kit.getItems().forEach(arg_0 -> KitListener.lambda$onJoin$0((Inventory)inventory, arg_0));
            }
        }
    }

    private static /* synthetic */ void lambda$onJoin$0(Inventory rec$, ItemStack xva$0) {
        rec$.addItem(new ItemStack[]{xva$0});
    }
}
