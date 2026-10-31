package me.x1no4ekk.core.chat.command;

import me.x1no4ekk.core.CorePlugin;
import me.x1no4ekk.core.chat.ChatModule;
import me.x1no4ekk.core.chat.ChatPermissions;
import me.x1no4ekk.core.command.BaseCommand;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * /chat [toggle] — hide/show the public chat (as /chattoggle);
 * /chat clear — clear the chat of everyone without {@code core.chat.clear.bypass};
 * /chat mute | unmute — lock the public and donate chat for players without {@code core.chat.mute.bypass}.
 */
public final class ChatCommand extends BaseCommand {

    private final ChatModule module;

    public ChatCommand(CorePlugin plugin, ChatModule module) {
        super(plugin);
        this.module = module;
    }

    @Override
    protected void execute(CommandSender sender, String label, String[] args) {
        if (args.length == 0) {
            if (!(sender instanceof Player)) {
                throw usage("chat.usage");
            }
            toggle(sender);
            return;
        }
        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "toggle" -> toggle(sender);
            case "clear" -> clear(sender);
            case "mute" -> mute(sender, true);
            case "unmute" -> mute(sender, false);
            default -> throw usage("chat.usage");
        }
    }

    private void toggle(CommandSender sender) {
        Player player = playerOnly(sender);
        ChatToggleCommand.toggle(messages(), player, user(player));
    }

    private void clear(CommandSender sender) {
        requirePermission(sender, ChatPermissions.CLEAR);
        Component blank = Component.text("\n".repeat(module.settings().clearLines() - 1));
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (!online.hasPermission(ChatPermissions.CLEAR_BYPASS)) {
                online.sendMessage(blank);
            }
        }
        messages().broadcast("chat.cleared", placeholder("player", sender.getName()));
    }

    private void mute(CommandSender sender, boolean mute) {
        requirePermission(sender, ChatPermissions.MUTE);
        if (module.isMuted() == mute) {
            throw fail(mute ? "chat.already-muted" : "chat.not-muted");
        }
        module.setMuted(mute);
        messages().broadcast(mute ? "chat.mute-enabled" : "chat.mute-disabled", placeholder("player", sender.getName()));
    }

    @Override
    protected List<String> complete(CommandSender sender, String label, String[] args) {
        if (args.length != 1) {
            return List.of();
        }
        List<String> options = new ArrayList<>();
        if (sender instanceof Player) {
            options.add("toggle");
        }
        if (sender.hasPermission(ChatPermissions.CLEAR)) {
            options.add("clear");
        }
        if (sender.hasPermission(ChatPermissions.MUTE)) {
            options.add(module.isMuted() ? "unmute" : "mute");
        }
        return filter(options, args[0]);
    }
}
