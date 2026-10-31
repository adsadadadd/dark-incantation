/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.md_5.bungee.api.ChatColor
 *  net.md_5.bungee.api.chat.BaseComponent
 *  net.md_5.bungee.api.chat.ClickEvent
 *  net.md_5.bungee.api.chat.ClickEvent$Action
 *  net.md_5.bungee.api.chat.ComponentBuilder
 *  net.md_5.bungee.api.chat.HoverEvent
 *  net.md_5.bungee.api.chat.HoverEvent$Action
 *  net.md_5.bungee.api.chat.TextComponent
 *  org.bukkit.Bukkit
 *  org.bukkit.Location
 *  org.bukkit.World
 *  org.bukkit.command.CommandSender
 *  org.bukkit.entity.Player
 *  org.bukkit.potion.PotionEffectType
 *  org.bukkit.util.Vector
 */
package ru.bestrax.essentials.executors;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import ru.bestrax.essentials.api.EssentialsApi;
import ru.bestrax.essentials.api.User;
import ru.bestrax.essentials.api.VaultAPI;
import ru.bestrax.essentials.api.commands.TrustCommand;
import ru.bestrax.essentials.utils.Direction;

public class NearExecutor
extends TrustCommand {
    public NearExecutor() {
        super("near", new String[0]);
        this.setPermission("trustessentials.near");
        this.setPlayersOnly(true);
        this.register();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player)sender;
        int radius = EssentialsApi.getInstance().getSettings().getInt("nearRadius");
        this.sendNearMessage(player, radius);
    }

    public void sendNearMessage(Player player, long radius) {
        Location location = player.getLocation();
        World world = location.getWorld();
        int count = 0;
        for (Player player1 : Bukkit.getOnlinePlayers()) {
            User targetUser;
            long delta;
            double radiusToPlayer;
            Location playerLoc = player1.getLocation();
            if (playerLoc.getWorld() != world || player1.getDisplayName().equals(player.getDisplayName()) || player1.hasPotionEffect(PotionEffectType.INVISIBILITY) || !((radiusToPlayer = Math.sqrt(delta = (long)playerLoc.distanceSquared(location))) < (double)radius) || (targetUser = EssentialsApi.getUserManager().getUser(player1)).isVanished() || EssentialsApi.getSpectateManager().isSpectation(targetUser.getPlayer())) continue;
            if (count == 0) {
                this.sendMessage((CommandSender)player, "NEAR_TOP", new String[0]);
            }
            Location origin = player.getLocation();
            Vector target = player1.getLocation().toVector();
            origin.setDirection(target.subtract(origin.toVector()));
            int yaw = (int)((player.getLocation().getYaw() - origin.getYaw()) / 45.0f);
            String message = this.getString("NEAR", "%name%", player1.getDisplayName(), "%blocks%", String.valueOf((int)Math.sqrt(delta)), "%where%", Direction.getDirection(yaw), "%y%", String.valueOf(player1.getLocation().getBlockY()), "%health%", String.valueOf((int)player1.getHealth()), "%prefix%", VaultAPI.getPrefix(player1));
            BaseComponent[] text = TextComponent.fromLegacyText((String)message);
            ComponentBuilder componentBuilder = new ComponentBuilder(EssentialsApi.getInstance().getSettings().format(message));
            HoverEvent hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("\u041d\u0430\u0436\u043c\u0438\u0442\u0435, \u0447\u0442\u043e\u0431\u044b \u043e\u0442\u043a\u0440\u044b\u0442\u044c \u0438\u043d\u0432\u0435\u043d\u0442\u0430\u0440\u044c").color(ChatColor.GRAY).create());
            ClickEvent clickEvent = new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/invsee " + player1.getName());
            for (BaseComponent baseComponent : text) {
                baseComponent.setClickEvent(clickEvent);
                baseComponent.setHoverEvent(hoverEvent);
            }
            player.spigot().sendMessage(text);
            ++count;
        }
        if (count == 0) {
            this.sendMessage((CommandSender)player, "NEAR_ONE", new String[0]);
        }
    }
}
