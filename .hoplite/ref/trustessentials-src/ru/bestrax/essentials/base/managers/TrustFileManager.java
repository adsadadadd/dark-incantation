/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.configuration.file.FileConfiguration
 *  org.bukkit.configuration.file.YamlConfiguration
 */
package ru.bestrax.essentials.base.managers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Path;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import ru.bestrax.essentials.api.manager.FileManager;

public class TrustFileManager
implements FileManager {
    @Override
    public FileConfiguration getFile(Class resourceAs, File folder, String name) {
        File file = new File(folder, name);
        if (!file.exists()) {
            this.saveResource(resourceAs, file.toPath());
        }
        return YamlConfiguration.loadConfiguration((File)file);
    }

    @Override
    public FileConfiguration save(File folder, String name, FileConfiguration configuration) {
        try {
            configuration.save(new File(folder, name));
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return configuration;
    }

    @Override
    public InputStream getResource(Class resourceAs, String filename) {
        if (filename == null) {
            throw new IllegalArgumentException("Filename cannot be null");
        }
        try {
            URL url = resourceAs.getClassLoader().getResource(filename);
            if (url == null) {
                return null;
            }
            URLConnection connection = url.openConnection();
            connection.setUseCaches(false);
            return connection.getInputStream();
        }
        catch (IOException var4) {
            return null;
        }
    }

    @Override
    public void saveResource(Class resourceAs, Path path) {
        if (path != null) {
            InputStream in = this.getResource(resourceAs, path.getFileName().toString());
            File outFile = path.toFile();
            File outDir = path.getParent().toFile();
            if (!outDir.exists()) {
                outDir.mkdirs();
            }
            try {
                int len;
                FileOutputStream out = new FileOutputStream(outFile);
                byte[] buf = new byte[1024];
                while ((len = in.read(buf)) > 0) {
                    ((OutputStream)out).write(buf, 0, len);
                }
                ((OutputStream)out).close();
                in.close();
            }
            catch (IOException var10) {
                var10.printStackTrace();
            }
        } else {
            throw new IllegalArgumentException("ResourcePath cannot be null or empty");
        }
    }
}
