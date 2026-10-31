package me.x1no4ekk.core.hook;

import me.x1no4ekk.core.util.Numbers;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.Locale;

/**
 * Vault-backed hooks. Only loaded when Vault is enabled, so Vault classes are never touched otherwise.
 * Providers are looked up on every call (a cheap map lookup) instead of being cached, so a provider plugin
 * that is disabled or replaced is never pinned in memory.
 */
final class VaultHooks {

    private VaultHooks() {
    }

    private static <T> T provider(Class<T> type) {
        RegisteredServiceProvider<T> registration = Bukkit.getServicesManager().getRegistration(type);
        return registration == null ? null : registration.getProvider();
    }

    static final class VaultEconomy implements EconomyHook {

        @Override
        public boolean isAvailable() {
            Economy economy = provider(Economy.class);
            return economy != null && economy.isEnabled();
        }

        @Override
        public double getBalance(OfflinePlayer player) {
            Economy economy = provider(Economy.class);
            return economy == null ? 0D : economy.getBalance(player);
        }

        @Override
        public boolean has(OfflinePlayer player, double amount) {
            Economy economy = provider(Economy.class);
            return economy != null && economy.has(player, amount);
        }

        @Override
        public boolean withdraw(OfflinePlayer player, double amount) {
            Economy economy = provider(Economy.class);
            if (economy == null || amount < 0) {
                return false;
            }
            if (amount == 0) {
                return true;
            }
            EconomyResponse response = economy.withdrawPlayer(player, amount);
            return response != null && response.transactionSuccess();
        }

        @Override
        public boolean deposit(OfflinePlayer player, double amount) {
            Economy economy = provider(Economy.class);
            if (economy == null || amount < 0) {
                return false;
            }
            if (amount == 0) {
                return true;
            }
            EconomyResponse response = economy.depositPlayer(player, amount);
            return response != null && response.transactionSuccess();
        }

        @Override
        public String format(double amount) {
            Economy economy = provider(Economy.class);
            return economy == null ? Numbers.format(amount) : economy.format(amount);
        }
    }

    static final class VaultChat implements ChatHook {

        @Override
        public boolean isAvailable() {
            return provider(Chat.class) != null || provider(Permission.class) != null;
        }

        @Override
        public String getPrimaryGroup(Player player) {
            try {
                Chat chat = provider(Chat.class);
                if (chat != null) {
                    String group = chat.getPrimaryGroup(player);
                    if (group != null) {
                        return group.toLowerCase(Locale.ROOT);
                    }
                }
                Permission permission = provider(Permission.class);
                if (permission != null && permission.hasGroupSupport()) {
                    String group = permission.getPrimaryGroup(player);
                    if (group != null) {
                        return group.toLowerCase(Locale.ROOT);
                    }
                }
            } catch (RuntimeException ignored) {
                // Some permission plugins do not support groups.
            }
            return "";
        }

        @Override
        public String getPrefix(Player player) {
            Chat chat = provider(Chat.class);
            if (chat == null) {
                return "";
            }
            String prefix = chat.getPlayerPrefix(player);
            return prefix == null ? "" : prefix;
        }

        @Override
        public String getSuffix(Player player) {
            Chat chat = provider(Chat.class);
            if (chat == null) {
                return "";
            }
            String suffix = chat.getPlayerSuffix(player);
            return suffix == null ? "" : suffix;
        }
    }
}
