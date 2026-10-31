package me.x1no4ekk.core.enderchest;

import me.x1no4ekk.core.config.ConfigUtil;
import me.x1no4ekk.core.config.MenuItem;
import me.x1no4ekk.core.config.SoundEffect;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;

/**
 * {@code enderchest} section of settings.yml.
 *
 * @param storageRows rows for items on every page; one more row below holds the navigation
 * @param pagePrices  price of every purchasable page in order; empty — purchases disabled
 */
public record EnderChestSettings(
        boolean replaceVanilla,
        String title,
        String titleOther,
        int storageRows,
        int defaultPages,
        int maxPages,
        List<Double> pagePrices,
        SoundEffect openSound,
        SoundEffect pageSound,
        SoundEffect buySound,
        MenuItem filler,
        MenuItem previousPage,
        MenuItem nextPage,
        MenuItem pageInfo,
        MenuItem lockedInfo,
        MenuItem buyPage
) {

    static final int MAX_PAGES_LIMIT = 100;

    public static EnderChestSettings load(ConfigurationSection s) {
        int storageRows = s.getInt("storage-rows", 5);
        if (storageRows < 1 || storageRows > 5) {
            throw new IllegalArgumentException(ConfigUtil.path(s, "storage-rows") + " must be between 1 and 5");
        }
        int maxPages = s.getInt("max-pages", 9);
        if (maxPages < 1 || maxPages > MAX_PAGES_LIMIT) {
            throw new IllegalArgumentException(ConfigUtil.path(s, "max-pages") + " must be between 1 and " + MAX_PAGES_LIMIT);
        }
        int defaultPages = s.getInt("default-pages", 1);
        if (defaultPages < 1 || defaultPages > maxPages) {
            throw new IllegalArgumentException(ConfigUtil.path(s, "default-pages") + " must be between 1 and max-pages (" + maxPages + ")");
        }

        ConfigurationSection purchase = ConfigUtil.sectionOrEmpty(s, "purchase");
        List<Double> prices = new ArrayList<>();
        if (purchase.getBoolean("enabled", false)) {
            List<?> rawPrices = purchase.getList("prices", List.of());
            for (int i = 0; i < rawPrices.size(); i++) {
                double price;
                try {
                    price = Double.parseDouble(String.valueOf(rawPrices.get(i)).trim());
                } catch (NumberFormatException e) {
                    price = -1;
                }
                if (!(price > 0) || Double.isInfinite(price)) {
                    throw new IllegalArgumentException(ConfigUtil.path(purchase, "prices") + "[" + i + "] must be a positive number");
                }
                prices.add(price);
            }
        }

        ConfigurationSection navigation = ConfigUtil.section(s, "navigation");
        return new EnderChestSettings(
                s.getBoolean("replace-vanilla", true),
                ConfigUtil.requireText(s, "title"),
                ConfigUtil.requireText(s, "title-other"),
                storageRows,
                defaultPages,
                maxPages,
                List.copyOf(prices),
                SoundEffect.load(s, "open-sound"),
                SoundEffect.load(s, "page-sound"),
                SoundEffect.load(s, "buy-sound"),
                MenuItem.load(navigation, "filler", 1, false),
                MenuItem.load(navigation, "previous-page", 1, true),
                MenuItem.load(navigation, "next-page", 1, true),
                MenuItem.load(navigation, "page-info", 1, true),
                MenuItem.load(navigation, "locked-info", 1, true),
                MenuItem.load(navigation, "buy-page", 1, true));
    }

    public int storageSize() {
        return storageRows * 9;
    }

    public int inventorySize() {
        return (storageRows + 1) * 9;
    }
}
