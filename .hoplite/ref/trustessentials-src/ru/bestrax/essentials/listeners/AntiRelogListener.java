/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.entity.Player
 *  org.bukkit.event.EventHandler
 *  org.bukkit.event.Listener
 *  ru.leymooo.antirelog.event.PvpStartedEvent
 */
package ru.bestrax.essentials.listeners;

import java.util.Map;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import ru.bestrax.essentials.api.EssentialsApi;
import ru.bestrax.essentials.base.objects.TrustUser;
import ru.leymooo.antirelog.event.PvpStartedEvent;

public class AntiRelogListener
implements Listener {
    @EventHandler
    public void onPvpStartEvent(PvpStartedEvent e) {
        if (e.getAttacker() != null && !e.getAttacker().hasPermission("antirelog.bypass")) {
            this.cancelTeleport(e.getAttacker());
        }
        if (e.getDefender() != null && !e.getDefender().hasPermission("antirelog.bypass")) {
            this.cancelTeleport(e.getDefender());
        }
    }

    private void cancelTeleport(Player player) {
        TrustUser trustUser = (TrustUser)EssentialsApi.getUserManager().getUser(player);
        int teleports = 0;
        for (Map.Entry<String, Long> stringLongEntry : trustUser.getWaitTeleport().entrySet()) {
            trustUser.removeWaitTeleport(stringLongEntry.getKey());
            ++teleports;
        }
        if (teleports > 0) {
            player.sendMessage(EssentialsApi.getInstance().getLang().getString("TELEPORTING_CANCEL", new String[0]));
        }
    }
}
