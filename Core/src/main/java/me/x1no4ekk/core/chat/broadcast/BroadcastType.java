package me.x1no4ekk.core.chat.broadcast;

import me.x1no4ekk.core.config.SoundEffect;

import java.util.List;

/**
 * One broadcast command from chat.yml {@code broadcasts}.
 *
 * @param linkOnly     the message must be a single link to one of {@code allowedLinks} (streams, videos)
 * @param allowedLinks lower-case domains; subdomains are accepted
 */
public record BroadcastType(
        String id,
        String command,
        String permission,
        String format,
        long cooldownMillis,
        double price,
        boolean linkOnly,
        List<String> allowedLinks,
        SoundEffect sound
) {

    public String usageKey() {
        return "broadcast.usage." + id;
    }
}
