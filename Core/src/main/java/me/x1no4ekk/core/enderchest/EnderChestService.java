package me.x1no4ekk.core.enderchest;

import me.x1no4ekk.core.CorePlugin;
import me.x1no4ekk.core.config.MenuItem;
import me.x1no4ekk.core.gui.Gui;
import me.x1no4ekk.core.hook.EconomyHook;
import me.x1no4ekk.core.util.Text;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;

/**
 * Multi-page ender chests stored in MySQL (main thread only).
 * <p>
 * Duplication safety: every owner has at most one {@link EnderChestSession}, and every page at most one inventory,
 * shared by all its viewers. A page is serialized and queued for saving on the serial writer when its last viewer
 * leaves; chests are loaded through the same writer, so a load can never overtake a pending save. Views that are no
 * longer the live page of a session (should never happen) are made read-only.
 */
final class EnderChestService {

    static final String PERMISSION = "core.enderchest";
    static final String OTHERS_PERMISSION = "core.enderchest.others";
    static final String OTHERS_EDIT_PERMISSION = "core.enderchest.others.edit";
    static final String PAGES_PERMISSION_PREFIX = "core.enderchest.pages.";

    /** Actions on a locked page's slot that only take items out. */
    private static final Set<InventoryAction> WITHDRAW_ACTIONS = EnumSet.of(
            InventoryAction.NOTHING, InventoryAction.PICKUP_ALL, InventoryAction.PICKUP_SOME,
            InventoryAction.PICKUP_HALF, InventoryAction.PICKUP_ONE, InventoryAction.MOVE_TO_OTHER_INVENTORY,
            InventoryAction.DROP_ALL_SLOT, InventoryAction.DROP_ONE_SLOT, InventoryAction.COLLECT_TO_CURSOR,
            InventoryAction.CLONE_STACK, InventoryAction.PICKUP_FROM_BUNDLE,
            InventoryAction.PICKUP_ALL_INTO_BUNDLE, InventoryAction.PICKUP_SOME_INTO_BUNDLE);

    private final CorePlugin plugin;
    private final EnderChestModule module;
    private final EnderChestRepository repository;
    private final NamespacedKey buttonKey;
    private final Map<UUID, EnderChestSession> sessions = new HashMap<>();
    /** Owner → viewers waiting for the chest to load. Removed when the load completes. */
    private final Map<UUID, List<UUID>> pending = new HashMap<>();

    EnderChestService(CorePlugin plugin, EnderChestModule module, EnderChestRepository repository) {
        this.plugin = plugin;
        this.module = module;
        this.repository = repository;
        this.buttonKey = new NamespacedKey(plugin, "enderchest_button");
    }

    // ---------------------------------------------------------------- opening

    /**
     * Opens the first page of {@code owner}'s chest for {@code viewer}, loading it first if nobody views it.
     */
    void open(Player viewer, UUID owner, String ownerName) {
        EnderChestSession session = sessions.get(owner);
        if (session != null) {
            openPage(viewer, session, 0, true);
            return;
        }
        UUID viewerId = viewer.getUniqueId();
        List<UUID> waiting = pending.get(owner);
        if (waiting != null) {
            if (!waiting.contains(viewerId)) {
                waiting.add(viewerId);
            }
            return;
        }
        waiting = new ArrayList<>();
        waiting.add(viewerId);
        pending.put(owner, waiting);
        AtomicReference<EnderChestRepository.StoredChest> loaded = new AtomicReference<>();
        // Read on the serial writer: it then runs after every queued save of this chest.
        plugin.database().update(connection -> loaded.set(repository.load(connection, owner)))
                .whenCompleteAsync((ignored, error) -> onLoaded(owner, ownerName, loaded.get(), error), plugin.mainThread());
    }

