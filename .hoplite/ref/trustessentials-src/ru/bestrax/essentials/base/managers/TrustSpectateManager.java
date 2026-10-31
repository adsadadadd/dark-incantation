/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.GameMode
 *  org.bukkit.Location
 *  org.bukkit.entity.Player
 */
package ru.bestrax.essentials.base.managers;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import ru.bestrax.essentials.api.EssentialsApi;
import ru.bestrax.essentials.api.config.Config;
import ru.bestrax.essentials.api.manager.SpectateManager;
import ru.bestrax.essentials.utils.BukkitUtils;

public class TrustSpectateManager
implements SpectateManager {
    private final ConcurrentHashMap<UUID, UUID> spectations = new ConcurrentHashMap();
    private final ConcurrentHashMap<UUID, Location> locations = new ConcurrentHashMap();

    public TrustSpectateManager() {
        BukkitUtils.runTaskTimer(20L, () -> {
            for (Map.Entry<UUID, UUID> entrySet : this.spectations.entrySet()) {
                Player player = Bukkit.getPlayer((UUID)entrySet.getKey());
                Player target = Bukkit.getPlayer((UUID)entrySet.getValue());
                if (player == null || !player.isOnline()) {
                    this.spectations.remove(entrySet.getKey());
                    continue;
                }
                if (target == null || !target.isOnline()) {
                    this.stop(player);
                    continue;
                }
                if (player.getGameMode() != GameMode.SPECTATOR) {
                    this.stop(player);
                    continue;
                }
                Location locAs = target.getLocation();
                Location playerLoc = player.getLocation();
                if (!locAs.getWorld().getName().equalsIgnoreCase(playerLoc.getWorld().getName())) {
                    player.teleport(locAs);
                    continue;
                }
                Config settings = EssentialsApi.instance.getSettings();
                if (!(locAs.distance(playerLoc) > (double)settings.getInt("spectateMaxDistance"))) continue;
                player.teleport(locAs);
            }
        }, EssentialsApi.getInstance());
    }

    @Override
    public void stop(Player player) {
        Config config = EssentialsApi.getInstance().getLang();
        this.spectations.remove(player.getUniqueId());
        player.sendMessage(config.getString("SPECTATE_STOP", new String[0]));
        player.setGameMode(GameMode.SURVIVAL);
        player.teleport(this.locations.get(player.getUniqueId()));
        this.locations.remove(player.getUniqueId());
    }

    @Override
    public void start(Player player, Player target) {
        Config config = EssentialsApi.getInstance().getLang();
        player.setGameMode(GameMode.SPECTATOR);
        this.spectations.put(player.getUniqueId(), target.getUniqueId());
        this.locations.put(player.getUniqueId(), player.getLocation());
        player.teleport(target.getLocation());
        player.sendMessage(config.getString("SPECTATE_START", "%name%", target.getDisplayName()));
    }

    @Override
    public boolean isSpectation(Player player) {
        return this.spectations.containsKey(player.getUniqueId());
    }
}
