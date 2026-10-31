/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.command.CommandSender
 */
package ru.bestrax.essentials.executors.warp;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import org.bukkit.command.CommandSender;
import ru.bestrax.essentials.api.EssentialsApi;
import ru.bestrax.essentials.api.Warp;
import ru.bestrax.essentials.api.commands.TrustCommand;

public class WarpInfoExecutor
extends TrustCommand {
    private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");

    public WarpInfoExecutor() {
        super("warpinfo", new String[0]);
        this.setPermission("trustessentials.warpinfo");
        this.register();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length != 1) {
            this.sendMessages(sender, "WARPINFO_USAGE", new String[0]);
            return;
        }
        String name = args[0];
        if (!EssentialsApi.getWarpManager().getWarps().containsKey(name.toLowerCase())) {
            this.sendMessage(sender, "WARP_NOT_FOUND", "%name%", name);
        } else {
            Warp warp = EssentialsApi.getWarpManager().getWarp(name);
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(warp.getDate().getTime());
            String date = this.simpleDateFormat.format(calendar.getTime());
            this.sendMessages(sender, "WARPINFO", "%name%", warp.getName(), "%owner%", warp.getOwnerName(), "%date%", date);
        }
    }
}
