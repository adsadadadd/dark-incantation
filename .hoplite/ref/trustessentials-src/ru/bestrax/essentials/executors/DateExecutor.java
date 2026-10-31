/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.command.CommandSender
 */
package ru.bestrax.essentials.executors;

import java.time.LocalDateTime;
import java.time.ZoneId;
import org.bukkit.command.CommandSender;
import ru.bestrax.essentials.api.commands.TrustCommand;

public class DateExecutor
extends TrustCommand {
    public DateExecutor() {
        super("date", "data", "rise");
        this.setPermission("trustessentials.date");
        this.register();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        LocalDateTime date = LocalDateTime.now(ZoneId.of("Europe/Moscow"));
        this.sendMessages(sender, "DATE", "%time%", this.getNum(date.getHour()) + ":" + this.getNum(date.getMinute()) + ":" + this.getNum(date.getSecond()), "%date%", this.getNum(date.getMonthValue()) + ":" + this.getNum(date.getDayOfMonth()) + ":" + this.getNum(date.getYear()));
    }

    private String getNum(int num) {
        return num < 10 ? "0" + num : String.valueOf(num);
    }
}
