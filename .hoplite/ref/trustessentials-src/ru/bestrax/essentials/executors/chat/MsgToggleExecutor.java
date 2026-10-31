/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.command.CommandSender
 *  org.bukkit.entity.Player
 */
package ru.bestrax.essentials.executors.chat;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.bestrax.essentials.api.EssentialsApi;
import ru.bestrax.essentials.api.User;
import ru.bestrax.essentials.api.commands.TrustCommand;

public class MsgToggleExecutor
extends TrustCommand {
    public MsgToggleExecutor() {
        super("msgtoggle", "mtoggle");
        this.setPlayersOnly(true);
        this.setPermission("trustessentials.msgtoggle");
        this.register();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        User user;
        Player player = (Player)sender;
        user.setMsgToggle(!(user = EssentialsApi.getUserManager().getUser(player)).isMsgToggle());
        if (!user.isMsgToggle()) {
            this.sendMessage(sender, "MSGTOGGLE_OFF", new String[0]);
        } else {
            this.sendMessage(sender, "MSGTOGGLE_ON", new String[0]);
        }
    }
}
