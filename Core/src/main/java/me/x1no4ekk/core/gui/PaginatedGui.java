package me.x1no4ekk.core.gui;

import me.x1no4ekk.core.config.MenuItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * Menu that shows a list of entries over several pages.
 * <p>
 * Subclasses provide the icon and click action per entry, and may decorate the menu (fillers, static buttons)
 * in {@link #decorate()}. Entries must be immutable snapshots (records, names, UUIDs) — never players or entities.
 *
 * @param <T> entry type
 */
public abstract class PaginatedGui<T> extends Gui {

    private final List<Integer> contentSlots;
    private List<T> entries = List.of();
    private int page;

    protected PaginatedGui(int rows, Component title, List<Integer> contentSlots) {
        super(rows, title);
        if (contentSlots.isEmpty()) {
            throw new IllegalArgumentException("contentSlots must not be empty");
        }
        this.contentSlots = List.copyOf(contentSlots);
    }

    /**
     * Replaces the entries (copied) and re-renders the current page.
     */
    protected void setEntries(List<T> entries) {
        this.entries = List.copyOf(entries);
        this.page = Math.clamp(page, 0, pageCount() - 1);
        render();
    }

    protected List<T> entries() {
        return entries;
    }

    public int page() {
        return page;
    }

    public int pageCount() {
        return Math.max(1, (entries.size() + contentSlots.size() - 1) / contentSlots.size());
    }

    protected void setPage(int page) {
        this.page = Math.clamp(page, 0, pageCount() - 1);
        render();
    }

    /**
     * Redraws the whole menu: decoration, entries of the current page, navigation.
     */
    protected final void render() {
        clear();
        decorate();
        if (entries.isEmpty()) {
            renderEmpty();
        } else {
            int perPage = contentSlots.size();
            int start = page * perPage;
            int end = Math.min(entries.size(), start + perPage);
            for (int i = start; i < end; i++) {
                T entry = entries.get(i);
                setItem(contentSlots.get(i - start), icon(entry), (player, event) -> onEntryClick(player, entry, event));
            }
        }
        renderNavigation();
        afterRender();
    }

    private void renderNavigation() {
        MenuItem previous = previousButton();
        MenuItem next = nextButton();
        TagResolver pages = TagResolver.resolver(
                Placeholder.unparsed("page", String.valueOf(page + 1)),
                Placeholder.unparsed("pages", String.valueOf(pageCount())));
        if (previous != null && previous.slot() >= 0 && page > 0) {
            setItem(previous.slot(), previous.build(pages), (player, event) -> setPage(page - 1));
        }
        if (next != null && next.slot() >= 0 && page < pageCount() - 1) {
            setItem(next.slot(), next.build(pages), (player, event) -> setPage(page + 1));
        }
    }

    /**
     * Icon of an entry on the current page.
     */
    protected abstract ItemStack icon(T entry);

    /**
     * Click on an entry. Close/open other inventories via {@link Gui#nextTick}.
     */
    protected abstract void onEntryClick(Player player, T entry, InventoryClickEvent event);

    /**
     * Static content drawn before the entries (fillers, info, back/close buttons).
     */
    protected void decorate() {
    }

    /**
     * Drawn instead of entries when the list is empty.
     */
    protected void renderEmpty() {
    }

    /**
     * Drawn last, after entries and navigation (e.g. buttons that must stay on top of fillers).
     */
    protected void afterRender() {
    }

    /**
     * "Previous page" button (placeholders {@code <page>}, {@code <pages>}), or {@code null} for none.
     */
    protected abstract MenuItem previousButton();

    /**
     * "Next page" button (placeholders {@code <page>}, {@code <pages>}), or {@code null} for none.
     */
    protected abstract MenuItem nextButton();
}
