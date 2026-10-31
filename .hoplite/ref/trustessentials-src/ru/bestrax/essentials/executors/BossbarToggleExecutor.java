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

public class BossbarToggleExecutor
extends TrustCommand {
    public BossbarToggleExecutor() {
        super("bossbartoggle", "bossbar");
        this.setPermission("trustessentials.togglebossbar");
        this.setPlayersOnly(true);
        this.register();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        User user;
        Player player = (Player)sender;
        user.setBossbarToggle(!(user = EssentialsApi.getUserManager().getUser(player.getName())).isBossbarToggle());
        if (user.isBossbarToggle()) {
            this.sendMessage((CommandSender)player, "BOSSBAR_ON", new String[0]);
        } else {
            this.sendMessage((CommandSender)player, "BOSSBAR_OFF", new String[0]);
        }
    }
}
