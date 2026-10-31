/*
 * Decompiled with CFR 0.152.
 */
package ru.bestrax.essentials.base.managers;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import ru.bestrax.essentials.api.Kit;
import ru.bestrax.essentials.api.manager.KitManager;

public class TrustKitManager
implements KitManager {
    private Map<String, Kit> kits = new LinkedHashMap<String, Kit>();

    @Override
    public Kit getKit(String name) {
        return this.kits.get(name.toLowerCase());
    }

    @Override
    public Map<String, Kit> getKits() {
        return this.kits;
    }

    @Override
    public List<Kit> getStarterKits() {
        return this.kits.values().stream().filter(Kit::isStart).collect(Collectors.toList());
    }

    @Override
    public void addKit(Kit kit) {
        String name = kit.getName().toLowerCase();
        if (!this.kits.containsKey(name)) {
            this.kits.put(name.toLowerCase(), kit);
        }
    }

    @Override
    public void removeKit(Kit kit) {
        String name = kit.getName();
        this.kits.remove(name.toLowerCase());
    }
}
