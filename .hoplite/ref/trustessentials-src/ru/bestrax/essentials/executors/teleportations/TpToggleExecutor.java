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
import ru.bestrax.essentials.api.User;
import ru.bestrax.essentials.api.commands.TrustCommand;

public class TpToggleExecutor
extends TrustCommand {
    public TpToggleExecutor() {
        super("tptoggle", new String[0]);
        this.setPlayersOnly(true);
        this.setPermission("trustessentials.tptoggle");
        this.register();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        User user;
        Player player = (Player)sender;
        user.setTpToggle(!(user = EssentialsApi.getUserManager().getUser(player)).isTpToggle());
        if (user.isTpToggle()) {
            this.sendMessage(sender, "TPTOGGLE_ON", new String[0]);
        } else {
            this.sendMessage(sender, "TPTOGGLE_OFF", new String[0]);
        }
    }
}
