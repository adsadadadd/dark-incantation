/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.command.CommandSender
 */
package ru.bestrax.essentials.executors.tops;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.command.CommandSender;
import ru.bestrax.essentials.api.commands.TrustCommand;

public abstract class TopExecutor
extends TrustCommand {
    public TopExecutor(String command, String ... aliases) {
        super(command, aliases);
        this.setPermission("trustessentials." + command);
        this.register();
    }

    public void send(CommandSender sender, String message, List<Pair<String, Integer>> top) {
        ArrayList<String> args = new ArrayList<String>();
        int topNum = 1;
        for (Pair<String, Integer> topEntry : top) {
            args.add("%" + topNum + "_name%");
            args.add(topEntry.getKey());
            args.add("%" + topNum + "_value%");
            args.add(String.valueOf(topEntry.getValue()));
            ++topNum;
        }
        this.sendMessages(sender, message, args.toArray(new String[args.size()]));
    }
}
