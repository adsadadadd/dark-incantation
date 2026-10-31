/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.md_5.bungee.api.ChatMessageType
 *  net.md_5.bungee.api.chat.BaseComponent
 *  net.md_5.bungee.api.chat.TextComponent
 *  org.bukkit.Bukkit
 *  org.bukkit.Color
 *  org.bukkit.Location
 *  org.bukkit.Material
 *  org.bukkit.Particle
 *  org.bukkit.Sound
 *  org.bukkit.World
 *  org.bukkit.boss.BarColor
 *  org.bukkit.boss.BarFlag
 *  org.bukkit.boss.BarStyle
 *  org.bukkit.boss.BossBar
 *  org.bukkit.entity.Player
 *  org.bukkit.inventory.ItemStack
 *  org.bukkit.plugin.Plugin
 *  org.bukkit.util.Vector
 */
package ru.bestrax.essentials.base.managers;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;
import ru.bestrax.essentials.EssentialsPlugin;
import ru.bestrax.essentials.api.EssentialsApi;
import ru.bestrax.essentials.api.User;
import ru.bestrax.essentials.api.config.Config;
import ru.bestrax.essentials.api.cooldown.Cooldowns;
import ru.bestrax.essentials.api.manager.UserManager;
import ru.bestrax.essentials.base.EssentialsData;
import ru.bestrax.essentials.base.objects.TrustUser;
import ru.bestrax.essentials.utils.BukkitUtils;
import ru.bestrax.essentials.utils.Direction;
import ru.bestrax.essentials.utils.DurabilityUtils;
import ru.bestrax.essentials.utils.LocationUtils;

