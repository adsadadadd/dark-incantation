/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.md_5.bungee.api.ChatMessageType
 *  net.md_5.bungee.api.chat.BaseComponent
 *  net.md_5.bungee.api.chat.TextComponent
 *  org.bukkit.Bukkit
 *  org.bukkit.ChatColor
 *  org.bukkit.OfflinePlayer
 *  org.bukkit.Sound
 *  org.bukkit.command.CommandSender
 *  org.bukkit.configuration.ConfigurationSection
 *  org.bukkit.configuration.file.FileConfiguration
 *  org.bukkit.entity.Entity
 *  org.bukkit.entity.Player
 */
package ru.bestrax.essentials.base.managers;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import ru.bestrax.essentials.api.EssentialsApi;
import ru.bestrax.essentials.api.config.AbstractConfig;
import ru.bestrax.essentials.api.config.Config;
import ru.bestrax.essentials.api.manager.FileManager;
import ru.bestrax.essentials.api.manager.MoneyKillerManager;
import ru.bestrax.essentials.api.manager.MoneyManager;
import ru.bestrax.essentials.utils.TrustItemsUtils;

public class TrustMoneyKillerManager
implements MoneyKillerManager {
    private boolean enabled = false;
    private int playerKillProcent = 0;
    private Map<String, String> entity;
    private Random random;
    private MoneyManager MONEY_MANAGER = EssentialsApi.getMoneyManager();
    private Config config;
    private String playerPlayerActionBar;
    private String playerPlayerMessage;
    private String targetActionBar;
    private String targetMessage;
    private String playerPlayerSound;
    private List<String> commands;
    private Map<String, EntitySetting> entities;

    public TrustMoneyKillerManager() {
        this.random = new Random();
        this.entity = new ConcurrentHashMap<String, String>();
        this.entities = new ConcurrentHashMap<String, EntitySetting>();
    }

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }

    @Override
    public void killPlayer(Player killer, Player deather) {
        if (killer == null) {
            return;
        }
        if (deather == null) {
            return;
        }
        if (!this.isEnabled()) {
            return;
        }
        int reward = (int)(this.MONEY_MANAGER.get((OfflinePlayer)deather) * (double)this.playerKillProcent / 100.0);
        if (this.playerPlayerActionBar != null) {
            killer.spigot().sendMessage(ChatMessageType.ACTION_BAR, (BaseComponent)new TextComponent(this.config.replace(this.playerPlayerActionBar, "%money%", String.valueOf(reward), "%player%", deather.getDisplayName())));
        }
        if (this.playerPlayerMessage != null) {
            killer.sendMessage(this.config.replace(this.playerPlayerMessage, "%money%", String.valueOf(reward), "%player%", deather.getDisplayName()));
        }
        if (this.targetActionBar != null) {
            deather.spigot().sendMessage(ChatMessageType.ACTION_BAR, (BaseComponent)new TextComponent(this.config.replace(this.targetActionBar, "%money%", String.valueOf(reward), "%player%", killer.getName())));
        }
        if (this.commands != null) {
            for (String command : this.commands) {
                Bukkit.dispatchCommand((CommandSender)Bukkit.getConsoleSender(), (String)command.replace("%player%", killer.getName()).replace("%target%", deather.getName()));
            }
        }
        if (this.targetMessage != null) {
            deather.sendMessage(this.config.replace(this.targetMessage, "%money%", String.valueOf(reward), "%player%", killer.getName()));
        }
        if (this.playerPlayerSound != null) {
            killer.playSound(killer.getLocation(), Sound.valueOf((String)this.playerPlayerSound.toUpperCase()), 1.0f, 1.0f);
        }
        this.MONEY_MANAGER.withdraw((OfflinePlayer)deather, (double)reward);
        this.MONEY_MANAGER.give((OfflinePlayer)killer, (double)reward);
    }

    @Override
    public void killEntity(Player killer, Entity deather) {
        if (killer == null) {
            return;
        }
        if (deather == null) {
            return;
        }
        if (deather instanceof Player) {
            this.killPlayer(killer, (Player)deather);
            return;
        }
        EntitySetting entitySetting = this.entities.get(deather.getType().toString().toLowerCase());
        if (entitySetting == null) {
            return;
        }
        int pay = entitySetting.getPay();
        if (Bukkit.getPluginManager().isPluginEnabled("TrustItems")) {
            pay *= TrustItemsUtils.getMultiplierOfMobsMoney(killer);
        }
        if (entitySetting.getActionbar() != null) {
            killer.spigot().sendMessage(ChatMessageType.ACTION_BAR, (BaseComponent)new TextComponent(entitySetting.getActionbar().replace("%pay%", String.valueOf(pay))));
        }
        if (entitySetting.getCommands() != null) {
            for (String command : entitySetting.getCommands()) {
                Bukkit.dispatchCommand((CommandSender)Bukkit.getConsoleSender(), (String)command.replace("%player%", killer.getName()).replace("%pay%", String.valueOf(pay)));
            }
        }
        if (entitySetting.getMessage() != null) {
            killer.sendMessage(entitySetting.getMessage().replace("%pay%", String.valueOf(pay)));
        }
        if (entitySetting.getSound() != null) {
            killer.playSound(killer.getLocation(), Sound.valueOf((String)entitySetting.getSound().toUpperCase()), 1.0f, 1.0f);
        }
        this.MONEY_MANAGER.give((OfflinePlayer)killer, (double)pay);
    }

    @Override
    public void reloadConfig() {
        ConfigurationSection entitiesSection;
        FileManager fileManager = EssentialsApi.getFileManager();
        this.config = new AbstractConfig();
        FileConfiguration fileConfiguration = fileManager.getFile(((Object)((Object)EssentialsApi.getInstance())).getClass(), EssentialsApi.getInstance().getDataFolder(), "killermoney.yml");
        this.config.reload(fileConfiguration);
        ConfigurationSection playerSection = fileConfiguration.getConfigurationSection("player");
        this.enabled = EssentialsApi.getInstance().getSettings().getBoolean("killermoney");
        if (playerSection != null) {
            this.playerKillProcent = playerSection.getInt("procent");
            this.playerPlayerActionBar = this.color(playerSection.getString("actionbar"));
            this.playerPlayerMessage = this.color(playerSection.getString("message"));
            this.playerPlayerSound = this.color(playerSection.getString("sound"));
            this.targetMessage = this.color(playerSection.getString("target_message"));
            this.targetActionBar = this.color(playerSection.getString("target_actionbar"));
            this.commands = playerSection.getStringList("commands");
        }
        if ((entitiesSection = fileConfiguration.getConfigurationSection("entities")) != null) {
            for (String key : entitiesSection.getKeys(false)) {
                ConfigurationSection entitySection = entitiesSection.getConfigurationSection(key);
                if (entitySection == null) continue;
                EntitySetting entitySetting = new EntitySetting(this.color(entitySection.getString("message")), this.color(entitySection.getString("actionbar")), this.color(entitySection.getString("sound")), this.color(entitySection.getString("pay")), entitySection.getStringList("commands"));
                this.entities.put(key.toLowerCase(), entitySetting);
            }
        }
    }

    private String color(String text) {
        if (text == null) {
            return text;
        }
        return ChatColor.translateAlternateColorCodes((char)'&', (String)text);
    }

    private class EntitySetting {
        private String actionbar;
        private String message;
        private String sound;
        private String pay;
        private List<String> commands;

        public EntitySetting(String message, String actionbar, String sound, String pay, List<String> commands) {
            this.message = message;
            this.actionbar = actionbar;
            this.sound = sound;
            this.pay = pay;
            this.commands = commands;
        }

        public List<String> getCommands() {
            return this.commands;
        }

        public String getActionbar() {
            return this.actionbar;
        }

        public String getMessage() {
            return this.message;
        }

        public String getSound() {
            return this.sound;
        }

        public int getPay() {
            if (this.pay == null) {
                return -1;
            }
            if (this.pay.contains("-")) {
                int lowerbound = Integer.parseInt(this.pay.split("-")[0]);
                int upperbound = Integer.parseInt(this.pay.split("-")[1]);
                return TrustMoneyKillerManager.this.random.nextInt(upperbound - lowerbound) + lowerbound;
            }
            return Integer.parseInt(this.pay);
        }
    }
}
