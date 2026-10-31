/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.ChatColor
 *  org.bukkit.boss.BarColor
 *  org.bukkit.boss.BarFlag
 *  org.bukkit.boss.BarStyle
 *  org.bukkit.boss.BossBar
 *  org.bukkit.configuration.ConfigurationSection
 *  org.bukkit.configuration.file.FileConfiguration
 *  org.bukkit.entity.Player
 */
package ru.bestrax.essentials.base.managers;

import java.util.ConcurrentModificationException;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import ru.bestrax.essentials.EssentialsPlugin;
import ru.bestrax.essentials.api.EssentialsApi;
import ru.bestrax.essentials.api.manager.BossBarManager;

public class TrustBossBarManager
implements BossBarManager {
    private BossBar mainBar;
    private ConfigurationSection barsConfig;
    private Thread thread;

    @Override
    public void add(Player player) {
        if (this.mainBar == null) {
            return;
        }
        this.mainBar.addPlayer(player);
    }

    @Override
    public void remove(Player player) {
        if (this.mainBar == null) {
            return;
        }
        this.mainBar.removePlayer(player);
    }

    @Override
    public void hide() {
        if (this.mainBar == null) {
            return;
        }
        this.mainBar.hide();
    }

    @Override
    public void show() {
        if (this.mainBar == null) {
            return;
        }
        this.mainBar.show();
    }

    @Override
    public void addAll() {
        if (this.mainBar == null) {
            return;
        }
        Bukkit.getOnlinePlayers().stream().filter(x -> EssentialsApi.getUserManager().getUser((Player)x).isBossbarToggle()).forEach(x -> this.mainBar.addPlayer(x));
    }

    @Override
    public void removeAll() {
        if (this.mainBar == null) {
            return;
        }
        this.mainBar.removeAll();
    }

    public void setBossBar(String bar) {
        if (this.mainBar == null) {
            return;
        }
        ConfigurationSection barS = this.barsConfig.getConfigurationSection(bar);
        this.mainBar.setColor(BarColor.valueOf((String)barS.getString("color")));
        this.mainBar.setStyle(BarStyle.valueOf((String)barS.getString("style")));
        this.mainBar.setTitle(ChatColor.translateAlternateColorCodes((char)'&', (String)barS.getString("title")));
    }

    public int getTime(String bar) {
        ConfigurationSection barS = this.barsConfig.getConfigurationSection(bar);
        return barS.getInt("time");
    }

    @Override
    public void reload() {
        FileConfiguration config;
        if (this.mainBar != null) {
            this.removeAll();
            this.thread.interrupt();
        }
        if ((config = EssentialsApi.getFileManager().getFile(EssentialsPlugin.class, EssentialsApi.getInstance().getDataFolder(), "settings.yml")).contains("bossbars")) {
            this.barsConfig = config.getConfigurationSection("bossbars");
            this.mainBar = Bukkit.createBossBar((String)"", (BarColor)BarColor.RED, (BarStyle)BarStyle.SOLID, (BarFlag[])new BarFlag[0]);
            this.addAll();
            this.thread = new Thread(() -> {
                block4: while (!Thread.interrupted()) {
                    block5: for (String key : this.barsConfig.getKeys(false)) {
                        this.setBossBar(key);
                        int timeStay = this.getTime(key);
                        for (double time = (double)timeStay; time != 0.0; time -= 1.0) {
                            double percentage = time / (double)timeStay;
                            try {
                                this.mainBar.setProgress(percentage);
                            }
                            catch (ConcurrentModificationException e) {
                                e.printStackTrace();
                                continue block5;
                            }
                            try {
                                Thread.sleep(50L);
                                continue;
                            }
                            catch (InterruptedException e) {
                                break block4;
                            }
                        }
                    }
                }
            });
            this.thread.start();
        }
    }
}
