/*
 * Decompiled with CFR 0.152.
 */
package ru.bestrax.essentials.base.managers;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import ru.bestrax.essentials.api.EssentialsApi;
import ru.bestrax.essentials.api.config.Config;
import ru.bestrax.essentials.api.manager.HomeManager;

public class TrustHomeManager
implements HomeManager {
    private Map<String, Integer> homesLimit = new ConcurrentHashMap<String, Integer>();

    public TrustHomeManager() {
        this.loadHomeLimit();
    }

    @Override
    public void loadHomeLimit() {
        Config settings = EssentialsApi.instance.getSettings();
        this.homesLimit.clear();
        this.homesLimit.put("default", settings.getInt("setHomeDefault"));
        for (String string : settings.getStringList("setHomeLimit", new String[0])) {
            String groupName;
            if (!string.contains(":") || (groupName = string.split(":")[0]).equalsIgnoreCase("default")) continue;
            int limit = Integer.parseInt(string.split(":")[1]);
            this.homesLimit.put(groupName, limit);
        }
    }

    @Override
    public int getHomeLimit(String group) {
        return this.homesLimit.getOrDefault(group, 0);
    }
}
