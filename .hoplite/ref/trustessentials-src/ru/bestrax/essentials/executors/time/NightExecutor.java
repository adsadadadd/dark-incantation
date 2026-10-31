/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.command.CommandSender
 */
package ru.bestrax.essentials.executors.time;

import org.bukkit.command.CommandSender;
import ru.bestrax.essentials.EssentialsPlugin;
import ru.bestrax.essentials.api.commands.TrustCommand;
import ru.bestrax.essentials.api.config.Config;
import ru.bestrax.essentials.api.cooldown.Cooldowns;
import ru.bestrax.essentials.executors.time.TimeExecutor;
import ru.bestrax.essentials.utils.TimeUtils;

public class NightExecutor
extends TrustCommand {
    private EssentialsPlugin plugin;

    public NightExecutor(EssentialsPlugin plugin) {
        super("night", new String[0]);
        this.setPermission("trustessentials.night");
        this.register();
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Config settings = this.plugin.getSettings();
        if (settings.getBoolean("timeCooldown") && Cooldowns.has("essentials", "time") && !sender.hasPermission("trustessentials.time.cooldown.bypass")) {
            this.sendMessage(sender, "TIME_COOLDOWN", "%time%", TimeUtils.getTimeInSeconds(Cooldowns.get("essentials", "time") / 1000));
            return;
        }
        TimeExecutor.setTime(settings.getStringList("timeWorld", new String[0]), this.getStringList("TIME_NIGHT_BROADCAST", "%name%", sender.getName()), 14000);
        this.sendMessage(sender, "TIME_NIGHT", new String[0]);
        if (settings.getBoolean("timeCooldown")) {
            Cooldowns.set("essentials", "time", settings.getInt("timeCooldownMillis"));
        }
    }
}