    private void onLoaded(UUID owner, String ownerName, EnderChestRepository.StoredChest stored, Throwable error) {
        List<UUID> waiting = pending.remove(owner);
        if (waiting == null) {
            return; // module disabled meanwhile
        }
        if (error != null || stored == null) {
            plugin.getLogger().log(Level.SEVERE, "Failed to load the ender chest of " + ownerName + " (" + owner + ")", error);
            for (UUID viewerId : waiting) {
                Player viewer = Bukkit.getPlayer(viewerId);
                if (viewer != null) {
                    plugin.messages().send(viewer, "general.database-error");
                }
            }
            return;
        }
        EnderChestSession session = sessions.computeIfAbsent(owner,
                id -> new EnderChestSession(owner, ownerName, stored.pages(), stored.boughtPages()));
        for (UUID viewerId : waiting) {
            Player viewer = Bukkit.getPlayer(viewerId);
            if (viewer != null) {
                openPage(viewer, session, 0, true);
            }
        }
        if (session.isIdle()) {
            sessions.remove(owner, session);
        }
    }

    private void openPage(Player viewer, EnderChestSession session, int requestedPage, boolean firstOpen) {
        int page = Math.clamp(requestedPage, 0, pageLimit(session) - 1);
        EnderChestHolder holder = session.live(page);
        if (holder != null && viewer.getOpenInventory().getTopInventory().getHolder(false) == holder) {
            return; // already viewing it; reopening would release the page under the viewer
        }
        if (holder == null) {
            try {
                holder = createPage(session, page, viewer.getUniqueId().equals(session.owner()));
            } catch (RuntimeException e) {
                // Corrupted data stays untouched in MySQL: the page is not opened, so it is never overwritten.
                plugin.getLogger().log(Level.SEVERE, "Cannot read page " + page + " of the ender chest of "
                        + session.ownerName() + " (" + session.owner() + ")", e);
                plugin.messages().send(viewer, "general.database-error");
                if (session.isIdle()) {
                    sessions.remove(session.owner(), session);
                }
                return;
            }
            session.putLive(page, holder);
        }
        InventoryView view = viewer.openInventory(holder.getInventory());
        if (view == null || view.getTopInventory().getHolder(false) != holder) {
            releaseIfUnused(session, page, null);
            return;
        }
        EnderChestSettings config = module.settings();
        (firstOpen ? config.openSound() : config.pageSound()).play(viewer);
    }

    private EnderChestHolder createPage(EnderChestSession session, int page, boolean openedByOwner) {
        EnderChestSettings config = module.settings();
        int storageSize = config.storageSize();
        byte[] data = session.page(page);
        ItemStack[] stored = data == null ? new ItemStack[0] : ItemStack.deserializeItemsFromBytes(data);
        ItemStack[] overflow = stored.length > storageSize
                ? Arrays.copyOfRange(stored, storageSize, stored.length)
                : new ItemStack[0];
        EnderChestHolder holder = new EnderChestHolder(session.owner(), page, storageSize, overflow);
        TagResolver placeholders = TagResolver.resolver(
                Placeholder.unparsed("page", String.valueOf(page + 1)),
                Placeholder.unparsed("pages", String.valueOf(pageLimit(session))),
                Placeholder.unparsed("player", session.ownerName()));
        Component title = Text.parse(openedByOwner ? config.title() : config.titleOther(), placeholders);
        Inventory inventory = Bukkit.createInventory(holder, storageSize + 9, title);
        holder.attach(inventory);
        for (int slot = 0; slot < Math.min(storageSize, stored.length); slot++) {
            ItemStack item = stored[slot];
            if (item != null && !item.isEmpty()) {
                inventory.setItem(slot, item);
            }
        }
        renderNavigation(session, holder);
        return holder;
    }

    // ---------------------------------------------------------------- limits

    /**
     * Pages the owner may use: {@code default-pages} or the highest {@code core.enderchest.pages.<n>} (only while the
     * owner is online) plus purchased pages, capped by {@code max-pages}.
     */
    int ownerLimit(EnderChestSession session) {
        EnderChestSettings config = module.settings();
        int base = config.defaultPages();
        Player owner = Bukkit.getPlayer(session.owner());
        if (owner != null) {
            for (int pages = config.maxPages(); pages > base; pages--) {
                if (owner.hasPermission(PAGES_PERMISSION_PREFIX + pages)) {
                    base = pages;
                    break;
                }
            }
        }
        return Math.min(config.maxPages(), base + session.boughtPages());
    }

