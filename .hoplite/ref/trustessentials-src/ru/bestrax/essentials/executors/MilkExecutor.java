/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.command.CommandSender
 *  org.bukkit.entity.Player
 *  org.bukkit.potion.PotionEffect
 */
package ru.bestrax.essentials.executors;

import java.util.ArrayList;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import ru.bestrax.essentials.api.commands.TrustCommand;
import ru.bestrax.essentials.utils.StringUtils;

public class MilkExecutor
extends TrustCommand {
    public MilkExecutor() {
        super("milk", new String[0]);
        this.setPermission("trustessentials.milk");
        this.setPlayersOnly(true);
        this.register();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player)sender;
        ArrayList<String> effects = new ArrayList<String>();
        for (PotionEffect activePotionEffect : player.getActivePotionEffects()) {
            player.removePotionEffect(activePotionEffect.getType());
            effects.add(activePotionEffect.getType().getName());
        }
        if (effects.size() == 0) {
            this.sendMessage(sender, "MILK_NOTHING", new String[0]);
        } else {
            this.sendMessage(sender, "MILK_COMPLETE", "%effects%", StringUtils.buildString(effects.toArray(new String[0]), ", "));
        }
    }
}
