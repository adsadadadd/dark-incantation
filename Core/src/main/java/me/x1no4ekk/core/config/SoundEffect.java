package me.x1no4ekk.core.config;

import net.kyori.adventure.key.InvalidKeyException;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.Locale;

/**
 * Configurable sound:
 * <pre>
 * sound:
 *   key: 'minecraft:entity.experience_orb.pickup'
 *   volume: 1.0
 *   pitch: 1.0
 * </pre>
 * An empty key disables the sound.
 */
public record SoundEffect(Sound sound) {

    public static final SoundEffect NONE = new SoundEffect(null);

    /**
     * @return the sound, or {@link #NONE} if the section is missing or its key is empty
     */
    public static SoundEffect load(ConfigurationSection parent, String key) {
        ConfigurationSection section = parent.getConfigurationSection(key);
        if (section == null) {
            return NONE;
        }
        String raw = section.getString("key", "");
        if (raw == null || raw.isBlank()) {
            return NONE;
        }
        Key soundKey;
        try {
            soundKey = Key.key(raw.trim().toLowerCase(Locale.ROOT));
        } catch (InvalidKeyException e) {
            throw new IllegalArgumentException(ConfigUtil.path(section, "key") + ": invalid sound key '" + raw
                    + "' (expected e.g. minecraft:entity.experience_orb.pickup)");
        }
        float volume = (float) section.getDouble("volume", 1.0);
        float pitch = (float) section.getDouble("pitch", 1.0);
        return new SoundEffect(Sound.sound(soundKey, Sound.Source.MASTER, volume, pitch));
    }

    public boolean isEnabled() {
        return sound != null;
    }

    /**
     * Plays the sound to the player only, following them.
     */
    public void play(Player player) {
        if (sound != null) {
            player.playSound(sound, Sound.Emitter.self());
        }
    }
}
