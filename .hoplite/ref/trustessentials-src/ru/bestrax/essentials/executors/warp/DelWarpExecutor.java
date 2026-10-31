/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.command.CommandSender
 */
package ru.bestrax.essentials.executors.warp;

import org.bukkit.command.CommandSender;
import ru.bestrax.essentials.api.EssentialsApi;
import ru.bestrax.essentials.api.User;
import ru.bestrax.essentials.api.Warp;
import ru.bestrax.essentials.api.commands.TrustCommand;
import ru.bestrax.essentials.api.events.UserDeleteWarpEvent;
import ru.bestrax.essentials.utils.BukkitUtils;

public class DelWarpExecutor
extends TrustCommand {
    public DelWarpExecutor() {
        super("delwarp", "deletewarp", "remwarp", "removewarp");
        this.setPermission("trustessentials.delwarp");
        this.setPlayersOnly(true);
        this.register();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        User user = EssentialsApi.getUserManager().getUser(sender.getName());
        if (user != null) {
            if (args.length == 0) {
                this.sendMessages(sender, "DELWARP_USAGE", new String[0]);
            } else {
                String name = args[0];
                if (!EssentialsApi.getWarpManager().getWarps().containsKey(name.toLowerCase())) {
                    this.sendMessage((CommandSender)user.getPlayer(), "WARP_NOT_FOUND", "%name%", name);
                } else {
                    Warp warp = EssentialsApi.getWarpManager().getWarp(name);
                    if (warp.isSystem() || user != null && !warp.getOwnerName().equalsIgnoreCase(sender.getName()) && !sender.hasPermission("trustessentials.delwarp.other")) {
                        this.sendMessage(sender, "WARP_NOT_YOU", "%name%", warp.getName());
                    } else {
                        UserDeleteWarpEvent event = new UserDeleteWarpEvent(user, warp);
                        BukkitUtils.callEvent(event, EssentialsApi.getInstance());
                        if (!event.isCancelled()) {
                            this.sendMessage((CommandSender)user.getPlayer(), "WARP_DELETE", "%name%", warp.getName());
                            EssentialsApi.getWarpManager().removeWarp(warp);
                        }
                    }
                }
            }
        }
    }
}
