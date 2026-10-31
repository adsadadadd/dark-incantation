/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.lang.StringUtils
 *  org.bukkit.Bukkit
 *  org.bukkit.OfflinePlayer
 *  org.bukkit.command.CommandSender
 *  org.bukkit.entity.Player
 */
package ru.bestrax.essentials.executors;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.bestrax.essentials.api.EssentialsApi;
import ru.bestrax.essentials.api.commands.TrustCommand;
import ru.bestrax.essentials.api.config.Config;
import ru.bestrax.essentials.api.manager.MoneyManager;

public class PayExecutor
extends TrustCommand {
    public PayExecutor() {
        super("pay", new String[0]);
        this.setPermission("trustessentials.pay");
        this.setPlayersOnly(true);
        this.register();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player)sender;
        if (args.length != 2) {
            this.sendMessages(sender, "PAY_USAGE", new String[0]);
            return;
        }
        Player target = Bukkit.getPlayer((String)args[0]);
        if (target == null || !player.isOnline()) {
            this.sendMessage(sender, "PLAYER_NOT_FOUND", "%name%", args[0]);
            return;
        }
        if (target.getName().equalsIgnoreCase(player.getName())) {
            this.sendMessage((CommandSender)player, "PAY_YOU", new String[0]);
            return;
        }
        String count = args[1];
        if (!StringUtils.isNumeric((String)count)) {
            this.sendMessage((CommandSender)player, "NUMBER_ONLY", "%replaced%", count);
            return;
        }
        MoneyManager moneyManager = EssentialsApi.getMoneyManager();
        int icount = Integer.parseInt(count);
        int comission = this.getComission(icount);
        if ((double)icount > moneyManager.get((OfflinePlayer)player)) {
            this.sendMessage((CommandSender)player, "PAY_ENOUGH", new String[0]);
            return;
        }
        moneyManager.withdraw((OfflinePlayer)player, (double)icount);
        moneyManager.give((OfflinePlayer)target, (double)(icount - comission));
        this.sendMessage((CommandSender)player, "PAY", "%name%", target.getDisplayName(), "%count%", String.valueOf(icount - comission));
        this.sendMessage((CommandSender)target, "PAY_TARGET", "%name%", player.getDisplayName(), "%count%", String.valueOf(icount - comission));
    }

    private int getComission(int price) {
        Config settings = EssentialsApi.getInstance().getSettings();
        int comissionProcent = settings.getInt("payComission");
        if (comissionProcent == 0) {
            return 0;
        }
        return price / 100 * comissionProcent;
    }
}
