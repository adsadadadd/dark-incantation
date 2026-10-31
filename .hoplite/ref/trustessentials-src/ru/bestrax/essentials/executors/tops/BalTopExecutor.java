/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.command.CommandSender
 */
package ru.bestrax.essentials.executors.tops;

import org.bukkit.command.CommandSender;
import ru.bestrax.essentials.api.EssentialsApi;
import ru.bestrax.essentials.executors.tops.TopExecutor;
import ru.bestrax.essentials.utils.BukkitUtils;

public class BalTopExecutor
extends TopExecutor {
    public BalTopExecutor() {
        super("baltop", "balancetop", "topbal");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        BukkitUtils.runTaskAsync(() -> this.send(sender, "BALTOP", EssentialsApi.getMoneyManager().getTop(100)), EssentialsApi.getInstance());
    }
}