public class TrustUserManager
implements UserManager {
    private final Map<String, User> usersByName = new ConcurrentHashMap<String, User>();
    private final Map<String, User> cachedUsers = new ConcurrentHashMap<String, User>();
    private final Map<String, BossBar> usersBossBarsCallRequests = new ConcurrentHashMap<String, BossBar>();

    public TrustUserManager() {
        ConcurrentHashMap tValues = new ConcurrentHashMap();
        Bukkit.getScheduler().runTaskTimerAsynchronously((Plugin)EssentialsPlugin.getProvidingPlugin(EssentialsPlugin.class), () -> {
            Config lang = EssentialsApi.getInstance().getLang();
            Config settings = EssentialsApi.getInstance().getSettings();
            if (settings.getBoolean("timeFixed")) {
                for (String timeFixedWorld : settings.getStringList("timeFixedWorld", new String[0])) {
                    World world = Bukkit.getWorld((String)timeFixedWorld);
                    if (world == null) continue;
                    Bukkit.getScheduler().callSyncMethod((Plugin)EssentialsApi.getInstance(), () -> {
                        world.setTime((long)settings.getInt("timeMeaning"));
                        return true;
                    });
                }
            }
            for (User user : this.usersByName.values()) {
                Player player = Bukkit.getPlayer((String)user.getUsername());
                if (player == null || !player.isOnline()) {
                    try {
                        this.usersByName.remove(user.getUsername().toLowerCase());
                    }
                    catch (Exception e) {
                        System.out.println(e.getLocalizedMessage());
                    }
                    continue;
                }
                TrustUser craftUser = (TrustUser)user;
                this.clear(craftUser.getCallRequests());
                if (craftUser.getGpsLocation() != null) {
                    if (!player.getLocation().getWorld().getName().equals(craftUser.getGpsLocation().getWorld().getName())) {
                        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, (BaseComponent)new TextComponent(lang.getString("GPS_IN_ANOTHER_WORLD", new String[0])));
                    } else {
                        long delta = (long)player.getLocation().distanceSquared(craftUser.getGpsLocation());
                        double radiusToPlayer = Math.sqrt(delta);
                        Location origin = player.getLocation();
                        Vector target = craftUser.getGpsLocation().toVector();
                        origin.setDirection(target.subtract(origin.toVector()));
                        int yaw = (int)((player.getLocation().getYaw() - origin.getYaw()) / 45.0f);
                        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, (BaseComponent)new TextComponent(lang.getString("GPS", "%blocks%", String.valueOf((int)radiusToPlayer), "%where%", Direction.getDirection(yaw))));
                    }
                }
                if (!user.isElytraFly() || user.isFly() || !settings.getBoolean("flyElytra")) continue;
                ItemStack chestPlace = player.getInventory().getChestplate();
                if (chestPlace != null && chestPlace.getType() == Material.ELYTRA) {
                    if (player.isOnGround()) continue;
                    if (DurabilityUtils.getDurability(chestPlace) <= 1) {
                        boolean unb;
                        boolean bl = unb = chestPlace.getItemMeta() != null && chestPlace.getItemMeta().isUnbreakable();
                        if (!unb) {
                            chestPlace.setAmount(chestPlace.getAmount() - 2);
                        }
                    }
                    DurabilityUtils.subtractDurability(chestPlace, 1);
                    player.getInventory().setChestplate(chestPlace);
                    player.setGliding(false);
                    player.setAllowFlight(true);
                    player.setFlying(true);
                    continue;
                }
                player.setAllowFlight(false);
            }
        }, 5L, 20L);
        double t = 0.0;
        boolean i = false;
        Bukkit.getScheduler().runTaskTimerAsynchronously((Plugin)EssentialsApi.getInstance(), () -> {
            Player player;
            Config lang = EssentialsApi.getInstance().getLang();
            Config settings = EssentialsApi.getInstance().getSettings();
            for (Map.Entry<String, User> entry : this.usersByName.entrySet()) {
                TrustUser trustUser = (TrustUser)entry.getValue();
                player = trustUser.getPlayer();
                for (Map.Entry<String, Long> targetAndTimeEntry : trustUser.getWaitTeleport().entrySet()) {
                    BossBar bossBar = this.usersBossBarsCallRequests.get(entry.getKey() + targetAndTimeEntry.getKey());
                    double left = targetAndTimeEntry.getValue() - System.currentTimeMillis();
                    if (left >= 200000.0) {
                        if (bossBar != null) {
                            bossBar.removeAll();
                        }
                        this.usersBossBarsCallRequests.remove(entry.getKey() + targetAndTimeEntry.getKey());
                        tValues.remove(entry.getKey() + targetAndTimeEntry.getKey());
                        trustUser.getWaitTeleport().remove(targetAndTimeEntry.getKey());
                        return;
                    }
                    if (left > 0.0 && bossBar == null) {
                        bossBar = Bukkit.createBossBar((String)lang.getString("TELEPORTING_BOSSBAR", "%seconds%", String.valueOf(EssentialsApi.getInstance().getSettings().getInt("teleportingTime") / 1000)), (BarColor)BarColor.valueOf((String)settings.getString("teleportingBarColor", new String[0]).toUpperCase()), (BarStyle)BarStyle.valueOf((String)settings.getString("teleportingBarStyle", new String[0]).toUpperCase()), (BarFlag[])new BarFlag[0]);
                        bossBar.addPlayer(player);
                        this.usersBossBarsCallRequests.put(entry.getKey() + targetAndTimeEntry.getKey(), bossBar);
                    } else {
                        if (left <= 0.0 && bossBar != null) {
                            try {
                                String message;
                                if (((Boolean)Bukkit.getScheduler().callSyncMethod((Plugin)EssentialsApi.getInstance(), () -> {
                                    if (trustUser.teleport(LocationUtils.stringToLocation(((String)targetAndTimeEntry.getKey()).split("\\[\\|]")[0], true))) {
                                        Sound sound = BukkitUtils.getSoundOfName("ENTITY_ENDER_EYE_DEATH");
                                        player.playSound(player.getEyeLocation(), sound == null ? BukkitUtils.getSoundOfName("ENTITY_ENDEREYE_DEATH") : sound, 1.0f, 1.0f);
                                        return true;
                                    }
                                    return false;
                                }).get()).booleanValue() && !(message = targetAndTimeEntry.getKey().split("\\[\\|]")[1]).isEmpty()) {
                                    player.sendMessage(message);
                                }
                                bossBar.removeAll();
                                this.usersBossBarsCallRequests.remove(entry.getKey() + targetAndTimeEntry.getKey());
                                tValues.remove(entry.getKey() + targetAndTimeEntry.getKey());
                                trustUser.getWaitTeleport().remove(targetAndTimeEntry.getKey());
                            }
                            catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                            catch (ExecutionException e) {
                                throw new RuntimeException(e);
                            }
                            return;
                        }
                        if (bossBar == null) {
                            tValues.remove(entry.getKey() + targetAndTimeEntry.getKey());
                            trustUser.getWaitTeleport().remove(targetAndTimeEntry.getKey());
                            return;
                        }
                    }
                    Location teleportLocation = LocationUtils.stringToLocation(targetAndTimeEntry.getKey().split("\\[\\|]")[0], true);
                    if (!Cooldowns.has(entry.getKey() + targetAndTimeEntry.getKey(), "bossbar")) {
                        player.sendTitle(lang.getString("TELEPORTING_TITLE", new String[0]), lang.getString("TELEPORTING_SUBTITLE", new String[0]));
                        Cooldowns.set(entry.getKey() + targetAndTimeEntry.getKey(), "bossbar", EssentialsApi.getInstance().getSettings().getInt("teleportingTime"));
                    }
                    bossBar.setTitle(lang.getString("TELEPORTING_BOSSBAR", "%seconds%", String.valueOf((int)(left / 1000.0) + 1)));
                    Random random = new Random();
                    double percentage = left / (double)EssentialsApi.getInstance().getSettings().getInt("teleportingTime");
                    if (!Cooldowns.has(entry.getKey() + targetAndTimeEntry.getKey(), "effects")) {
                        tValues.put(entry.getKey() + targetAndTimeEntry.getKey(), tValues.getOrDefault(entry.getKey() + targetAndTimeEntry.getKey(), 0.0) + 0.39269908169872414);
                        for (int j = 0; j < 4; ++j) {
                            Location loc = player.getLocation().clone().add(Math.cos(t + 1.5707963267948966 * (double)j), 0.4 * (Double)tValues.get(entry.getKey() + targetAndTimeEntry.getKey()), Math.sin((Double)tValues.get(entry.getKey() + targetAndTimeEntry.getKey()) + 1.5707963267948966 * (double)j));
                            this.spawnRedstone(loc, settings.getFile().getDouble("teleportingParticlesRed"), settings.getFile().getDouble("teleportingParticlesGreen"), settings.getFile().getDouble("teleportingParticlesBlue"));
                            Location tpLoc = teleportLocation.clone().add(Math.cos(t + 1.5707963267948966 * (double)j), 0.4 * (Double)tValues.get(entry.getKey() + targetAndTimeEntry.getKey()), Math.sin((Double)tValues.get(entry.getKey() + targetAndTimeEntry.getKey()) + 1.5707963267948966 * (double)j));
                            this.spawnRedstone(tpLoc, settings.getFile().getDouble("teleportingParticlesRed"), settings.getFile().getDouble("teleportingParticlesGreen"), settings.getFile().getDouble("teleportingParticlesBlue"));
                        }
                        StringBuilder stringBuilder = new StringBuilder();
                        if ((Double)tValues.get(stringBuilder.append(entry.getKey()).append(targetAndTimeEntry.getKey()).toString()) > 6.5) {
                            tValues.put(entry.getKey() + targetAndTimeEntry.getKey(), 0.0);
                        }
                    }
                    bossBar.setProgress(percentage);
                }
            }
            for (Map.Entry<String, User> entry : this.usersBossBarsCallRequests.entrySet()) {
                List playerList = ((BossBar)entry.getValue()).getPlayers();
                if (playerList.size() == 0) {
                    this.usersBossBarsCallRequests.remove(entry.getKey());
                    return;
                }
                player = (Player)playerList.get(0);
                if (player != null && player.isOnline()) continue;
                this.usersBossBarsCallRequests.remove(entry.getKey());
            }
        }, 1L, 1L);
    }

    private void spawnRedstone(Location loc, double r, double g, double b) {
        double version = Double.parseDouble(EssentialsApi.getInstance().getFormattedVersion());
        if (version >= 1.13) {
            Object dust;
            Class<?> dustOptionsClass;
            try {
                dustOptionsClass = Class.forName("org.bukkit.Particle").getClasses()[0];
            }
            catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
            try {
                dust = dustOptionsClass.getConstructor(Color.class, Float.TYPE).newInstance(Color.fromRGB((int)((int)(r * 255.0)), (int)((int)(g * 255.0)), (int)((int)(b * 255.0))), Float.valueOf(1.0f));
            }
            catch (IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
            loc.getWorld().spawnParticle(Particle.REDSTONE, loc.getX(), loc.getY(), loc.getZ(), 0, dust);
        } else {
            loc.getWorld().spawnParticle(Particle.REDSTONE, loc.getX(), loc.getY(), loc.getZ(), 0, r, g, b, 1.0);
        }
    }

    @Override
    public User getUser(Player player) {
        User user = this.getUser(player.getName());
        if (user == null) {
            this.loadUser(player.getName());
        }
        return user;
    }

    @Override
    public User getUserUnsafe(Player player) {
        if (player == null) {
            return null;
        }
        User user = this.usersByName.get(player.getName().toLowerCase());
        if (user == null) {
            return this.cachedUsers.get(player.getName().toLowerCase());
        }
        return user;
    }

    @Override
    public User getUser(String name) {
        User user = this.usersByName.get(name.toLowerCase());
        if (user == null && this.cachedUsers.containsKey(name.toLowerCase())) {
            this.usersByName.put(name.toLowerCase(), this.cachedUsers.get(name.toLowerCase()));
            this.cachedUsers.remove(name.toLowerCase());
            return this.usersByName.get(name.toLowerCase());
        }
        return user;
    }

    @Override
    public Map<String, User> getUsers() {
        return this.usersByName;
    }

    @Override
    public void loadUser(String name) {
        String lowerName = name.toLowerCase();
        if (this.cachedUsers.containsKey(lowerName)) {
            this.usersByName.put(lowerName, this.cachedUsers.get(lowerName));
            this.cachedUsers.remove(lowerName);
            return;
        }
        if (!this.usersByName.containsKey(lowerName)) {
            this.usersByName.put(lowerName, EssentialsData.loadUser(name.toLowerCase()));
        }
    }

    @Override
    public void removeUser(User user) {
        if (this.usersByName.containsValue(user) && user != null) {
            this.removeUser(user.getUsername());
        }
    }

    @Override
    public void removeUser(String name) {
        User user;
        String nameLower = name.toLowerCase();
        if (this.usersByName.remove(nameLower, user = this.usersByName.get(nameLower))) {
            this.cachedUsers.put(nameLower, user);
            if (user != null) {
                TrustUser trustUser = (TrustUser)user;
                BukkitUtils.runTaskAsync(trustUser::save, EssentialsPlugin.getProvidingPlugin(EssentialsPlugin.class));
            }
        } else {
            throw new RuntimeException("\u041d\u0435 \u0443\u0434\u0430\u043b\u043e\u0441\u044c \u0443\u0434\u0430\u043b\u0438\u0442\u044c \u043f\u043e\u043b\u044c\u0437\u043e\u0432\u0430\u0442\u0435\u043b\u044f " + nameLower);
        }
    }

    private void clear(Map<String, Long> data) {
        for (Map.Entry<String, Long> request : data.entrySet()) {
            Player player;
            String name = request.getKey();
            Long time = request.getValue();
            if (time + 122000L < System.currentTimeMillis()) {
                data.remove(name);
            }
            if ((player = Bukkit.getPlayerExact((String)name)) != null && player.isOnline()) continue;
            data.remove(name);
        }
    }
}
