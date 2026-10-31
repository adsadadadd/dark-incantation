package me.x1no4ekk.core.travel.command;

import me.x1no4ekk.core.command.CommandException;
import me.x1no4ekk.core.travel.TpaManager;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;

import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Picks one request out of a player's pending ones for /tpaccept, /tpdeny and /tpcancel.
 */
final class TpaRequests {

    private TpaRequests() {
    }

    /**
     * @param requests    candidates (non-empty)
     * @param name        typed name, or {@code null} when omitted
     * @param otherName   name of the other side of a request
     * @param multipleKey message shown when the name is omitted but several requests exist
     * @param notFoundKey message shown when no request matches the name
     */
    static TpaManager.Request select(List<TpaManager.Request> requests, String name,
                                     Function<TpaManager.Request, String> otherName,
                                     String multipleKey, String notFoundKey) {
        if (name == null) {
            if (requests.size() == 1) {
                return requests.getFirst();
            }
            String names = requests.stream().map(otherName).collect(Collectors.joining(", "));
            throw new CommandException(multipleKey, Placeholder.unparsed("players", names));
        }
        for (TpaManager.Request request : requests) {
            if (otherName.apply(request).equalsIgnoreCase(name)) {
                return request;
            }
        }
        String prefix = name.toLowerCase(Locale.ROOT);
        TpaManager.Request match = null;
        for (TpaManager.Request request : requests) {
            if (otherName.apply(request).toLowerCase(Locale.ROOT).startsWith(prefix)) {
                if (match != null) {
                    match = null;
                    break;
                }
                match = request;
            }
        }
        if (match == null) {
            throw new CommandException(notFoundKey, Placeholder.unparsed("player", name));
        }
        return match;
    }
}
