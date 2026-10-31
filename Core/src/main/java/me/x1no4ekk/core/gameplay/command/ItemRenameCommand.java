package me.x1no4ekk.core.gameplay.command;

import me.x1no4ekk.core.CorePlugin;
import me.x1no4ekk.core.command.BaseCommand;
import me.x1no4ekk.core.gameplay.GameplayModule;
import me.x1no4ekk.core.gameplay.GameplaySettings;
import me.x1no4ekk.core.util.Text;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Stream;

/**
 * /itemrename &lt;name...&gt; | reset — renames the held item. Colours and decorations need
 * {@code core.itemrename.color}; other players' text is shown literally.
 */
public final class ItemRenameCommand extends BaseCommand {

    private static final Set<String> RESET = Set.of("reset", "сброс");
    // Only styling tags: click/hover/insertion/selector tags are useless or abusable in item names.
    private static final MiniMessage STYLE_ONLY = MiniMessage.builder()
            .tags(TagResolver.resolver(StandardTags.color(), StandardTags.decorations(), StandardTags.gradient(),
                    StandardTags.rainbow(), StandardTags.transition(), StandardTags.reset()))
            .build();

    private final GameplayModule module;

    public ItemRenameCommand(CorePlugin plugin, GameplayModule module) {
        super(plugin);
        this.module = module;
    }

    @Override
    protected void execute(CommandSender sender, String label, String[] args) {
        Player player = playerOnly(sender);
        requirePermission(sender, "core.itemrename");
        if (args.length == 0) {
            throw usage("gameplay.itemrename.usage");
        }
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.isEmpty()) {
            throw fail("general.no-item-in-hand");
        }
        String raw = String.join(" ", args).trim();
        if (RESET.contains(raw.toLowerCase(Locale.ROOT))) {
            item.editMeta(meta -> meta.customName(null));
            player.getInventory().setItemInMainHand(item);
            send(player, "gameplay.itemrename.reset");
            return;
        }

        GameplaySettings.ItemRename config = module.settings().itemRename();
        Component name = player.hasPermission("core.itemrename.color") ? STYLE_ONLY.deserialize(raw) : Component.text(raw);
        int length = Text.plain(name).strip().length();
        if (length == 0) {
            throw usage("gameplay.itemrename.usage");
        }
        if (length > config.maxLength()) {
            throw fail("gameplay.itemrename.too-long", placeholder("max", config.maxLength()));
        }
        String prefix = player.hasPermission("core.itemrename.noprefix") ? "" : config.prefix();
        Component display = Text.item(prefix + "<name>", Placeholder.component("name", name));
        item.editMeta(meta -> meta.customName(display));
        player.getInventory().setItemInMainHand(item);
        send(player, "gameplay.itemrename.renamed", Placeholder.component("name", display));
    }

    @Override
    protected List<String> complete(CommandSender sender, String label, String[] args) {
        if (args.length == 1 && sender.hasPermission("core.itemrename")) {
            return filter(Stream.of("reset"), args[0]);
        }
        return List.of();
    }
}
