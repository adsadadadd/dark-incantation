package me.x1no4ekk.core.gameplay.command;

import com.destroystokyo.paper.profile.PlayerProfile;
import me.x1no4ekk.core.CorePlugin;
import me.x1no4ekk.core.command.BaseCommand;
import me.x1no4ekk.core.gameplay.GameplayModule;
import me.x1no4ekk.core.util.Inventories;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * /head [player] [amount] — gives a player head. Skins of offline players are fetched asynchronously.
 */
public final class HeadCommand extends BaseCommand {

    private static final Pattern NAME = Pattern.compile("^[A-Za-z0-9_]{1,16}$");

    private final GameplayModule module;

    public HeadCommand(CorePlugin plugin, GameplayModule module) {
        super(plugin);
        this.module = module;
    }

    @Override
    protected void execute(CommandSender sender, String label, String[] args) {
        Player player = playerOnly(sender);
        requirePermission(sender, "core.head");
        if (args.length > 2) {
            throw usage("gameplay.head.usage");
        }
        String name = args.length >= 1 ? args[0] : player.getName();
        int amount = args.length == 2 ? parseInt(args[1], 1, module.settings().headMaxAmount()) : 1;

        Player online = Bukkit.getPlayerExact(name);
        if (online != null) {
            give(player, online.getPlayerProfile(), online.getName(), amount);
            return;
        }
        if (!NAME.matcher(name).matches()) {
            throw fail("gameplay.head.invalid-name", placeholder("player", name));
        }
        PlayerProfile profile = Bukkit.createProfile(name);
        UUID receiverId = player.getUniqueId();
        profile.update().whenCompleteAsync((updated, error) -> {
            Player receiver = Bukkit.getPlayer(receiverId);
            if (receiver != null) {
                // Without textures (unknown name / Mojang API down) the head is still given with the default skin.
                give(receiver, error == null && updated != null ? updated : profile, name, amount);
            }
        }, plugin.mainThread());
    }

    private void give(Player receiver, PlayerProfile profile, String name, int amount) {
        ItemStack head = ItemStack.of(Material.PLAYER_HEAD, amount);
        head.editMeta(SkullMeta.class, meta -> meta.setPlayerProfile(profile));
        if (Inventories.giveOrDrop(receiver, head)) {
            send(receiver, "gameplay.dropped");
        }
        send(receiver, "gameplay.head.given", placeholder("player", name), placeholder("amount", amount));
    }

    @Override
    protected List<String> complete(CommandSender sender, String label, String[] args) {
        if (!sender.hasPermission("core.head")) {
            return List.of();
        }
        if (args.length == 1) {
            return players(sender, args[0]);
        }
        if (args.length == 2) {
            return filter(Stream.of("1", "16", "64"), args[1]);
        }
        return List.of();
    }
}
