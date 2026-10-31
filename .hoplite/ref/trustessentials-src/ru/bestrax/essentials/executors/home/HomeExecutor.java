/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.command.CommandSender
 *  org.bukkit.entity.Player
 */
package ru.bestrax.essentials.executors.home;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.bestrax.essentials.api.EssentialsApi;
import ru.bestrax.essentials.api.Home;
import ru.bestrax.essentials.api.User;
import ru.bestrax.essentials.api.commands.TrustCommand;
import ru.bestrax.essentials.base.objects.TrustUser;
import ru.bestrax.essentials.gui.HomeGui;
import ru.bestrax.essentials.utils.BukkitUtils;
import ru.bestrax.essentials.utils.LocationUtils;

public class HomeExecutor
extends TrustCommand {
    public HomeExecutor() {
        super("home", "homes");
        this.setPermission("trustessentials.home");
        this.setPlayersOnly(true);
        this.register();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        User user = EssentialsApi.getUserManager().getUser(sender.getName());
        if (user != null) {
            int size = user.getHomes().size();
            if (size == 0) {
                this.sendMessage(sender, "HOME_EMPTY", new String[0]);
            } else {
                Home home;
                String nameHome;
                Player player = (Player)sender;
                if (size == 1) {
                    nameHome = this.getFirstHome(user);
                    home = user.getHomes().get(nameHome);
                } else {
                    if (args.length == 0) {
                        HomeGui homeGui = EssentialsApi.getGuiManager().getGui(HomeGui.class, player);
                        if (homeGui != null) {
                            homeGui.open();
                            player.playSound(player.getLocation(), BukkitUtils.getBlockBellSound(), 1.0f, 1.0f);
                        }
                        return;
                    }
                    nameHome = args[0].toLowerCase();
                    home = user.getHomes().get(nameHome);
                    if (home == null) {
                        this.sendMessage(sender, "HOME_NOT_FOUND", "%name%", nameHome);
                        return;
                    }
                }
                ((TrustUser)user).addWaitTeleport(LocationUtils.stringFromLocation(home.getLocation(), true), EssentialsApi.getInstance().getLang().getString("HOME_TO", "%name%", nameHome));
            }
        }
    }

    private String getFirstHome(User user) {
        return user.getHomes().keySet().stream().findFirst().orElse(null);
    }
}
