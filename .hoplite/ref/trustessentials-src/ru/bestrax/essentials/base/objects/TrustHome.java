/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Location
 */
package ru.bestrax.essentials.base.objects;

import org.bukkit.Location;
import ru.bestrax.essentials.api.Home;

public class TrustHome
implements Home {
    private String name;
    private Location location;

    public TrustHome(String name, Location location) {
        this.location = location;
        this.name = name;
    }

    @Override
    public String getName() {
        return this.name;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    @Override
    public Location getLocation() {
        return this.location;
    }
}
