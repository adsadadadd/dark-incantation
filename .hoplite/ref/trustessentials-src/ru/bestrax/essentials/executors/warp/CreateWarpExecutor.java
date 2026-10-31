/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.command.CommandSender
 *  org.bukkit.entity.Player
 */
package ru.bestrax.essentials.executors.warp;

import java.util.regex.Pattern;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.bestrax.essentials.api.EssentialsApi;
import ru.bestrax.essentials.api.User;
import ru.bestrax.essentials.api.VaultAPI;
import ru.bestrax.essentials.api.commands.TrustCommand;
import ru.bestrax.essentials.api.events.UserCreateWarpEvent;
import ru.bestrax.essentials.api.manager.SpectateManager;
import ru.bestrax.essentials.base.objects.TrustWarp;
import ru.bestrax.essentials.utils.BukkitUtils;

public class CreateWarpExecutor
extends TrustCommand {
    private Pattern warpNamePattern = Pattern.compile("^[a-zA-Z\u0430-\u044f\u0410-\u042f0-9]+$");

    public CreateWarpExecutor() {
        super("createwarp", "setwarp", "addwarp");
        this.setPermission("trustessentials.createwarp");
        this.setPlayersOnly(true);
        this.register();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        User user = EssentialsApi.getUserManager().getUser(sender.getName());
        if (user != null) {
            if (args.length == 0) {
                this.sendMessages(sender, "CREATEWARP_USAGE", new String[0]);
            } else {
                Player player = (Player)sender;
                String name = args[0];
                if (EssentialsApi.getWarpManager().getWarps().containsKey(name.toLowerCase())) {
                    this.sendMessage((CommandSender)player, "WARP_CREATE_ERROR", "%name%", name.toLowerCase());
                } else {
                    Integer playerLimit;
                    int warps = EssentialsApi.getWarpManager().getWarps(user).size();
                    if (warps >= (playerLimit = Integer.valueOf(EssentialsApi.getWarpManager().getWarpLimit(VaultAPI.getPrimaryGroup(player))))) {
                        this.sendMessage((CommandSender)player, "WARP_LIMIT", "%limit%", String.valueOf(playerLimit));
                    } else if (name.length() > 32 || !this.warpNamePattern.matcher(name).matches()) {
                        this.sendMessage((CommandSender)player, "WARP_CREATE_NAME_ERROR", new String[0]);
                    } else {
                        SpectateManager spectateManager = EssentialsApi.getSpectateManager();
                        if (spectateManager.isSpectation(player)) {
                            this.sendMessage(sender, "SPECTATE_WARP", new String[0]);
                            return;
                        }
                        TrustWarp warp = new TrustWarp(name, null, user.getUsername(), player.getLocation());
                        UserCreateWarpEvent event = new UserCreateWarpEvent(user, warp);
                        BukkitUtils.callEvent(event, EssentialsApi.getInstance());
                        if (!event.isCancelled()) {
                            this.sendMessage((CommandSender)player, "WARP_CREATE", "%name%", warp.getName());
                            EssentialsApi.getWarpManager().addToDataBase(warp);
                        }
                    }
                }
            }
        }
    }
}