    /**
     * Navigable pages: the owner's limit, extended to every page that still holds items so nothing is ever
     * unreachable. Pages above the owner's limit are withdraw-only for the owner.
     */
    private int pageLimit(EnderChestSession session) {
        return Math.max(1, Math.max(ownerLimit(session), session.usedPageCount()));
    }

    private boolean canBuyMore(EnderChestSession session) {
        List<Double> prices = module.settings().pagePrices();
        return session.boughtPages() < prices.size() && ownerLimit(session) < module.settings().maxPages();
    }

    private boolean isLocked(Player viewer, EnderChestSession session, EnderChestHolder holder) {
        return viewer.getUniqueId().equals(session.owner()) && holder.page() >= ownerLimit(session);
    }

    private static boolean canEdit(Player viewer, EnderChestHolder holder) {
        return viewer.getUniqueId().equals(holder.owner()) || viewer.hasPermission(OTHERS_EDIT_PERMISSION);
    }

    // ---------------------------------------------------------------- navigation row

    private void renderNavigation(EnderChestSession session, EnderChestHolder holder) {
        EnderChestSettings config = module.settings();
        Inventory inventory = holder.getInventory();
        int base = holder.storageSize();
        int page = holder.page();
        int limit = pageLimit(session);
        boolean buyable = canBuyMore(session);
        EconomyHook economy = plugin.hooks().economy();
        TagResolver placeholders = TagResolver.resolver(
                Placeholder.unparsed("page", String.valueOf(page + 1)),
                Placeholder.unparsed("pages", String.valueOf(limit)),
                Placeholder.unparsed("player", session.ownerName()),
                Placeholder.unparsed("price", buyable ? economy.format(config.pagePrices().get(session.boughtPages())) : "—"),
                Placeholder.unparsed("bought", String.valueOf(session.boughtPages())),
                Placeholder.unparsed("purchasable", String.valueOf(config.pagePrices().size())));

        ItemStack filler = button(config.filler(), placeholders);
        for (int slot = 0; slot < 9; slot++) {
            inventory.setItem(base + slot, filler);
        }
        MenuItem info = page >= ownerLimit(session) ? config.lockedInfo() : config.pageInfo();
        inventory.setItem(base + info.slot(), button(info, placeholders));
        if (buyable) {
            inventory.setItem(base + config.buyPage().slot(), button(config.buyPage(), placeholders));
        }
        if (page > 0) {
            inventory.setItem(base + config.previousPage().slot(), button(config.previousPage(), placeholders));
        }
        if (page + 1 < limit) {
            inventory.setItem(base + config.nextPage().slot(), button(config.nextPage(), placeholders));
        }
    }

    /**
     * Navigation items carry a PDC marker, so no player item can ever stack onto them.
     */
    private ItemStack button(MenuItem item, TagResolver placeholders) {
        ItemStack stack = item.build(placeholders);
        stack.editPersistentDataContainer(pdc -> pdc.set(buttonKey, PersistentDataType.BYTE, (byte) 1));
        return stack;
    }

    /**
     * Redraws the navigation of every open page (after a purchase or a reload).
     */
    void refreshNavigation() {
        for (EnderChestSession session : List.copyOf(sessions.values())) {
            for (int page : session.livePages()) {
                renderNavigation(session, session.live(page));
            }
        }
    }

    // ---------------------------------------------------------------- inventory events

