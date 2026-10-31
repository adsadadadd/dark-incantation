package me.x1no4ekk.core.enderchest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Live state of one player's ender chest while anybody views it (main thread only).
 * <p>
 * Stored pages are kept serialized. A page inventory exists only while it has viewers and is shared by all of them,
 * so two viewers can never hold two copies of the same items. The session is dropped when its last page is released.
 */
final class EnderChestSession {

    private final UUID owner;
    private final String ownerName;
    private final Map<Integer, byte[]> pages;
    private final Map<Integer, EnderChestHolder> live = new HashMap<>();
    private int boughtPages;

    EnderChestSession(UUID owner, String ownerName, Map<Integer, byte[]> pages, int boughtPages) {
        this.owner = owner;
        this.ownerName = ownerName;
        this.pages = new HashMap<>(pages);
        this.boughtPages = boughtPages;
    }

    UUID owner() {
        return owner;
    }

    String ownerName() {
        return ownerName;
    }

    int boughtPages() {
        return boughtPages;
    }

    void setBoughtPages(int boughtPages) {
        this.boughtPages = boughtPages;
    }

    byte[] page(int page) {
        return pages.get(page);
    }

    /**
     * @param contents serialized items, or {@code null} for an empty page
     */
    void setPage(int page, byte[] contents) {
        if (contents == null) {
            pages.remove(page);
        } else {
            pages.put(page, contents);
        }
    }

    /**
     * Number of pages up to the last stored or currently open one.
     */
    int usedPageCount() {
        int count = 0;
        for (int page : pages.keySet()) {
            count = Math.max(count, page + 1);
        }
        for (int page : live.keySet()) {
            count = Math.max(count, page + 1);
        }
        return count;
    }

    EnderChestHolder live(int page) {
        return live.get(page);
    }

    void putLive(int page, EnderChestHolder holder) {
        live.put(page, holder);
    }

    void removeLive(int page) {
        live.remove(page);
    }

    List<Integer> livePages() {
        return List.copyOf(live.keySet());
    }

    boolean isIdle() {
        return live.isEmpty();
    }
}
