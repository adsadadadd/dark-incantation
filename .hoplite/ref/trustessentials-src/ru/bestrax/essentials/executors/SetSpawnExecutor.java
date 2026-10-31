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
import ru.bestrax.essentials.EssentialsPlugin;
import ru.bestrax.essentials.api.EssentialsApi;
import ru.bestrax.essentials.api.commands.TrustCommand;

public class SetSpawnExecutor
extends TrustCommand {
    private EssentialsPlugin plugin;

    public SetSpawnExecutor(EssentialsPlugin plugin) {
        super("setspawn", "set-spawn");
        this.setPermission("trustessentials.setspawn");
        this.setPlayersOnly(true);
        this.register();
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player)sender;
        EssentialsApi.setSpawn(player.getLocation());
        this.sendMessage((CommandSender)player, "SETSPAWN", new String[0]);
    }
}