    void handleClick(Player player, EnderChestHolder holder, InventoryClickEvent event) {
        Inventory top = holder.getInventory();
        int rawSlot = event.getRawSlot();
        int storageSize = holder.storageSize();
        EnderChestSession session = sessions.get(holder.owner());
        boolean live = session != null && session.live(holder.page()) == holder;

        if (rawSlot >= storageSize && rawSlot < top.getSize()) {
            event.setCancelled(true);
            if (live) {
                clickNavigation(player, session, holder, rawSlot - storageSize);
            }
            return;
        }
        if (!live || !canEdit(player, holder)) {
            event.setCancelled(true);
            return;
        }
        boolean clickedTop = rawSlot >= 0 && rawSlot < storageSize;
        InventoryAction action = event.getAction();
        if (action == InventoryAction.COLLECT_TO_CURSOR && navigationHolds(holder, event.getCursor())) {
            event.setCancelled(true);
            return;
        }
        if (isLocked(player, session, holder)) {
            boolean adds = clickedTop ? !WITHDRAW_ACTIONS.contains(action) : action == InventoryAction.MOVE_TO_OTHER_INVENTORY;
            if (adds) {
                event.setCancelled(true);
                plugin.messages().sendActionBar(player, "enderchest.locked-page");
            }
        }
    }

    void handleDrag(Player player, EnderChestHolder holder, InventoryDragEvent event) {
        int storageSize = holder.storageSize();
        int topSize = holder.getInventory().getSize();
        boolean touchesTop = false;
        for (int rawSlot : event.getRawSlots()) {
            if (rawSlot >= storageSize && rawSlot < topSize) {
                event.setCancelled(true);
                return;
            }
            touchesTop |= rawSlot < storageSize;
        }
        if (!touchesTop) {
            return;
        }
        EnderChestSession session = sessions.get(holder.owner());
        boolean live = session != null && session.live(holder.page()) == holder;
        if (!live || !canEdit(player, holder) || isLocked(player, session, holder)) {
            event.setCancelled(true);
        }
    }

    private boolean navigationHolds(EnderChestHolder holder, ItemStack cursor) {
        if (cursor == null || cursor.isEmpty()) {
            return false;
        }
        Inventory inventory = holder.getInventory();
        for (int slot = holder.storageSize(); slot < inventory.getSize(); slot++) {
            ItemStack item = inventory.getItem(slot);
            if (item != null && item.isSimilar(cursor)) {
                return true;
            }
        }
        return false;
    }

    private void clickNavigation(Player player, EnderChestSession session, EnderChestHolder holder, int slot) {
        EnderChestSettings config = module.settings();
        int page = holder.page();
        UUID owner = holder.owner();
        if (slot == config.previousPage().slot() && page > 0) {
            switchPage(player, owner, page - 1);
        } else if (slot == config.nextPage().slot() && page + 1 < pageLimit(session)) {
            switchPage(player, owner, page + 1);
        } else if (slot == config.buyPage().slot() && canBuyMore(session)) {
            Gui.nextTick(player, online -> buyPage(online, owner));
        }
    }

    private void switchPage(Player clicker, UUID owner, int page) {
        Gui.nextTick(clicker, player -> {
            EnderChestSession session = sessions.get(owner);
            if (session != null && player.getOpenInventory().getTopInventory().getHolder(false) instanceof EnderChestHolder open
                    && open.owner().equals(owner)) {
                openPage(player, session, page, false);
            }
        });
    }

    private void buyPage(Player player, UUID owner) {
        EnderChestSession session = sessions.get(owner);
        if (session == null) {
            return;
        }
        if (!player.getUniqueId().equals(owner)) {
            plugin.messages().send(player, "enderchest.buy-not-owner");
            return;
        }
        if (!canBuyMore(session)) {
            plugin.messages().send(player, "enderchest.buy-max");
            return;
        }
        EconomyHook economy = plugin.hooks().economy();
        if (!economy.isAvailable()) {
            plugin.messages().send(player, "general.economy-unavailable");
            return;
        }
        double price = module.settings().pagePrices().get(session.boughtPages());
        TagResolver pricePlaceholder = Placeholder.unparsed("price", economy.format(price));
        if (!economy.has(player, price) || !economy.withdraw(player, price)) {
            plugin.messages().send(player, "general.not-enough-money", pricePlaceholder);
            return;
        }
        int bought = session.boughtPages() + 1;
        session.setBoughtPages(bought);
        plugin.database().update(connection -> repository.saveBoughtPages(connection, owner, bought))
                .whenComplete((ignored, error) -> {
                    if (error != null) {
                        plugin.getLogger().log(Level.SEVERE, "Failed to save purchased ender chest pages (" + bought
                                + ") of " + session.ownerName() + " (" + owner + ")", error);
                        notifyDatabaseError(owner);
                    }
                });
        int newPage = ownerLimit(session);
        plugin.messages().send(player, "enderchest.page-bought",
                Placeholder.unparsed("page", String.valueOf(newPage)), pricePlaceholder);
        module.settings().buySound().play(player);
        for (int page : session.livePages()) {
            renderNavigation(session, session.live(page));
        }
        openPage(player, session, newPage - 1, false);
    }

