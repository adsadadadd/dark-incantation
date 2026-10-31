/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.command.CommandSender
 *  org.bukkit.entity.Player
 */
package ru.bestrax.essentials.executors.broadcasts;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.bestrax.essentials.api.EssentialsApi;
import ru.bestrax.essentials.api.commands.TrustCommand;
import ru.bestrax.essentials.api.cooldown.Cooldowns;
import ru.bestrax.essentials.utils.StringUtils;
import ru.bestrax.essentials.utils.TimeUtils;

public abstract class BroadCastExecutor
extends TrustCommand {
    public BroadCastExecutor(String command, String ... aliases) {
        super(command, aliases);
        this.setPermission("trustessentials." + command);
        this.register();
    }

    public void send(CommandSender sender, String broadcast, String[] args) {
        if (args.length == 0) {
            this.sendMessages(sender, broadcast + "_USAGE", new String[0]);
            return;
        }
        if (EssentialsApi.getInstance().getSettings().getBoolean(broadcast.toLowerCase() + "Cooldown") && !sender.hasPermission("trustessentials." + broadcast.toLowerCase() + ".cooldown")) {
            if (Cooldowns.has(sender.getName(), broadcast)) {
                this.sendMessage(sender, "COOLDOWN_" + broadcast, "%time%", TimeUtils.getTimeInSeconds(Cooldowns.get(sender.getName(), broadcast) / 1000));
                return;
            }
            Cooldowns.set(sender.getName(), broadcast, EssentialsApi.getInstance().getSettings().getInt(broadcast.toLowerCase() + "CooldownMillis"));
        }
        String message = StringUtils.buildString(args, " ");
        String name = !(sender instanceof Player) ? "\u00a7c\u00a7l\u0421\u0415\u0420\u0412\u0415\u0420" : sender.getName();
        this.sendBroadcasts(broadcast, "%name%", name, "%message%", message);
    }
}
