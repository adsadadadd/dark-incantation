package me.x1no4ekk.core.hook;

import org.bukkit.OfflinePlayer;

/**
 * Economy access (АРЫ) through Vault. Implemented by the Economy plugin's Vault provider.
 * When Vault or a provider is missing, {@link #isAvailable()} is {@code false} and every operation fails safely.
 */
public interface EconomyHook {

    boolean isAvailable();

    double getBalance(OfflinePlayer player);

    boolean has(OfflinePlayer player, double amount);

    /**
     * @return {@code true} if the money was taken
     */
    boolean withdraw(OfflinePlayer player, double amount);

    /**
     * @return {@code true} if the money was given
     */
    boolean deposit(OfflinePlayer player, double amount);

    /**
     * Formats an amount with the provider's currency name (e.g. {@code 150 АР}).
     */
    String format(double amount);
}
