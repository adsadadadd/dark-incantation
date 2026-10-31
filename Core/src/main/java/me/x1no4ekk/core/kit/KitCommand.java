package me.x1no4ekk.core.kit;

import me.x1no4ekk.core.CorePlugin;
import me.x1no4ekk.core.command.BaseCommand;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * {@code /kit} — kit menu; {@code /kit <kit>} — free claim; admin: {@code create|delete|seticon|give}.
 */
final class KitCommand extends BaseCommand {

    private static final List<String> ADMIN_SUBCOMMANDS = List.of("create", "delete", "seticon", "give");
    private static final long DEFAULT_COOLDOWN_SECONDS = 86_400L;

    private final KitModule module;

    KitCommand(CorePlugin plugin, KitModule module) {
        super(plugin);
        this.module = module;
    }

    @Override
    protected void execute(CommandSender sender, String label, String[] args) {
        requirePermission(sender, "core.kit");
        boolean admin = sender.hasPermission(KitService.ADMIN_PERMISSION);
        if (args.length == 0) {
            KitGui.open(plugin, module, playerOnly(sender));
            return;
        }
        String sub = args[0].toLowerCase(Locale.ROOT);
        if (admin && ADMIN_SUBCOMMANDS.contains(sub)) {
            switch (sub) {
                case "create" -> create(sender, args);
                case "delete" -> delete(sender, args);
                case "seticon" -> setIcon(sender, args);
                default -> give(sender, args);
            }
            return;
        }
        if (args.length != 1) {
            throw usage(admin ? "kit.admin-usage" : "kit.usage");
        }
        Player player = playerOnly(sender);
        module.service().claim(player, kit(args[0]));
    }

    // ---------------------------------------------------------------- admin

    private void create(CommandSender sender, String[] args) {
        if (args.length != 2) {
            throw usage("kit.admin-usage");
        }
        Player player = playerOnly(sender);
        String id = args[1].toLowerCase(Locale.ROOT);
        if (!KitSettings.NAME_PATTERN.matcher(id).matches() || KitSettings.RESERVED.contains(id)) {
            throw fail("kit.invalid-name", placeholder("name", args[1]));
        }
        List<ItemStack> items = inventoryItems(player.getInventory());
        if (items.isEmpty()) {
            throw fail("kit.empty-inventory");
        }
        boolean existed = module.settings().kit(id) != null;
        Supplier<CommandSender> reply = replyTo(sender);
        edit(reply, yaml -> {
            ConfigurationSection kits = kitsSection(yaml);
            String key = findKey(kits, id);
            ConfigurationSection section;
            if (key == null) {
                section = kits.createSection(id);
                section.set("name", "<gold>" + id);
                section.set("icon.material", Material.CHEST.name());
                section.set("icon.lore", List.of());
                section.set("cooldown", DEFAULT_COOLDOWN_SECONDS);
                section.set("price", 0);
                section.set("free", true);
                section.set("permission", "core.kit." + id);
                section.set("auto-equip", true);
                section.set("start", false);
            } else {
                section = kits.getConfigurationSection(key);
                if (section == null) {
                    section = kits.createSection(key);
                }
            }
            section.set("items", items);
        }, existed ? "kit.updated" : "kit.created", id);
    }

    private void delete(CommandSender sender, String[] args) {
        if (args.length != 2) {
            throw usage("kit.admin-usage");
        }
        String id = kit(args[1]).id();
        Supplier<CommandSender> reply = replyTo(sender);
        module.editor().edit(yaml -> {
            ConfigurationSection kits = kitsSection(yaml);
            String key = findKey(kits, id);
            if (key != null) {
                kits.set(key, null);
            }
        }).whenComplete((settings, error) -> {
            if (error == null) {
                module.service().forgetKit(id);
            }
            report(reply, error, "kit.deleted", id);
        });
    }

    private void setIcon(CommandSender sender, String[] args) {
        if (args.length != 2) {
            throw usage("kit.admin-usage");
        }
        Player player = playerOnly(sender);
        String id = kit(args[1]).id();
        ItemStack hand = player.getInventory().getItemInMainHand();
        if (hand.isEmpty()) {
            throw fail("general.no-item-in-hand");
        }
        String material = hand.getType().name();
        boolean glint = !hand.getEnchantments().isEmpty();
        edit(replyTo(sender), yaml -> {
            ConfigurationSection kits = kitsSection(yaml);
            String key = findKey(kits, id);
            if (key == null) {
                throw new IllegalArgumentException("kit '" + id + "' no longer exists in kits.yml");
            }
            kits.set(key + ".icon.material", material);
            kits.set(key + ".icon.glint", glint);
        }, "kit.icon-set", id);
    }

