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

public class SocialspyExecutor
extends TrustCommand {
    public SocialspyExecutor() {
        super("socialspy", "ss");
        this.setPermission("trustessentials.socialspy");
        this.setPlayersOnly(true);
        this.register();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        User user;
        Player player = (Player)sender;
        user.setSocialSpy(!(user = EssentialsApi.getUserManager().getUser(player)).isSocialSpy());
        if (user.isSocialSpy()) {
            this.sendMessage(sender, "SOCIALSPY_ON", new String[0]);
        } else {
            this.sendMessage(sender, "SOCIALSPY_OFF", new String[0]);
        }
    }
}
