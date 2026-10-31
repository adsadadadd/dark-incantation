package me.x1no4ekk.core.module;

import me.x1no4ekk.core.config.ConfigFiles;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * A self-contained feature of Core (commands, listeners, tasks and its own configuration).
 * <p>
 * Lifecycle, all on the main thread:
 * <ol>
 *     <li>{@link #createSchema(Connection)} — once, during onEnable, on a blocking connection;</li>
 *     <li>{@link #loadConfig(ConfigFiles)} — on enable and on every {@code /core reload};</li>
 *     <li>{@link #enable()} — once, after the first successful configuration load;</li>
 *     <li>{@link #disable()} — once, on plugin disable, in reverse registration order.</li>
 * </ol>
 * Modules must keep only UUID-keyed per-player state and drop it on {@code PlayerQuitEvent} and in {@link #disable()}.
 */
public interface CoreModule {

    /**
     * Short identifier used in log messages.
     */
    String id();

    /**
     * Creates this module's tables (idempotent DDL). Blocking; runs only during onEnable.
     */
    default void createSchema(Connection connection) throws SQLException {
    }

    /**
     * Parses and validates this module's configuration without touching live state.
     * <p>
     * Reload is all-or-nothing: the returned actions are run only if every module validated successfully,
     * otherwise all modules keep their previous configuration.
     *
     * @return action that publishes the parsed configuration
     * @throws Exception if the configuration is invalid; the message is shown to the administrator
     */
    default Runnable loadConfig(ConfigFiles files) throws Exception {
        return () -> {
        };
    }

    /**
     * Registers listeners, commands and tasks.
     */
    void enable();

    /**
     * Cancels tasks and clears per-player state.
     */
    default void disable() {
    }
}