    private void give(CommandSender sender, String[] args) {
        if (args.length != 3) {
            throw usage("kit.admin-usage");
        }
        Kit kit = kit(args[1]);
        Player target = player(sender, args[2]);
        if (kit.items().isEmpty()) {
            throw fail("kit.empty", KitService.kitPlaceholder(kit));
        }
        module.service().give(sender, target, kit);
    }

    private void edit(Supplier<CommandSender> reply, Consumer<YamlConfiguration> change, String successKey, String id) {
        module.editor().edit(change).whenComplete((settings, error) -> report(reply, error, successKey, id));
    }

    private void report(Supplier<CommandSender> reply, Throwable error, String successKey, String id) {
        if (error != null) {
            String reason = KitFileEditor.describe(error);
            plugin.getLogger().warning("[kits] Failed to edit " + KitFileEditor.FILE + ": " + reason);
            plugin.mainThread().execute(() -> {
                CommandSender target = reply.get();
                if (target != null) {
                    send(target, "kit.save-failed", placeholder("error", reason));
                }
            });
            return;
        }
        CommandSender target = reply.get();
        if (target != null) {
            send(target, successKey, placeholder("name", id));
        }
    }

    // ---------------------------------------------------------------- helpers

    private Kit kit(String name) {
        Kit kit = module.settings().kit(name);
        if (kit == null) {
            throw fail("kit.not-found", placeholder("name", name));
        }
        return kit;
    }

    /**
     * Re-resolves the sender later: players by UUID, everything else as the console.
     */
    private static Supplier<CommandSender> replyTo(CommandSender sender) {
        if (sender instanceof Player player) {
            UUID uuid = player.getUniqueId();
            return () -> Bukkit.getPlayer(uuid);
        }
        return Bukkit::getConsoleSender;
    }

    /**
     * Storage, armor and off-hand items (copies), in that order.
     */
    private static List<ItemStack> inventoryItems(PlayerInventory inventory) {
        List<ItemStack> items = new ArrayList<>();
        for (ItemStack[] part : List.of(inventory.getStorageContents(), inventory.getArmorContents(), inventory.getExtraContents())) {
            for (ItemStack item : part) {
                if (item != null && !item.isEmpty()) {
                    items.add(item.clone());
                }
            }
        }
        return items;
    }

    private static ConfigurationSection kitsSection(YamlConfiguration yaml) {
        ConfigurationSection kits = yaml.getConfigurationSection("kits");
        return kits != null ? kits : yaml.createSection("kits");
    }

    private static String findKey(ConfigurationSection kits, String id) {
        for (String key : kits.getKeys(false)) {
            if (key.equalsIgnoreCase(id)) {
                return key;
            }
        }
        return null;
    }

    // ---------------------------------------------------------------- tab completion

    @Override
    protected List<String> complete(CommandSender sender, String label, String[] args) {
        if (!sender.hasPermission("core.kit")) {
            return List.of();
        }
        boolean admin = sender.hasPermission(KitService.ADMIN_PERMISSION);
        KitSettings settings = module.settings();
        if (args.length == 1) {
            List<String> options = new ArrayList<>();
            for (Kit kit : settings.kits().values()) {
                if (admin || kit.isBuyable() || (kit.free() && (!kit.requiresPermission() || sender.hasPermission(kit.permission())))) {
                    options.add(kit.id());
                }
            }
            if (admin) {
                options.addAll(ADMIN_SUBCOMMANDS);
            }
            return filter(options, args[0]);
        }
        if (!admin) {
            return List.of();
        }
        String sub = args[0].toLowerCase(Locale.ROOT);
        if (args.length == 2 && (sub.equals("delete") || sub.equals("seticon") || sub.equals("give"))) {
            return filter(settings.kits().keySet(), args[1]);
        }
        if (args.length == 3 && sub.equals("give")) {
            return players(sender, args[2]);
        }
        return List.of();
    }
}
