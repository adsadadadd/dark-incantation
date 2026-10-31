/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.lang.StringUtils
 *  org.bukkit.Bukkit
 *  org.bukkit.World
 *  org.bukkit.command.CommandSender
 *  org.bukkit.entity.Player
 */
package ru.bestrax.essentials.executors.time;

import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.bestrax.essentials.EssentialsPlugin;
import ru.bestrax.essentials.api.commands.TrustCommand;
import ru.bestrax.essentials.api.config.Config;
import ru.bestrax.essentials.api.cooldown.Cooldowns;
import ru.bestrax.essentials.utils.MessagesUtils;
import ru.bestrax.essentials.utils.TimeUtils;

public class TimeExecutor
extends TrustCommand {
    private EssentialsPlugin plugin;

    public TimeExecutor(EssentialsPlugin plugin) {
        super("time", new String[0]);
        this.setPermission("trustessentials.time");
        this.register();
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        block15: {
            block14: {
                Config settings = this.plugin.getSettings();
                if (args.length != 2) {
                    String time = sender instanceof Player ? TimeExecutor.getTime(((Player)sender).getWorld()) : "\u00a7c\u041d\u0438\u0447\u0435\u0433\u043e";
                    this.sendMessages(sender, "TIME", "%time%", time);
                    return;
                }
                if (!args[0].equalsIgnoreCase("set")) break block14;
                String time = args[1];
                if (settings.getBoolean("timeCooldown") && Cooldowns.has("essentials", "time") && !sender.hasPermission("trustessentials.time.cooldown.bypass")) {
                    this.sendMessage(sender, "TIME_COOLDOWN", "%time%", TimeUtils.getTimeInSeconds(Cooldowns.get("essentials", "time") / 1000));
                    return;
                }
                switch (time) {
                    case "day": {
                        TimeExecutor.setTime(settings.getStringList("timeWorld", new String[0]), this.getStringList("TIME_DAY_BROADCAST", "%name%", sender.getName()), 0);
                        this.sendMessage(sender, "TIME_DAY", new String[0]);
                        if (settings.getBoolean("timeCooldown")) {
                            Cooldowns.set("essentials", "time", settings.getInt("timeCooldownMillis"));
                            break;
                        }
                        break block15;
                    }
                    case "night": {
                        TimeExecutor.setTime(settings.getStringList("timeWorld", new String[0]), this.getStringList("TIME_NIGHT_BROADCAST", "%name%", sender.getName()), 14000);
                        this.sendMessage(sender, "TIME_NIGHT", new String[0]);
                        if (settings.getBoolean("timeCooldown")) {
                            Cooldowns.set("essentials", "time", settings.getInt("timeCooldownMillis"));
                            break;
                        }
                        break block15;
                    }
                    default: {
                        String str = args[1];
                        if (!StringUtils.isNumeric((String)str)) {
                            this.sendMessage(sender, "NUMBER_ONLY", "%replaced%", str);
                            break;
                        }
                        TimeExecutor.setTime(settings.getStringList("timeWorld", new String[0]), this.getStringList("TIME_CUSTOM_BROADCAST", "%name%", sender.getName(), "%custom%", str), Integer.parseInt(str));
                        this.sendMessage(sender, "TIME_CUSTOM", "%custom%", str);
                        if (settings.getBoolean("timeCooldown")) {
                            Cooldowns.set("essentials", "time", settings.getInt("timeCooldownMillis"));
                            break;
                        }
                        break block15;
                    }
                }
                break block15;
            }
            String time = sender instanceof Player ? TimeExecutor.getTime(((Player)sender).getWorld()) : "\u00a7c\u041d\u0438\u0447\u0435\u0433\u043e";
            this.sendMessages(sender, "TIME", "%time%", time);
        }
    }

    public static void setTime(List<String> worlds, List<String> broadcastMessage, int ticks) {
        MessagesUtils.sendMessagesAll(broadcastMessage);
        worlds.forEach(x -> Bukkit.getWorld((String)x).setTime((long)ticks));
    }

    public static String getTime(World world) {
        return world.getTime() < 14000L ? "\u00a7e\u0414\u0435\u043d\u044c" : "\u00a7e\u041d\u043e\u0447\u044c";
    }
}
