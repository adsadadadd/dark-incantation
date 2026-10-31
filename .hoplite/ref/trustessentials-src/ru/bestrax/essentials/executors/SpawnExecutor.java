/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.Location
 *  org.bukkit.command.CommandSender
 *  org.bukkit.entity.Player
 */
package ru.bestrax.essentials.executors;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.bestrax.essentials.api.EssentialsApi;
import ru.bestrax.essentials.api.commands.TrustCommand;
import ru.bestrax.essentials.base.objects.TrustUser;

public class SpawnExecutor
extends TrustCommand {
    public SpawnExecutor() {
        super("spawn", new String[0]);
        this.setPermission("trustessentials.spawn");
        this.register();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Location spawnDot = EssentialsApi.getSpawn();
        String target = args.length == 1 ? args[0] : sender.getName();
        Player player = Bukkit.getPlayer((String)target);
        if (player == null || !player.isOnline()) {
            this.sendMessage(sender, "PLAYER_NOT_FOUND", "%name%", target);
            return;
        }
        if (spawnDot == null) {
            this.sendMessage(sender, "SPAWN_FOUND", new String[0]);
            return;
        }
        if (!target.equalsIgnoreCase(sender.getName()) && !sender.hasPermission("trustessentials.spawn.other")) {
            this.sendMessage(sender, "NO_PERMISSION", "%permission%", "trustessentials.spawn.other");
            return;
        }
        TrustUser user = (TrustUser)EssentialsApi.getUserManager().getUser(player);
        if (!user.teleport(spawnDot)) {
            return;
        }
        this.sendMessage((CommandSender)player, "SPAWN", new String[0]);
        if (player.getName().equalsIgnoreCase(sender.getName())) {
            return;
        }
        this.sendMessage(sender, "SPAWN_TARGET", "%name%", player.getName());
    }
}
