package me.x1no4ekk.core.top;

import java.util.UUID;

/**
 * One leaderboard row.
 */
public record TopEntry(UUID uuid, String name, double value) {
}
