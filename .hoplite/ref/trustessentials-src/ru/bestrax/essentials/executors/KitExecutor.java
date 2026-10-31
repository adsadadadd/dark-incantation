/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.Material
 *  org.bukkit.command.CommandSender
 *  org.bukkit.entity.Player
 *  org.bukkit.event.EventHandler
 *  org.bukkit.event.Listener
 *  org.bukkit.event.inventory.InventoryCloseEvent
 *  org.bukkit.inventory.Inventory
 *  org.bukkit.inventory.ItemStack
 *  org.bukkit.plugin.Plugin
 */
package ru.bestrax.essentials.executors;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import ru.bestrax.essentials.api.EssentialsApi;
import ru.bestrax.essentials.api.Kit;
import ru.bestrax.essentials.api.User;
import ru.bestrax.essentials.api.VaultAPI;
import ru.bestrax.essentials.api.commands.TrustCommand;
import ru.bestrax.essentials.base.objects.TrustKit;
import ru.bestrax.essentials.gui.KitContentGui;
import ru.bestrax.essentials.gui.KitGui;
import ru.bestrax.essentials.utils.TimeUtils;

public class KitExecutor
extends TrustCommand
implements Listener {
    private Map<String, String> kitChange = new ConcurrentHashMap<String, String>();

    public KitExecutor() {
        super("tkit", "tkits");
        this.setPermission("trustessentials.kit");
        this.register();
        EssentialsApi.getInstance().getServer().getPluginManager().registerEvents((Listener)this, (Plugin)EssentialsApi.getInstance());
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        Player player;
        if (e.getPlayer() instanceof Player && this.kitChange.containsKey((player = (Player)e.getPlayer()).getName())) {
            Kit kit = EssentialsApi.getKitManager().getKit(this.kitChange.get(player.getName()));
            if (kit == null) {
                kit = new TrustKit(this.kitChange.get(player.getName()), this.kitChange.get(player.getName()), new ArrayList<ItemStack>(), false, true, true, new ItemStack(Material.DIAMOND), 1000, "default", (List<String>)new ArrayList<String>(){
                    {
                        this.add("default");
                    }
                }, "\u00a77\u0418\u0433\u0440\u043e\u043a", 100, 0);
            } else {
                EssentialsApi.getKitManager().removeKit(kit);
            }
            kit.getItems().clear();
            for (final ItemStack content : e.getInventory().getContents()) {
                if (content == null) continue;
                kit.addItems((Collection<ItemStack>)new ArrayList<ItemStack>(this){
                    final /* synthetic */ KitExecutor this$0;
                    {
                        this.this$0 = this$0;
                        this.add(content);
                    }
                });
            }
            EssentialsApi.getKitManager().addKit(kit);
            EssentialsApi.getInstance().saveKit(kit);
            this.sendMessage((CommandSender)player, "KIT_SAVE", new String[0]);
            this.kitChange.remove(player.getName());
        }
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 0 && sender instanceof Player) {
            if (EssentialsApi.getInstance().getSettings().getBoolean("kitsGuiEnabled")) {
                KitGui kitGui = EssentialsApi.getGuiManager().getGui(KitGui.class, (Player)sender);
                if (kitGui != null) {
                    kitGui.open();
                }
                return;
            }
            String kitCommand = EssentialsApi.getInstance().getSettings().getString("kitCommand", new String[0]);
            if (kitCommand.startsWith("console:")) {
                Bukkit.dispatchCommand((CommandSender)Bukkit.getConsoleSender(), (String)kitCommand.split("console:")[1].replace("%player%", sender.getName()));
            } else {
                ((Player)sender).chat(kitCommand);
            }
        } else {
            String name = args[0];
            Player target = sender instanceof Player ? (Player)sender : null;
            Player player = target;
            if (args.length >= 2) {
                if (args[0].equalsIgnoreCase("changekit") && sender instanceof Player && sender.hasPermission("trustessentials.kit.admin")) {
                    Kit kit1 = EssentialsApi.getKitManager().getKit(args[1]);
                    this.kitChange.put(target.getName(), args[1]);
                    Inventory inventory = Bukkit.createInventory(null, (int)54, (String)("" + args[1]));
                    if (kit1 != null) {
                        inventory.setContents((ItemStack[])kit1.getItems().toArray((Object[])new ItemStack[0]));
                    }
                    target.openInventory(inventory);
                    return;
                }
                if (args[0].equalsIgnoreCase("view") && sender instanceof Player) {
                    Kit kit1 = EssentialsApi.getKitManager().getKit(args[1]);
                    if (kit1 == null) {
                        this.sendMessage(sender, "KIT_NOT_FOUND", "%name%", name);
                        return;
                    }
                    new KitContentGui(target, kit1).open();
                    return;
                }
                target = Bukkit.getPlayer((String)args[1]);
                if (target == null || !target.isOnline()) {
                    this.sendMessage(sender, "PLAYER_NOT_FOUND", "%name%", args[1]);
                    return;
                }
            }
            if (target == null) {
                this.sendMessage(sender, "PLAYER_NOT_FOUND", "%name%", "<error>");
                return;
            }
            User user = sender instanceof Player ? EssentialsApi.getUserManager().getUser((Player)sender) : null;
            Kit kit = EssentialsApi.getKitManager().getKit(name);
            if (kit == null) {
                this.sendMessage(sender, "KIT_NOT_FOUND", "%name%", name);
            } else if (user != null && user.isCooldown(kit)) {
                long time = (long)user.getCooldown(kit) * 1000L + System.currentTimeMillis();
                String finalString = TimeUtils.getTimeInSeconds((time - System.currentTimeMillis()) / 1000L);
                this.sendMessage(sender, "KIT_COOLDOWN", "%time%", finalString);
            } else if (user != null && kit.getDefaultGroups() != null && !kit.getDefaultGroups().contains(VaultAPI.getPrimaryGroup((Player)sender))) {
                this.sendMessage(sender, "KIT_NO_PERMS_GROUP", "%group%", kit.getGroupPrefix());
            } else if (user != null && kit.getDefaultGroups() == null && !VaultAPI.hasGroup(kit.getMinimalGroup(), (Player)sender)) {
                this.sendMessage(sender, "KIT_NO_PERMS_GROUP", "%group%", kit.getGroupPrefix());
            } else {
                if (!target.getName().equalsIgnoreCase(sender.getName())) {
                    this.sendMessage(sender, "KIT_SELECT_TARGET", "%kit%", kit.getName(), "%name%", target.getName());
                } else {
                    this.sendMessage(sender, "KIT_SELECT", "%name%", kit.getName());
                }
                kit.giveKit(target);
                if (user != null) {
                    user.addKit(kit);
                }
            }
        }
    }
}
