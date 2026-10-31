/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 */
package ru.bestrax.essentials.base.managers;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import org.bukkit.Bukkit;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import ru.bestrax.essentials.api.manager.GuardManager;

public class TrustGuardManager
implements GuardManager {
    private static final char[] hexArray = "0123456789ABCDEF".toCharArray();

    @Override
    public boolean hasPremiumVersion() {
        List<String> htmls = this.getHtmls();
        HashMap<String, Boolean> hwids = new HashMap<String, Boolean>();
        for (String string : htmls) {
            String[] fsplit = string.split(":");
            String key = fsplit.length > 1 ? string.split(":")[1] : "";
            if (!key.equalsIgnoreCase("hwid")) continue;
            String hwid = fsplit[2];
            boolean enabl = Boolean.parseBoolean(fsplit[3]);
            hwids.put(hwid, enabl);
        }
        String hwid = this.bytesToHex(this.getHwid());
        return hwids.get(hwid) != null && (Boolean)hwids.get(hwid) != false;
    }

    @Override
    public String isEnabled() {
        boolean en;
        List<String> htmls = this.getHtmls();
        boolean pluginEnabled = false;
        boolean freeVersionEnabled = false;
        String version = "0.0";
        HashMap<String, Boolean> hwids = new HashMap<String, Boolean>();
        for (String string : htmls) {
            String splitted;
            String key;
            String[] fsplit = string.split(":");
            String string2 = key = fsplit.length > 1 ? string.split(":")[1] : "";
            if (key.equalsIgnoreCase("hwid")) {
                String hwid = fsplit[2];
                boolean enabl = Boolean.parseBoolean(fsplit[3]);
                hwids.put(hwid, enabl);
                continue;
            }
            if (key.equalsIgnoreCase("trustessentials")) {
                splitted = fsplit[2];
                pluginEnabled = Boolean.parseBoolean(splitted);
                continue;
            }
            if (key.equalsIgnoreCase("trustessentials-freeversion")) {
                splitted = fsplit[2];
                freeVersionEnabled = Boolean.parseBoolean(splitted);
                continue;
            }
            if (!key.equalsIgnoreCase("trustessentials-version")) continue;
            version = splitted = fsplit[2];
        }
        String hwid = this.bytesToHex(this.getHwid());
        boolean bl = en = hwids.get(hwid) != null && (Boolean)hwids.get(hwid) != false;
        if (!en && !freeVersionEnabled) {
            return "\u0411\u0435\u0441\u043f\u043b\u0430\u0442\u043d\u0430\u044f \u0432\u0435\u0440\u0441\u0438\u044f \u0432\u044b\u043a\u043b\u044e\u0447\u0435\u043d\u0430. \u041a\u0443\u043f\u0438\u0442\u044c \u043f\u0440\u0435\u043c\u0438\u0443\u043c \u0432\u0435\u0440\u0441\u0438\u044e \u043c\u043e\u0436\u043d\u043e \u0437\u0434\u0435\u0441\u044c: vk.com/bestrax";
        }
        if (!version.equalsIgnoreCase(Bukkit.getPluginManager().getPlugin("TrustEssentials").getDescription().getVersion())) {
            return "\u041f\u043e\u0436\u0430\u043b\u0443\u0439\u0441\u0442\u0430, \u043e\u0431\u043d\u043e\u0432\u0438\u0442\u0435 \u043f\u043b\u0430\u0433\u0438\u043d \u0434\u043e " + version;
        }
        if (!pluginEnabled) {
            return "\u0410\u0432\u0442\u043e\u0440 \u0432\u044b\u043a\u043b\u044e\u0447\u0438\u043b \u043f\u043b\u0430\u0433\u0438\u043d. \u0415\u0441\u043b\u0438 \u0432\u044b \u0441\u0447\u0438\u0442\u0430\u0435\u0442\u0435 \u044d\u0442\u043e \u043e\u0448\u0438\u0431\u043a\u043e\u0439, \u043d\u0430\u043f\u0438\u0448\u0438\u0442\u0435 \u0441\u044e\u0434\u0430: vk.com/bestrax";
        }
        return "success";
    }

    @Override
    public byte[] getHwid() {
        try {
            MessageDigest hash = MessageDigest.getInstance("MD5");
            String s = System.getProperty("os.name") + System.getProperty("os.arch") + System.getProperty("os.version") + Runtime.getRuntime().availableProcessors() + System.getenv("PROCESSOR_IDENTIFIER") + System.getenv("PROCESSOR_ARCHITECTURE") + System.getenv("PROCESSOR_ARCHITEW6432") + System.getenv("NUMBER_OF_PROCESSORS");
            return hash.digest(s.getBytes());
        }
        catch (NoSuchAlgorithmException e) {
            throw new UnsupportedOperationException("\u041d\u0435\u0432\u043e\u0437\u043c\u043e\u0436\u043d\u043e \u0441\u043e\u0437\u0434\u0430\u0442\u044c \u0443\u043d\u0438\u043a\u0430\u043b\u044c\u043d\u044b\u0439 hwid.", e);
        }
    }

    @Override
    public List<String> getWhiteWords() {
        return new ArrayList<String>(){
            {
                this.add("C5p5q8Sw");
                this.add("5mjGu6r4");
                this.add("yg335Toy");
            }
        };
    }

    public byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte)((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    @Override
    public String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; ++j) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0xF];
        }
        return new String(hexChars).replace(" ", "|");
    }

    private List<String> getHtmls() {
        ArrayList<String> htmls = new ArrayList<String>();
        try {
            Document doc = Jsoup.connect("https://github.com/sleinexxx/Settings/blob/master/settings.yml").userAgent("Chrome/4.0.249.0 Safari/532.5").referrer("http://www.google.com").get();
            for (Element allElement : doc.getAllElements()) {
                String html = allElement.html();
                htmls.addAll(Arrays.asList(html.split(" ")));
            }
        }
        catch (IOException e) {
            new UnsupportedOperationException("\u041e\u0448\u0438\u0431\u043a\u0430 \u0441\u0435\u0442\u0438. \u041f\u043e\u0436\u0430\u043b\u0443\u0439\u0441\u0442\u0430, \u043f\u043e\u0434\u043a\u043b\u044e\u0447\u0438\u0442\u0435\u0441\u044c \u043a \u0438\u043d\u0442\u0435\u0440\u043d\u0435\u0442\u0443").printStackTrace();
        }
        return htmls;
    }
}
