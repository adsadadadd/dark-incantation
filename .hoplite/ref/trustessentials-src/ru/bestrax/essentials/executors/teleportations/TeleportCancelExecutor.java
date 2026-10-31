/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.command.CommandSender
 *  org.bukkit.entity.Player
 */
package ru.bestrax.essentials.executors.teleportations;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.bestrax.essentials.api.EssentialsApi;
import ru.bestrax.essentials.api.commands.TrustCommand;
import ru.bestrax.essentials.base.objects.TrustUser;
import ru.bestrax.essentials.utils.BukkitUtils;
import ru.bestrax.essentials.utils.StringUtils;

public class TeleportCancelExecutor
extends TrustCommand {
    public TeleportCancelExecutor() {
        super("teleportcancel", new String[0]);
        this.setPlayersOnly(true);
        this.register();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length < 1) {
            return;
        }
        TrustUser trustUser = (TrustUser)EssentialsApi.getUserManager().getUser((Player)sender);
        String key = trustUser.getWaitTeleportationKeyOfLocation(StringUtils.buildString(args, " "));
        if (key == null) {
            return;
        }
        trustUser.removeWaitTeleport(key);
        trustUser.getPlayer().playSound(trustUser.getPlayer().getLocation(), BukkitUtils.getBlockBellSound(), 1.0f, 1.0f);
        this.sendMessage(sender, "TELEPORTING_CANCEL", new String[0]);
    }
}
