package me.x1no4ekk.core.utility.workstation;

import me.x1no4ekk.core.config.ConfigUtil;
import org.bukkit.configuration.ConfigurationSection;

import java.util.EnumMap;
import java.util.Map;

/**
 * {@code utility.workstations}: window titles of the virtual workstations.
 */
public record WorkstationSettings(Map<Workstation, String> titles) {

    public static WorkstationSettings load(ConfigurationSection utility) {
        ConfigurationSection titles = ConfigUtil.section(ConfigUtil.section(utility, "workstations"), "titles");
        Map<Workstation, String> loaded = new EnumMap<>(Workstation.class);
        for (Workstation workstation : Workstation.values()) {
            loaded.put(workstation, ConfigUtil.requireText(titles, workstation.command()));
        }
        return new WorkstationSettings(Map.copyOf(loaded));
    }

    public String title(Workstation workstation) {
        return titles.get(workstation);
    }
}