    // ---------------------------------------------------------------- closing & saving

    void handleClose(UUID viewer, EnderChestHolder holder) {
        EnderChestSession session = sessions.get(holder.owner());
        if (session != null && session.live(holder.page()) == holder) {
            releaseIfUnused(session, holder.page(), viewer);
        }
    }

    /**
     * Releases every page that has no viewers left besides the quitting player and forgets their pending opens.
     */
    void handleQuit(UUID uuid) {
        for (List<UUID> waiting : pending.values()) {
            waiting.remove(uuid);
        }
        for (EnderChestSession session : List.copyOf(sessions.values())) {
            for (int page : session.livePages()) {
                releaseIfUnused(session, page, uuid);
            }
        }
    }

    /**
     * Saves and releases the page if nobody but {@code leaving} (may be {@code null}) is viewing it.
     */
    private void releaseIfUnused(EnderChestSession session, int page, UUID leaving) {
        EnderChestHolder holder = session.live(page);
        if (holder == null) {
            return;
        }
        for (HumanEntity viewer : holder.getInventory().getViewers()) {
            if (!viewer.getUniqueId().equals(leaving)) {
                return;
            }
        }
        session.removeLive(page);
        persist(session, holder);
        if (session.isIdle()) {
            sessions.remove(session.owner(), session);
        }
    }

    private void persist(EnderChestSession session, EnderChestHolder holder) {
        Inventory inventory = holder.getInventory();
        int storageSize = holder.storageSize();
        ItemStack[] overflow = holder.overflow();
        ItemStack[] items = new ItemStack[storageSize + overflow.length];
        int length = 0;
        for (int slot = 0; slot < items.length; slot++) {
            ItemStack item = slot < storageSize ? inventory.getItem(slot) : overflow[slot - storageSize];
            if (item == null || item.isEmpty()) {
                items[slot] = ItemStack.empty();
            } else {
                items[slot] = item;
                length = slot + 1;
            }
        }
        byte[] data = length == 0 ? null : ItemStack.serializeItemsAsBytes(Arrays.copyOf(items, Math.max(length, storageSize)));
        int page = holder.page();
        UUID owner = session.owner();
        session.setPage(page, data);
        plugin.database().update(connection -> repository.savePage(connection, owner, page, data))
                .whenComplete((ignored, error) -> {
                    if (error != null) {
                        plugin.getLogger().log(Level.SEVERE, "Failed to save page " + page + " of the ender chest of "
                                + session.ownerName() + " (" + owner + ")", error);
                        notifyDatabaseError(owner);
                    }
                });
    }

    private void notifyDatabaseError(UUID owner) {
        plugin.mainThread().execute(() -> {
            Player online = Bukkit.getPlayer(owner);
            if (online != null) {
                plugin.messages().send(online, "general.database-error");
            }
        });
    }

    /**
     * Closes every open chest and queues the final saves. Must run while the database is still open.
     */
    void shutdown() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getOpenInventory().getTopInventory().getHolder(false) instanceof EnderChestHolder) {
                player.closeInventory(); // the close handler saves the page
            }
        }
        for (EnderChestSession session : List.copyOf(sessions.values())) {
            for (int page : session.livePages()) {
                EnderChestHolder holder = session.live(page);
                session.removeLive(page);
                persist(session, holder);
            }
        }
        sessions.clear();
        pending.clear();
    }
}
