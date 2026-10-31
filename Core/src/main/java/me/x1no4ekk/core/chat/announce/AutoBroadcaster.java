package me.x1no4ekk.core.chat.announce;

import me.x1no4ekk.core.CorePlugin;
import me.x1no4ekk.core.chat.AnnouncerModule;
import me.x1no4ekk.core.chat.ChatPermissions;
import me.x1no4ekk.core.chat.format.ChatFormatter;
import me.x1no4ekk.core.util.Text;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Runs once per second and sends the next automatic announcement when the interval has passed.
 * A reload (new settings instance) restarts the interval and the order.
 */
public final class AutoBroadcaster implements Runnable {

    private final CorePlugin plugin;
    private final AnnouncerModule module;

    private AnnouncerSettings applied;
    private long elapsedSeconds;
    private int nextIndex;
    private int lastIndex = -1;

    public AutoBroadcaster(CorePlugin plugin, AnnouncerModule module) {
        this.plugin = plugin;
        this.module = module;
    }

    @Override
    public void run() {
        AnnouncerSettings settings = module.settings();
        if (settings != applied) {
            reset();
            applied = settings;
        }
        if (!settings.autoEnabled() || settings.messages().isEmpty()) {
            return;
        }
        if (++elapsedSeconds < settings.intervalSeconds()) {
            return;
        }
        elapsedSeconds = 0;
        announce(settings, settings.messages().get(pick(settings.messages().size(), settings.random())));
    }

    private int pick(int size, boolean random) {
        int index;
        if (random && size > 1) {
            do {
                index = ThreadLocalRandom.current().nextInt(size);
            } while (index == lastIndex);
        } else {
            index = nextIndex % size;
            nextIndex = (index + 1) % size;
        }
        lastIndex = index;
        return index;
    }

    private void announce(AnnouncerSettings settings, String template) {
        TagResolver common = TagResolver.resolver(plugin.messages().prefixResolver(),
                Placeholder.unparsed("online", String.valueOf(Bukkit.getOnlinePlayers().size())));
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasPermission(ChatPermissions.ANNOUNCER_EXEMPT)) {
                continue;
            }
            player.sendMessage(Text.parse(ChatFormatter.template(player, template), common,
                    Placeholder.component("player", player.displayName()),
                    Placeholder.unparsed("name", player.getName())));
            settings.sound().play(player);
        }
    }

    public void reset() {
        applied = null;
        elapsedSeconds = 0;
        nextIndex = 0;
        lastIndex = -1;
    }
}
