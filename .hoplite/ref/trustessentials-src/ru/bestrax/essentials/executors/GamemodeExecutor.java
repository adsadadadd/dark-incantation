/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.GameMode
 *  org.bukkit.command.CommandSender
 *  org.bukkit.entity.Player
 */
package ru.bestrax.essentials.executors;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.bestrax.essentials.api.EssentialsApi;
import ru.bestrax.essentials.api.User;
import ru.bestrax.essentials.api.commands.TrustCommand;

public class GamemodeExecutor
extends TrustCommand {
    public GamemodeExecutor() {
        super("gamemode", "gm");
        this.setPermission("trustessentials.gamemode");
        this.register();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        GameMode gameMode;
        if (args.length != 1 && args.length != 2) {
            this.sendMessages(sender, "GAMEMODE_USAGE", new String[0]);
            return;
        }
        String target = args.length == 1 ? sender.getName() : args[1];
        Player player = Bukkit.getPlayer((String)target);
        if (player == null || !player.isOnline()) {
            this.sendMessage(sender, "PLAYER_NOT_FOUND", "%name%", target);
            return;
        }
        if (!player.getName().equalsIgnoreCase(sender.getName()) && !sender.hasPermission("trustessentials.gamemode.other")) {
            this.sendMessage(sender, "NO_PERMISSION", "%permission%", "trustessentials.gamemode.other");
            return;
        }
        String mode = args[0];
        try {
            gameMode = this.parseGamemode(sender, mode);
        }
        catch (Exception e) {
            this.sendMessage(sender, "NO_PERMISSION", "%permission%", e.getLocalizedMessage());
            return;
        }
        if (gameMode == null) {
            this.sendMessage(sender, "GAMEMODE_FOUND", "%mode%", mode);
            return;
        }
        User user = EssentialsApi.getUserManager().getUser(player);
        if (EssentialsApi.getSpectateManager().isSpectation(player)) {
            this.sendMessage((CommandSender)player, "SPECTATE_GM", new String[0]);
            return;
        }
        user.setGameMode(gameMode);
        this.sendMessages((CommandSender)player, "GAMEMODE", "%mode%", this.parseString(gameMode));
        if (user.getUsername().equalsIgnoreCase(sender.getName())) {
            return;
        }
        this.sendMessages(sender, "GAMEMODE_TARGET", "%name%", user.getUsername(), "%mode%", this.parseString(gameMode));
    }

    private String parseString(GameMode gameMode) {
        String result = gameMode == GameMode.CREATIVE ? "\u043a\u0440\u0435\u0430\u0442\u0438\u0432" : null;
        result = gameMode == GameMode.ADVENTURE ? "\u043f\u0440\u0438\u043a\u043b\u044e\u0447\u0435\u043d\u0438\u044f" : result;
        result = gameMode == GameMode.SPECTATOR ? "\u0437\u0440\u0438\u0442\u0435\u043b\u044c" : result;
        result = gameMode == GameMode.SURVIVAL ? "\u0432\u044b\u0436\u0438\u0432\u0430\u043d\u0438\u0435" : result;
        return result;
    }

    private GameMode parseGamemode(CommandSender sender, String modeString) {
        int var3 = -1;
        switch (modeString.hashCode()) {
            case -2008454212: {
                if (!modeString.equals("spector")) break;
                var3 = 27;
                break;
            }
            case -1684593425: {
                if (!modeString.equals("spectator")) break;
                var3 = 28;
                break;
            }
            case -1600582850: {
                if (!modeString.equals("survival")) break;
                var3 = 12;
                break;
            }
            case -1421971624: {
                if (!modeString.equals("advent")) break;
                var3 = 19;
                break;
            }
            case -896072839: {
                if (!modeString.equals("spectr")) break;
                var3 = 26;
                break;
            }
            case -694094064: {
                if (!modeString.equals("adventure")) break;
                var3 = 20;
                break;
            }
            case 48: {
                if (!modeString.equals("0")) break;
                var3 = 13;
                break;
            }
            case 49: {
                if (!modeString.equals("1")) break;
                var3 = 4;
                break;
            }
            case 50: {
                if (!modeString.equals("2")) break;
                var3 = 21;
                break;
            }
            case 51: {
                if (!modeString.equals("3")) break;
                var3 = 29;
                break;
            }
            case 97: {
                if (!modeString.equals("a")) break;
                var3 = 16;
                break;
            }
            case 99: {
                if (!modeString.equals("c")) break;
                var3 = 0;
                break;
            }
            case 115: {
                if (!modeString.equals("s")) break;
                var3 = 7;
                break;
            }
            case 3107: {
                if (!modeString.equals("ad")) break;
                var3 = 17;
                break;
            }
            case 3183: {
                if (!modeString.equals("cr")) break;
                var3 = 1;
                break;
            }
            case 3677: {
                if (!modeString.equals("sp")) break;
                var3 = 24;
                break;
            }
            case 3682: {
                if (!modeString.equals("su")) break;
                var3 = 8;
                break;
            }
            case 3683: {
                if (!modeString.equals("sv")) break;
                var3 = 9;
                break;
            }
            case 96435: {
                if (!modeString.equals("adv")) break;
                var3 = 18;
                break;
            }
            case 102459: {
                if (!modeString.equals("gma")) break;
                var3 = 22;
                break;
            }
            case 102461: {
                if (!modeString.equals("gmc")) break;
                var3 = 5;
                break;
            }
            case 102477: {
                if (!modeString.equals("gms")) break;
                var3 = 14;
                break;
            }
            case 114167: {
                if (!modeString.equals("srv")) break;
                var3 = 10;
                break;
            }
            case 3111350: {
                if (!modeString.equals("egma")) break;
                var3 = 23;
                break;
            }
            case 3111352: {
                if (!modeString.equals("egmc")) break;
                var3 = 6;
                break;
            }
            case 3111368: {
                if (!modeString.equals("egms")) break;
                var3 = 15;
                break;
            }
            case 3536827: {
                if (!modeString.equals("spec")) break;
                var3 = 25;
                break;
            }
            case 3542054: {
                if (!modeString.equals("surv")) break;
                var3 = 11;
                break;
            }
            case 94924937: {
                if (!modeString.equals("creat")) break;
                var3 = 2;
                break;
            }
            case 1820422063: {
                if (!modeString.equals("creative")) break;
                var3 = 3;
            }
        }
        switch (var3) {
            case 0: 
            case 1: 
            case 2: 
            case 3: 
            case 4: 
            case 5: 
            case 6: {
                if (!sender.hasPermission("trustessentials.gamemode.creative")) {
                    throw new UnsupportedOperationException("trustessentials.gamemode.creative");
                }
                return GameMode.CREATIVE;
            }
            case 7: 
            case 8: 
            case 9: 
            case 10: 
            case 11: 
            case 12: 
            case 13: 
            case 14: 
            case 15: {
                if (!sender.hasPermission("trustessentials.gamemode.survival")) {
                    throw new UnsupportedOperationException("trustessentials.gamemode.survival");
                }
                return GameMode.SURVIVAL;
            }
            case 16: 
            case 17: 
            case 18: 
            case 19: 
            case 20: 
            case 21: 
            case 22: 
            case 23: {
                if (!sender.hasPermission("trustessentials.gamemode.adventure")) {
                    throw new UnsupportedOperationException("trustessentials.gamemode.adventure");
                }
                return GameMode.ADVENTURE;
            }
            case 24: 
            case 25: 
            case 26: 
            case 27: 
            case 28: 
            case 29: {
                if (!sender.hasPermission("trustessentials.gamemode.spectator")) {
                    throw new UnsupportedOperationException("trustessentials.gamemode.spectator");
                }
                return GameMode.SPECTATOR;
            }
        }
        return null;
    }
}
