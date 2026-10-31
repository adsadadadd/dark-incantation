package me.x1no4ekk.core.chat.announce;

import me.x1no4ekk.core.CorePlugin;
import me.x1no4ekk.core.chat.AnnouncerModule;
import me.x1no4ekk.core.chat.format.ChatFormatter;
import me.x1no4ekk.core.user.User;
import me.x1no4ekk.core.util.Text;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Rotates the boss bar announcements: one shared {@link BossBar} per entry, shown to online players with
 * {@link User#isBossbarToggle()}. Runs every {@link #PERIOD_TICKS} ticks on the main thread; a reload (new
 * settings instance) rebuilds the bars.
 */
public final class BossBarRotator implements Runnable {

    public static final long PERIOD_TICKS = 2L;
    private static final int TITLE_REFRESH_TICKS = 20;

    private final CorePlugin plugin;
    private final AnnouncerModule module;

    private AnnouncerSettings applied;
    private List<BossBar> bars = List.of();
    private int current = -1;
    private int elapsedTicks;
    private int titleTicks;

    public BossBarRotator(CorePlugin plugin, AnnouncerModule module) {
        this.plugin = plugin;
        this.module = module;
    }

    @Override
    public void run() {
        AnnouncerSettings settings = module.settings();
        if (settings != applied) {
            rebuild(settings);
        }
        if (bars.isEmpty()) {
            return;
        }
        BossBarEntry entry = settings.bars().get(current);
        elapsedTicks += (int) PERIOD_TICKS;
        if (elapsedTicks >= entry.durationTicks()) {
            advance(settings);
            return;
        }
        BossBar bar = bars.get(current);
        bar.progress(entry.progress().at(elapsedTicks, entry.durationTicks()));
        titleTicks += (int) PERIOD_TICKS;
        if (titleTicks >= TITLE_REFRESH_TICKS) {
            titleTicks = 0;
            bar.name(title(entry)); // only changed titles are sent (e.g. online counters)
        }
    }

    private void rebuild(AnnouncerSettings settings) {
        hideAll();
        applied = settings;
        current = -1;
        List<BossBar> created = new ArrayList<>();
        if (settings.barsEnabled()) {
            for (BossBarEntry entry : settings.bars()) {
                created.add(BossBar.bossBar(Component.empty(), 1f, entry.color(), entry.overlay()));
            }
        }
        bars = List.copyOf(created);
        if (!bars.isEmpty()) {
            advance(settings);
        }
    }

    private void advance(AnnouncerSettings settings) {
        int previous = current;
        current = (current + 1) % bars.size();
        elapsedTicks = 0;
        titleTicks = 0;
        BossBarEntry entry = settings.bars().get(current);
        BossBar bar = bars.get(current);
        bar.name(title(entry));
        bar.progress(entry.progress().at(0, entry.durationTicks()));
        BossBar old = previous >= 0 && previous != current ? bars.get(previous) : null;
        // Also syncs players whose data finished loading after they joined.
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (old != null) {
                player.hideBossBar(old);
            }
            if (isEnabledFor(player)) {
                player.showBossBar(bar);
            } else {
                player.hideBossBar(bar);
            }
        }
    }

    private static Component title(BossBarEntry entry) {
        // Shared by all viewers, so only server-wide PlaceholderAPI placeholders make sense here.
        return Text.parse(ChatFormatter.template(null, entry.title()),
                Placeholder.unparsed("online", String.valueOf(Bukkit.getOnlinePlayers().size())));
    }

    private boolean isEnabledFor(Player player) {
        User user = plugin.userManager().getUser(player);
        return user != null && user.isBossbarToggle();
    }

    /**
     * Shows the current bar if the player has boss bars enabled.
     */
    public void show(Player player) {
        if (current >= 0 && current < bars.size() && isEnabledFor(player)) {
            player.showBossBar(bars.get(current));
        }
    }

    public void hide(Player player) {
        for (BossBar bar : bars) {
            player.hideBossBar(bar);
        }
    }

    private void hideAll() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            hide(player);
        }
    }

    /**
     * Hides every bar and forgets them (module disable).
     */
    public void stop() {
        hideAll();
        bars = List.of();
        applied = null;
        current = -1;
    }
}
