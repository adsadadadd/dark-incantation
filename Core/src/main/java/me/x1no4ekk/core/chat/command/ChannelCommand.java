package me.x1no4ekk.core.chat.command;

import me.x1no4ekk.core.CorePlugin;
import me.x1no4ekk.core.chat.ChatModule;
import me.x1no4ekk.core.chat.ChatPermissions;
import me.x1no4ekk.core.chat.CooldownMap;
import me.x1no4ekk.core.chat.format.ChannelSettings;
import me.x1no4ekk.core.chat.format.ChatSettings;
import me.x1no4ekk.core.command.BaseCommand;
import me.x1no4ekk.core.user.User;
import me.x1no4ekk.core.util.Durations;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

/**
 * /donatechat &lt;message&gt; and /staffchat &lt;message&gt;: a chat seen only by holders of the "see" permission.
 */
public final class ChannelCommand extends BaseCommand {

    public enum Channel {
        DONATE(ChatPermissions.DONATE_CHAT, ChatPermissions.DONATE_CHAT_SEE, ChatPermissions.DONATE_CHAT_BYPASS_COOLDOWN, "chat.donate-usage"),
        STAFF(ChatPermissions.STAFF_CHAT, ChatPermissions.STAFF_CHAT, ChatPermissions.STAFF_CHAT_BYPASS_COOLDOWN, "chat.staff-usage");

        private final String permission;
        private final String seePermission;
        private final String bypassCooldownPermission;
        private final String usageKey;

        Channel(String permission, String seePermission, String bypassCooldownPermission, String usageKey) {
            this.permission = permission;
            this.seePermission = seePermission;
            this.bypassCooldownPermission = bypassCooldownPermission;
            this.usageKey = usageKey;
        }
    }

    private final ChatModule module;
    private final Channel channel;

    public ChannelCommand(CorePlugin plugin, ChatModule module, Channel channel) {
        super(plugin);
        this.module = module;
        this.channel = channel;
    }

    @Override
    protected void execute(CommandSender sender, String label, String[] args) {
        requirePermission(sender, channel.permission);
        String text = String.join(" ", args).strip();
        if (text.isEmpty()) {
            throw usage(channel.usageKey);
        }
        ChatSettings settings = module.settings();
        ChannelSettings channelSettings = channel == Channel.DONATE ? settings.donateChat() : settings.staffChat();
        CooldownMap cooldowns = channel == Channel.DONATE ? module.donateCooldowns() : module.staffCooldowns();

        Player player = sender instanceof Player p ? p : null;
        boolean bypassCooldown = player == null || player.hasPermission(channel.bypassCooldownPermission);
        if (player != null) {
            if (channel == Channel.DONATE) {
                if (module.isMuted() && !player.hasPermission(ChatPermissions.MUTE_BYPASS)) {
                    throw fail("chat.muted");
                }
                long playtimeLeft = module.playtimeLeft(player);
                if (playtimeLeft > 0L) {
                    throw fail("chat.minimal-playtime", placeholder("time", Durations.format(playtimeLeft)));
                }
            }
            long left = bypassCooldown ? 0L : cooldowns.remaining(player.getUniqueId());
            if (left > 0L) {
                throw fail("chat.cooldown", placeholder("time", Durations.format(left)));
            }
        }

        Component line = module.renderChannel(sender, channelSettings.format(), text);
        UUID senderId = player == null ? null : player.getUniqueId();
        boolean exempt = sender.hasPermission(ChatPermissions.IGNORE_EXEMPT);
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online.equals(player) || (online.hasPermission(channel.seePermission) && receives(online, senderId, exempt))) {
                online.sendMessage(line);
            }
        }
        Bukkit.getConsoleSender().sendMessage(line);
        if (player != null && !bypassCooldown) {
            cooldowns.start(player.getUniqueId(), channelSettings.cooldownMillis());
        }
    }

    /**
     * The donate chat respects /chattoggle and /ignore; the staff chat reaches every staff member.
     */
    private boolean receives(Player viewer, UUID senderId, boolean exempt) {
        if (channel == Channel.STAFF) {
            return true;
        }
        User user = plugin.userManager().getUser(viewer);
        return user == null || (user.isChatToggle() && (exempt || senderId == null || !user.isIgnoring(senderId)));
    }

    @Override
    protected List<String> complete(CommandSender sender, String label, String[] args) {
        if (args.length == 0 || !sender.hasPermission(channel.permission)) {
            return List.of();
        }
        return players(sender, args[args.length - 1]);
    }
}
