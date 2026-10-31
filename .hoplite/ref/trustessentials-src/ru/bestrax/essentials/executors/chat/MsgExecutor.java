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

public class MsgExecutor
extends MessageExecutor {
    public MsgExecutor() {
        super("msg", "message", "tell", "w", "t", "pm", "m");
        this.setPlayersOnly(true);
        this.setPermission("trustessentials.msg");
        this.register();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player)sender;
        if (args.length < 2) {
            this.sendMessages(sender, "MSG_USAGE", new String[0]);
            return;
        }
        String bot = EssentialsApi.getBotManager() == null ? null : EssentialsApi.getBotManager().getBot(args[0]);
        Player target = Bukkit.getPlayer((String)args[0]);
        if (target == null && bot != null) {
            this.sendBot(player, bot, StringUtils.buildString(args, " ", 1));
            return;
        }
        if (target == null || !target.isOnline() || EssentialsApi.getUserManager().getUser(target).isVanished() && !player.hasPermission("trustessentials.vanish.see")) {
            this.sendMessage(sender, "PLAYER_NOT_FOUND", "%name%", args[0]);
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
        String message = StringUtils.buildString(args, " ", 1);
        this.send(player, target, message);
    }
}
