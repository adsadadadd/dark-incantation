package me.x1no4ekk.core.kit;

import me.x1no4ekk.core.config.ConfigUtil;
import me.x1no4ekk.core.config.MenuItem;
import me.x1no4ekk.core.config.SoundEffect;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Parsed kits.yml: kits, claim options and the layouts of the kit menu and the kit preview.
 *
 * @param kits      kits by lower-case id, in file order
 * @param kitSlots  slot of every kit shown in the kit menu (kits that do not fit are absent)
 * @param overflow  ids of kits that did not fit into the kit menu
 */
public record KitSettings(Map<String, Kit> kits, Map<String, Integer> kitSlots, List<String> overflow,
                          boolean dropWhenFull, SoundEffect claimSound, SoundEffect errorSound,
                          KitMenu menu, ContentMenu content) {

    /** Kit ids accepted by {@code /kit create}; also the format of the keys in core_kit_cooldowns. */
    static final Pattern NAME_PATTERN = Pattern.compile("[a-z0-9_-]{1,32}");
    /** Admin sub-commands; kits must not use these names. */
    static final Set<String> RESERVED = Set.of("create", "delete", "seticon", "give");

    /**
     * Status lines added to kit icons and to the "get" button.
     */
    public record StatusLore(List<String> available, List<String> cooldown, List<String> used,
                             List<String> noPermission, List<String> purchaseOnly) {

        static StatusLore load(ConfigurationSection s) {
            return new StatusLore(
                    List.copyOf(s.getStringList("available")),
                    List.copyOf(s.getStringList("cooldown")),
                    List.copyOf(s.getStringList("used")),
                    List.copyOf(s.getStringList("no-permission")),
                    List.copyOf(s.getStringList("purchase-only")));
        }

        List<String> of(KitStatus status) {
            return switch (status) {
                case AVAILABLE -> available;
                case COOLDOWN -> cooldown;
                case USED -> used;
                case NO_PERMISSION -> noPermission;
                case PURCHASE_ONLY -> purchaseOnly;
            };
        }
    }

    /**
     * Kit menu ({@code /kit}).
     */
    public record KitMenu(String title, int rows, MenuItem filler, MenuItem close, StatusLore status,
                          List<String> buyLore, List<String> clickLore) {
    }

    /**
     * Kit preview with the get/buy buttons.
     */
    public record ContentMenu(String title, int rows, List<Integer> contentSlots, MenuItem filler,
                              MenuItem previousPage, MenuItem nextPage, MenuItem back,
                              MenuItem get, int getAloneSlot, StatusLore getStatus,
                              MenuItem buy, int buyAloneSlot) {
    }

    public static KitSettings load(ConfigurationSection root) {
        ConfigurationSection options = ConfigUtil.sectionOrEmpty(root, "settings");
        String fullMode = options.getString("full-inventory", "deny").trim().toLowerCase(Locale.ROOT);
        if (!fullMode.equals("deny") && !fullMode.equals("drop")) {
            throw new IllegalArgumentException(ConfigUtil.path(options, "full-inventory") + " must be 'deny' or 'drop'");
        }

        ConfigurationSection gui = ConfigUtil.section(root, "gui");
        ConfigurationSection menuSection = ConfigUtil.section(gui, "kits");
        int menuRows = ConfigUtil.rows(menuSection, "rows", 6);
        List<Integer> menuContent = ConfigUtil.slots(menuSection, "content-slots", menuRows);
        KitMenu menu = new KitMenu(
                ConfigUtil.requireText(menuSection, "title"),
                menuRows,
                optionalItem(menuSection, "filler", menuRows),
                optionalItem(menuSection, "close", menuRows),
                StatusLore.load(ConfigUtil.section(menuSection, "status")),
                List.copyOf(menuSection.getStringList("buy-lore")),
                List.copyOf(menuSection.getStringList("click-lore")));

        ConfigurationSection contentSection = ConfigUtil.section(gui, "content");
        int contentRows = ConfigUtil.rows(contentSection, "rows", 6);
        List<Integer> contentSlots = ConfigUtil.slots(contentSection, "content-slots", contentRows);
        if (contentSlots.isEmpty()) {
            throw new IllegalArgumentException(ConfigUtil.path(contentSection, "content-slots") + " must not be empty");
        }
        ConfigurationSection getSection = ConfigUtil.section(contentSection, "get");
        ConfigurationSection buySection = ConfigUtil.section(contentSection, "buy");
        MenuItem get = MenuItem.load(getSection, contentRows, true);
        MenuItem buy = MenuItem.load(buySection, contentRows, true);
        ContentMenu content = new ContentMenu(
                ConfigUtil.requireText(contentSection, "title"),
                contentRows,
                contentSlots,
                optionalItem(contentSection, "filler", contentRows),
                optionalItem(contentSection, "previous-page", contentRows),
                optionalItem(contentSection, "next-page", contentRows),
                optionalItem(contentSection, "back", contentRows),
                get,
                aloneSlot(getSection, get, contentRows),
                StatusLore.load(ConfigUtil.section(getSection, "status")),
                buy,
                aloneSlot(buySection, buy, contentRows));

        Map<String, Kit> kits = new LinkedHashMap<>();
        ConfigurationSection kitsSection = ConfigUtil.sectionOrEmpty(root, "kits");
        for (String key : kitsSection.getKeys(false)) {
            ConfigurationSection kitSection = kitsSection.getConfigurationSection(key);
            if (kitSection == null) {
                throw new IllegalArgumentException("'" + ConfigUtil.path(kitsSection, key) + "' must be a section");
            }
            Kit kit = loadKit(key, kitSection, menuRows);
            if (kits.putIfAbsent(kit.id(), kit) != null) {
                throw new IllegalArgumentException(ConfigUtil.path(kitsSection, key) + ": duplicate kit name (names are case-insensitive)");
            }
        }

        Map<String, Integer> kitSlots = new HashMap<>();
        Set<Integer> taken = new HashSet<>();
        for (Kit kit : kits.values()) {
            if (kit.slot() >= 0) {
                if (!taken.add(kit.slot())) {
                    throw new IllegalArgumentException("kits." + kit.id() + ".slot: slot " + kit.slot() + " is used by another kit");
                }
                kitSlots.put(kit.id(), kit.slot());
            }
        }
        List<String> overflow = new ArrayList<>();
        int next = 0;
        for (Kit kit : kits.values()) {
            if (kit.slot() >= 0) {
                continue;
            }
            while (next < menuContent.size() && taken.contains(menuContent.get(next))) {
                next++;
            }
            if (next < menuContent.size()) {
                int slot = menuContent.get(next++);
                taken.add(slot);
                kitSlots.put(kit.id(), slot);
            } else {
                overflow.add(kit.id());
            }
        }

        return new KitSettings(
                Collections.unmodifiableMap(kits),
                Map.copyOf(kitSlots),
                List.copyOf(overflow),
                fullMode.equals("drop"),
                SoundEffect.load(options, "claim-sound"),
                SoundEffect.load(options, "error-sound"),
                menu,
                content);
    }

    private static Kit loadKit(String key, ConfigurationSection s, int menuRows) {
        String id = key.toLowerCase(Locale.ROOT);
        if (!NAME_PATTERN.matcher(id).matches()) {
            throw new IllegalArgumentException(s.getCurrentPath() + ": kit name may contain only a-z, 0-9, '_' and '-' (up to 32 characters)");
        }
        if (RESERVED.contains(id)) {
            throw new IllegalArgumentException(s.getCurrentPath() + ": '" + id + "' is reserved for /kit " + id);
        }
        int slot = s.getInt("slot", -1);
        if (s.contains("slot") && (slot < 0 || slot >= menuRows * 9)) {
            throw new IllegalArgumentException(ConfigUtil.path(s, "slot") + " must be inside the kit menu (0-" + (menuRows * 9 - 1) + ")");
        }
        long cooldown = s.getLong("cooldown", 0L);
        if (cooldown < -1) {
            throw new IllegalArgumentException(ConfigUtil.path(s, "cooldown") + " must be -1 (one-time), 0 (none) or a number of seconds");
        }
        String displayName = s.getString("name", id);
        ConfigurationSection icon = ConfigUtil.sectionOrEmpty(s, "icon");
        String permission = s.getString("permission", "core.kit." + id);
        return new Kit(
                id,
                displayName,
                ConfigUtil.material(icon, "material", Material.CHEST),
                icon.getString("name", displayName),
                List.copyOf(icon.getStringList("lore")),
                icon.getBoolean("glint", false),
                slot,
                cooldown,
                ConfigUtil.nonNegativeDouble(s, "price", 0D),
                s.getBoolean("free", true),
                permission == null ? "" : permission.trim(),
                s.getBoolean("auto-equip", true),
                s.getBoolean("start", false),
                KitItems.parse(s.getList("items", List.of()), ConfigUtil.path(s, "items")));
    }

    private static MenuItem optionalItem(ConfigurationSection parent, String key, int rows) {
        ConfigurationSection section = parent.getConfigurationSection(key);
        return section == null ? null : MenuItem.load(section, rows, true);
    }

    private static int aloneSlot(ConfigurationSection section, MenuItem button, int rows) {
        if (!section.contains("alone-slot")) {
            return button.slot();
        }
        int slot = section.getInt("alone-slot", -1);
        if (slot < 0 || slot >= rows * 9) {
            throw new IllegalArgumentException(ConfigUtil.path(section, "alone-slot") + " must be inside the menu (0-" + (rows * 9 - 1) + ")");
        }
        return slot;
    }

    public Kit kit(String name) {
        return kits.get(name.toLowerCase(Locale.ROOT));
    }
}
