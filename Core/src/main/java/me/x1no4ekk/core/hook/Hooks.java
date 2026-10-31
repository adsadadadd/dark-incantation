package me.x1no4ekk.core.hook;

import me.x1no4ekk.core.util.Numbers;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

/**
 * Entry point for optional integrations (Vault economy/chat, PlaceholderAPI).
 * Created once in onEnable; falls back to no-op implementations when a plugin is missing.
 */
public final class Hooks {

    private final EconomyHook economy;
    private final ChatHook chat;

    private Hooks(EconomyHook economy, ChatHook chat) {
        this.economy = economy;
        this.chat = chat;
    }

    public static Hooks create() {
        PlaceholderHook.init();
        if (Bukkit.getPluginManager().isPluginEnabled("Vault")) {
            return new Hooks(new VaultHooks.VaultEconomy(), new VaultHooks.VaultChat());
        }
        return new Hooks(NoEconomy.INSTANCE, NoChat.INSTANCE);
    }

    public EconomyHook economy() {
        return economy;
    }

    public ChatHook chat() {
        return chat;
    }

    private enum NoEconomy implements EconomyHook {
        INSTANCE;

        @Override
        public boolean isAvailable() {
            return false;
        }

        @Override
        public double getBalance(OfflinePlayer player) {
            return 0D;
        }

        @Override
        public boolean has(OfflinePlayer player, double amount) {
            return false;
        }

        @Override
        public boolean withdraw(OfflinePlayer player, double amount) {
            return false;
        }

        @Override
        public boolean deposit(OfflinePlayer player, double amount) {
            return false;
        }

        @Override
        public String format(double amount) {
            return Numbers.format(amount);
        }
    }

    private enum NoChat implements ChatHook {
        INSTANCE;

        @Override
        public boolean isAvailable() {
            return false;
        }

        @Override
        public String getPrimaryGroup(Player player) {
            return "";
        }

        @Override
        public String getPrefix(Player player) {
            return "";
        }

        @Override
        public String getSuffix(Player player) {
            return "";
        }
    }
}
