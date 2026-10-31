package me.x1no4ekk.core.warp.command;

import me.x1no4ekk.core.warp.Warp;
import me.x1no4ekk.core.warp.WarpModule;

import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;

/**
 * Tab completion of warp names.
 */
final class WarpNames {

    private static final int MAX_SUGGESTIONS = 100;

    private WarpNames() {
    }

    static List<String> all(WarpModule module, String input) {
        return matching(module, input, warp -> true);
    }

    static List<String> matching(WarpModule module, String input, Predicate<Warp> filter) {
        if (!module.manager().isLoaded()) {
            return List.of();
        }
        String prefix = input.toLowerCase(Locale.ROOT);
        return module.manager().find(warp -> filter.test(warp) && warp.key().startsWith(prefix)).stream()
                .limit(MAX_SUGGESTIONS)
                .map(Warp::name)
                .toList();
    }
}
