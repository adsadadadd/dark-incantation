package me.x1no4ekk.core.utility.info;

import me.x1no4ekk.core.CorePlugin;
import me.x1no4ekk.core.command.BaseCommand;
import org.bukkit.command.CommandSender;

import java.time.Instant;

/**
 * {@code /date}: server date and time in the configured time zone.
 */
final class DateCommand extends BaseCommand {

    private final InfoModule module;

    DateCommand(CorePlugin plugin, InfoModule module) {
        super(plugin);
        this.module = module;
    }

    @Override
    protected void execute(CommandSender sender, String label, String[] args) {
        requirePermission(sender, "core.date");
        InfoSettings.Dates dates = module.settings().dates();
        Instant now = Instant.now();
        send(sender, "utility.date.message",
                placeholder("date", dates.date().format(now)),
                placeholder("time", dates.time().format(now)),
                placeholder("day", dates.day().format(now)),
                placeholder("zone", dates.zone().getId()));
    }
}
