package me.x1no4ekk.core.kit;

import me.x1no4ekk.core.CorePlugin;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

/**
 * Applies administrative edits to kits.yml and republishes the kit configuration.
 * <p>
 * Each edit: read the file (async) → apply the change and validate (main thread: item stacks are (de)serialized
 * through the server registries) → atomic write (async) → publish. Edits are chained, so a second edit always reads
 * the file written by the first one.
 */
final class KitFileEditor {

    static final String FILE = "kits.yml";

    private final CorePlugin plugin;
    private final KitModule module;
    private final Executor async;
    /** Last queued edit; accessed on the main thread only. */
    private CompletableFuture<?> tail = CompletableFuture.completedFuture(null);

    private record Texts(String current, String defaults) {
    }

    private record Prepared(String text, KitSettings settings) {
    }

    KitFileEditor(CorePlugin plugin, KitModule module) {
        this.plugin = plugin;
        this.module = module;
        this.async = task -> {
            if (plugin.isEnabled()) {
                Bukkit.getScheduler().runTaskAsynchronously(plugin, task);
            }
        };
    }

    /**
     * Queues an edit of kits.yml. {@code change} runs on the main thread against the current file contents.
     *
     * @return the published configuration; completes on the main thread
     */
    CompletableFuture<KitSettings> edit(Consumer<YamlConfiguration> change) {
        CompletableFuture<KitSettings> result = tail
                .handle((ignored, error) -> null)
                .thenCompose(ignored -> run(change));
        tail = result;
        return result;
    }

    private CompletableFuture<KitSettings> run(Consumer<YamlConfiguration> change) {
        return CompletableFuture.supplyAsync(this::read, async)
                .thenApplyAsync(texts -> prepare(texts, change), plugin.mainThread())
                .thenApplyAsync(prepared -> {
                    write(prepared.text());
                    return prepared.settings();
                }, async)
                .thenApplyAsync(settings -> {
                    module.publish(settings);
                    return settings;
                }, plugin.mainThread());
    }

    private Texts read() {
        try {
            File file = file();
            if (!file.exists()) {
                plugin.saveResource(FILE, false);
            }
            String current = Files.readString(file.toPath(), StandardCharsets.UTF_8);
            String defaults = "";
            try (InputStream in = plugin.getResource(FILE)) {
                if (in != null) {
                    defaults = new String(in.readAllBytes(), StandardCharsets.UTF_8);
                }
            }
            return new Texts(current, defaults);
        } catch (IOException e) {
            throw new UncheckedIOException("cannot read " + FILE + ": " + e.getMessage(), e);
        }
    }

    private static Prepared prepare(Texts texts, Consumer<YamlConfiguration> change) {
        try {
            YamlConfiguration yaml = new YamlConfiguration();
            yaml.loadFromString(texts.current());
            change.accept(yaml);
            String output = yaml.saveToString();
            YamlConfiguration defaults = new YamlConfiguration();
            defaults.loadFromString(texts.defaults());
            yaml.setDefaults(defaults);
            return new Prepared(output, KitSettings.load(yaml));
        } catch (InvalidConfigurationException e) {
            throw new CompletionException(new IllegalArgumentException(FILE + ": " + e.getMessage(), e));
        }
    }

    private void write(String text) {
        Path target = file().toPath();
        Path temp = target.resolveSibling(FILE + ".tmp");
        try {
            Files.writeString(temp, text, StandardCharsets.UTF_8);
            try {
                Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("cannot write " + FILE + ": " + e.getMessage(), e);
        }
    }

    private File file() {
        return new File(plugin.getDataFolder(), FILE);
    }

    /**
     * Human-readable reason of a failed edit.
     */
    static String describe(Throwable error) {
        Throwable cause = error;
        while (cause instanceof CompletionException && cause.getCause() != null) {
            cause = cause.getCause();
        }
        String message = cause.getMessage();
        return message == null || message.isBlank() ? cause.getClass().getSimpleName() : message;
    }
}
