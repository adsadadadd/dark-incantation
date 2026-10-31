/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.plugin.Plugin
 */
package ru.bestrax.essentials.base.objects;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.plugin.Plugin;
import ru.bestrax.essentials.api.VkBot;
import ru.bestrax.essentials.api.manager.VkManager;

public class TrustVkManager
implements VkManager {
    private final Map<String, VkBot> vkBots = new ConcurrentHashMap<String, VkBot>();

    @Override
    public void addVkBot(Plugin plugin, String id, VkBot vkBot) {
        this.vkBots.put(id.toLowerCase(), vkBot);
    }

    @Override
    public void removeVkBot(String id) {
        this.vkBots.remove(id.toLowerCase());
    }

    @Override
    public VkBot getVkBot(String id) {
        return this.vkBots.get(id.toLowerCase());
    }

    @Override
    public Map<String, VkBot> getVkBots() {
        return this.vkBots;
    }
}
