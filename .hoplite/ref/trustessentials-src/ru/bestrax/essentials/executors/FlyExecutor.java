/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.command.CommandSender
 *  org.bukkit.entity.Player
 */
package ru.bestrax.essentials.executors;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.bestrax.essentials.api.EssentialsApi;
import ru.bestrax.essentials.api.User;
import ru.bestrax.essentials.api.commands.TrustCommand;
import ru.bestrax.essentials.api.cooldown.Cooldowns;
import ru.bestrax.essentials.utils.TimeUtils;

public class FlyExecutor
extends TrustCommand {
    public FlyExecutor() {
        super("fly", new String[0]);
        this.setPermission("trustessentials.fly");
        this.register();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        User user;
        String target = args.length == 0 ? sender.getName() : args[0];
        Player player = Bukkit.getPlayer((String)target);
        if (player == null || !player.isOnline()) {
            this.sendMessage(sender, "PLAYER_NOT_FOUND", "%name%", target);
            return;
        }
        if (!target.equalsIgnoreCase(sender.getName())) {
            if (!sender.hasPermission("trustessentials.fly.other")) {
                this.sendMessage(sender, "NO_PERMISSION", "%permission%", "trustessentials.fly.other");
                return;
            }
            if (!sender.hasPermission("trustessentials.fly.other.cooldownbypass")) {
                if (Cooldowns.has(sender.getName(), "flyother")) {
                    this.sendMessage(sender, "COOLDOWN_OTHER_FLY", "%time%", TimeUtils.getTimeInSeconds(Cooldowns.get(sender.getName(), "flyother") / 1000));
                    return;
                }
                Cooldowns.set(sender.getName(), "flyother", EssentialsApi.getInstance().getSettings().getInt("flyOtherCooldown"));
            }
        }
        user.setFly(!(user = EssentialsApi.getUserManager().getUser(player)).isFly(), true);
        boolean cur = player.getAllowFlight();
        if (sender.getName().equalsIgnoreCase(user.getUsername())) {
            return;
        }
        if (cur) {
            this.sendMessage(sender, "FLY_TARGET_ON", "%name%", target);
        } else {
            this.sendMessage(sender, "FLY_TARGET_OFF", "%name%", target);
        }
    }
}
