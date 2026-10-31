/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.command.CommandSender
 *  org.bukkit.entity.Player
 */
package ru.bestrax.essentials.executors.chat;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.bestrax.essentials.api.EssentialsApi;
import ru.bestrax.essentials.api.User;
import ru.bestrax.essentials.executors.chat.MessageExecutor;
import ru.bestrax.essentials.utils.StringUtils;

public class ReplyExecutor
extends MessageExecutor {
    public ReplyExecutor() {
        super("reply", "r");
        this.setPermission("trustessentials.reply");
        this.setPlayersOnly(true);
        this.register();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player)sender;
        if (args.length < 1) {
            this.sendMessages(sender, "REPLY_USAGE", new String[0]);
            return;
        }
        User user = EssentialsApi.getUserManager().getUser(player);
        if (user.getReplyPlayer() == null) {
            this.sendMessage(sender, "REPLY_PLAYER_NOT_FOUND", new String[0]);
            return;
        }
        String bot = EssentialsApi.getBotManager() == null ? null : EssentialsApi.getBotManager().getBot(user.getReplyPlayer());
        Player target = Bukkit.getPlayer((String)user.getReplyPlayer());
        if (target == null && bot != null) {
            this.sendBot(player, bot, StringUtils.buildString(args, " "));
            return;
        }
        if (target == null || !target.isOnline()) {
            this.sendMessage(sender, "REPLY_PLAYER_NOT_FOUND", new String[0]);
            return;
        }
        if (player.getName().equalsIgnoreCase(target.getName())) {
            this.sendMessage(sender, "MSG_YOU", new String[0]);
            return;
        }
        User targetUser = EssentialsApi.getUserManager().getUser(target);
        if (targetUser.getIgnores().contains(player.getName())) {
            this.sendMessage(sender, "IGNORE_BLOCK", "%name%", targetUser.getUsername());
            return;
        }
        if (!targetUser.isMsgToggle()) {
            this.sendMessage(sender, "MSGTOGGLE_BLOCK", new String[0]);
            return;
        }
        String message = StringUtils.buildString(args, " ");
        this.send(player, target, message);
    }
}
