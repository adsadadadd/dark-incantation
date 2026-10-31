/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.command.CommandSender
 *  org.bukkit.entity.Player
 */
package ru.bestrax.essentials.executors;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.bestrax.essentials.api.EssentialsApi;
import ru.bestrax.essentials.api.User;
import ru.bestrax.essentials.api.commands.TrustCommand;

public class ChatToggleExecutor
extends TrustCommand {
    public ChatToggleExecutor() {
        super("chattoggle", "chat");
        this.setPlayersOnly(true);
        this.setPermission("trustessentials.chattoggle");
        this.register();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        User user;
        Player player = (Player)sender;
        user.setChatToggle(!(user = EssentialsApi.getUserManager().getUser(player)).isChatToggle());
        if (!user.isChatToggle()) {
            this.sendMessage(sender, "CHATTOGGLE_OFF", new String[0]);
        } else {
            this.sendMessage(sender, "CHATTOGGLE_ON", new String[0]);
        }
    }
}
