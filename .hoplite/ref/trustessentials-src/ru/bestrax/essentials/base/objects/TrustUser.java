/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.md_5.bungee.api.ChatColor
 *  net.md_5.bungee.api.chat.ClickEvent
 *  net.md_5.bungee.api.chat.ClickEvent$Action
 *  net.md_5.bungee.api.chat.HoverEvent
 *  net.md_5.bungee.api.chat.HoverEvent$Action
 *  net.md_5.bungee.api.chat.TextComponent
 *  org.bukkit.Bukkit
 *  org.bukkit.GameMode
 *  org.bukkit.Location
 *  org.bukkit.command.CommandSender
 *  org.bukkit.entity.Player
 *  org.bukkit.inventory.Inventory
 *  org.bukkit.metadata.FixedMetadataValue
 *  org.bukkit.metadata.MetadataValue
 */
package ru.bestrax.essentials.base.objects;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import ru.bestrax.essentials.api.EssentialsApi;
import ru.bestrax.essentials.api.EssentialsStatistic;
import ru.bestrax.essentials.api.Home;
import ru.bestrax.essentials.api.Kit;
import ru.bestrax.essentials.api.User;
import ru.bestrax.essentials.api.config.Config;
import ru.bestrax.essentials.api.events.UserAddIgnoredEvent;
import ru.bestrax.essentials.api.events.UserBossbarStatusChangeEvent;
import ru.bestrax.essentials.api.events.UserChangeGamemodeEvent;
import ru.bestrax.essentials.api.events.UserChatStatusChangeEvent;
import ru.bestrax.essentials.api.events.UserElytraFlyStatusChangeEvent;
import ru.bestrax.essentials.api.events.UserFakeNameChangeEvent;
import ru.bestrax.essentials.api.events.UserFlyStatusChangeEvent;
import ru.bestrax.essentials.api.events.UserGodStatusChangeEvent;
import ru.bestrax.essentials.api.events.UserGpsLocationChangeEvent;
import ru.bestrax.essentials.api.events.UserHomeAddEvent;
import ru.bestrax.essentials.api.events.UserHomeRemoveEvent;
import ru.bestrax.essentials.api.events.UserInventoryClearEvent;
import ru.bestrax.essentials.api.events.UserLastLocationChangeEvent;
import ru.bestrax.essentials.api.events.UserMsgToggleChangeEvent;
import ru.bestrax.essentials.api.events.UserRemoveIgnoredEvent;
import ru.bestrax.essentials.api.events.UserTeleportEvent;
import ru.bestrax.essentials.api.events.UserTpToggleChangeEvent;
import ru.bestrax.essentials.api.events.UserVanishStatusChangeEvent;
import ru.bestrax.essentials.api.manager.UserManager;
import ru.bestrax.essentials.base.EssentialsData;
import ru.bestrax.essentials.base.objects.TrustCustomEnderChest;
import ru.bestrax.essentials.base.objects.TrustHome;
import ru.bestrax.essentials.listeners.PlayerListener;
import ru.bestrax.essentials.utils.BukkitUtils;
import ru.bestrax.essentials.utils.ComponentBuilder;
import ru.bestrax.essentials.utils.LocationUtils;
import ru.bestrax.essentials.utils.MessagesUtils;

public class TrustUser
implements User {
    private static final UserManager MANAGER = EssentialsApi.getUserManager();
    private final Map<String, Long> callReguests = new ConcurrentHashMap<String, Long>();
    private final Map<String, Long> waitTeleport = new ConcurrentHashMap<String, Long>();
    private final String name;
    private String fakeName;
    private boolean recipeSee;
    private boolean fly;
    private List<String> ignores;
    private boolean god;
    private String replyPlayer;
    private boolean tpToggle;
    private boolean socialSpy;
    private String targetSpy;
    private boolean bossbarToggle;
    private boolean msgToggle;
    private boolean vanish;
    private Location gpsLocation;
    private boolean elytraFly;
    private Location lastLocation;
    private final TrustCustomEnderChest enderChest;
    private final Map<Kit, Timestamp> kits;
    private final Map<String, Home> homes;
    private long backTime;
    private transient long lastActivity;
    private final boolean firstJoin;
    private boolean chatToggle;
    private boolean saved = false;

    public TrustUser(String name, String fakeName, boolean fly, boolean god, boolean tpToggle, boolean msgToggle, boolean bossbarToggle, boolean chatToggle, boolean first, Location lastLocation, Map<Kit, Timestamp> kits, Map<String, Home> homes, List<String> ignores, Inventory enderChest, int enderChestRows) {
        this.name = name;
        this.kits = kits;
        this.fakeName = fakeName;
        this.chatToggle = chatToggle;
        this.bossbarToggle = bossbarToggle;
        this.msgToggle = msgToggle;
        this.firstJoin = first;
        this.fly = fly;
        this.god = god;
        this.tpToggle = tpToggle;
        this.lastLocation = lastLocation;
        this.ignores = ignores;
        this.enderChest = new TrustCustomEnderChest(name, EssentialsApi.getInstance().getLang().getString("ENDER_CHEST_NAME", new String[0]), enderChestRows);
        this.enderChest.fillContent(enderChest.getContents());
        this.homes = homes;
        this.lastActivity = System.currentTimeMillis();
    }

    @Override
    public String getReplyPlayer() {
        return this.replyPlayer;
    }

    @Override
    public boolean isMsgToggle() {
        return this.msgToggle;
    }

    @Override
    public void setReplyPlayer(String replyPlayer) {
        this.replyPlayer = replyPlayer;
    }

    @Override
    public boolean isSocialSpy() {
        return this.socialSpy;
    }

    @Override
    public void setTargetSpy(String targetSpy) {
        this.targetSpy = targetSpy;
    }

    @Override
    public void setSocialSpy(boolean socialSpy) {
        this.socialSpy = socialSpy;
    }

    @Override
    public String getTargetSpy() {
        return this.targetSpy;
    }

    @Override
    public List<String> getIgnores() {
        return this.ignores;
    }

    @Override
    public long getStatistic(EssentialsStatistic statistic) {
        return EssentialsApi.getStatisticManager().getUser(this.getUsername()).getStatistic(statistic);
    }

    @Override
    public void setStatistic(EssentialsStatistic statistic, long value) {
        EssentialsApi.getStatisticManager().getUser(this.getUsername()).setStatistic(statistic, value);
    }

    @Override
    public String getUsername() {
        return this.name;
    }

    @Override
    public String getFakeName() {
        return this.fakeName;
    }

    @Override
    public boolean isBossbarToggle() {
        return this.bossbarToggle;
    }

    @Override
    public boolean isChatToggle() {
        return this.chatToggle;
    }

    @Override
    public Player getPlayer() {
        return Bukkit.getPlayer((String)this.getUsername());
    }

    @Override
    public Map<Kit, Timestamp> getKits() {
        for (Map.Entry<Kit, Timestamp> kitTimestampEntry : this.kits.entrySet()) {
            Kit kit = kitTimestampEntry.getKey();
            long time = kitTimestampEntry.getValue().getTime();
            if (System.currentTimeMillis() <= time) continue;
            this.kits.remove(kit);
            EssentialsData.removeKitFromSql(this.getUsername(), kit);
        }
        return this.kits;
    }

    @Override
    public void setGpsLocation(Location gpsLocation) {
        UserGpsLocationChangeEvent userGpsLocationChangeEvent = new UserGpsLocationChangeEvent((User)this, gpsLocation);
        BukkitUtils.callEvent(userGpsLocationChangeEvent, EssentialsApi.getInstance());
        if (!userGpsLocationChangeEvent.isCancelled()) {
            this.gpsLocation = gpsLocation;
        }
    }

    @Override
    public Location getGpsLocation() {
        return this.gpsLocation;
    }

    @Override
    public void setBossbarToggle(boolean bool) {
        UserBossbarStatusChangeEvent event = new UserBossbarStatusChangeEvent(this, bool);
        BukkitUtils.callEvent(event, EssentialsApi.getInstance());
        if (!event.isCancelled()) {
            Player player;
            if (this.bossbarToggle != bool) {
                this.saved = true;
                this.bossbarToggle = bool;
            }
            if ((player = this.getPlayer()) != null) {
                if (bool) {
                    EssentialsApi.getBossBarManager().add(this.getPlayer());
                } else {
                    EssentialsApi.getBossBarManager().remove(this.getPlayer());
                }
            }
        }
    }

    @Override
    public void addKit(Kit kit) {
        this.getKits();
        if (this.getPlayer() != null && this.getPlayer().isOnline() && !this.getPlayer().hasPermission("trustessentials.kit.cooldown.bypass")) {
            long time = System.currentTimeMillis() + (long)kit.getCooldown() * 1000L;
            this.kits.put(kit, new Timestamp(time));
            EssentialsData.addKitToSql(this.getUsername(), kit.getName(), time);
        }
    }

    @Override
    public void setElytraFly(boolean bool, boolean message) {
        UserElytraFlyStatusChangeEvent event = new UserElytraFlyStatusChangeEvent(this, bool);
        BukkitUtils.callEvent(event, EssentialsApi.getInstance());
        if (!event.isCancelled()) {
            this.elytraFly = bool;
            Player player = this.getPlayer();
            if (player != null && player.isOnline()) {
                Config lang = EssentialsApi.getInstance().getLang();
                if (!this.elytraFly) {
                    player.setAllowFlight(false);
                }
                if (message) {
                    player.sendMessage(this.elytraFly ? lang.getString("FLYELYTRA_ON", new String[0]) : lang.getString("FLYELYTRA_OFF", new String[0]));
                }
            }
        }
    }

    @Override
    public void setChatToggle(boolean bool) {
        UserChatStatusChangeEvent event = new UserChatStatusChangeEvent(this, bool);
        BukkitUtils.callEvent(event, EssentialsApi.getInstance());
        if (!event.isCancelled()) {
            Player player;
            if (this.chatToggle != bool) {
                this.saved = true;
                this.chatToggle = bool;
            }
            if ((player = this.getPlayer()) != null) {
                // empty if block
            }
        }
    }

    @Override
    public boolean isElytraFly() {
        return this.elytraFly;
    }

    @Override
    public long getBackTime() {
        return this.backTime;
    }

    @Override
    public void setBackTime(long time) {
        this.backTime = time;
    }

    @Override
    public boolean isCooldown(Kit kit) {
        return this.getPlayer() != null && this.getPlayer().isOnline() && this.getPlayer().hasPermission("trustessentials.kit.cooldown.bypass") ? false : this.getKits().containsKey(kit);
    }

    @Override
    public int getCooldown(Kit kit) {
        if (this.getPlayer() != null && this.getPlayer().isOnline() && this.getPlayer().hasPermission("trustessentials.kit.cooldown.bypass")) {
            return -1;
        }
        Timestamp time = this.getKits().get(kit);
        return time == null ? 0 : (int)((time.getTime() - System.currentTimeMillis()) / 1000L);
    }

    @Override
    public Map<String, Home> getHomes() {
        return this.homes;
    }

    @Override
    public void addHome(String name, Location location) {
        if (!this.homes.containsKey(name)) {
            TrustHome home = new TrustHome(name, location);
            UserHomeAddEvent event = new UserHomeAddEvent((User)this, home);
            BukkitUtils.callEvent(event, EssentialsApi.getInstance());
            if (event.isCancelled()) {
                return;
            }
            this.homes.put(name, home);
            EssentialsData.addHomeToSql(this.getUsername(), name, home.getLocation());
        }
    }

    @Override
    public void removeHome(String home) {
        UserHomeRemoveEvent event = new UserHomeRemoveEvent((User)this, home);
        BukkitUtils.callEvent(event, EssentialsApi.getInstance());
        if (!event.isCancelled()) {
            this.homes.remove(home);
            EssentialsData.removeHomeFromSql(this.getUsername(), home);
        }
    }

    @Override
    public boolean teleport(Location location) {
        Player player = this.getPlayer();
        if (player == null) {
            return false;
        }
        Location old = player.getLocation();
        UserTeleportEvent userTeleportEvent = new UserTeleportEvent((User)this, location);
        BukkitUtils.callEvent(userTeleportEvent, EssentialsApi.getInstance());
        if (userTeleportEvent.isCancelled()) {
            return false;
        }
        if (player.teleport(location)) {
            long godTime = EssentialsApi.getInstance().getSettings().getInt("godAfterTeleport");
            if (godTime > 0L) {
                PlayerListener.gods.put(player.getName(), System.currentTimeMillis() + godTime);
            }
            this.setLastLocation(old);
            return true;
        }
        return false;
    }

    @Override
    public void addIgnored(String ignored) {
        UserAddIgnoredEvent event = new UserAddIgnoredEvent((User)this, this.name);
        BukkitUtils.callEvent(event, EssentialsApi.getInstance());
        if (!event.isCancelled()) {
            this.ignores.add(ignored);
            EssentialsData.addIgnoredToSql(this.getUsername(), ignored);
        }
    }

    @Override
    public void removeIgnored(String ignored) {
        UserRemoveIgnoredEvent event = new UserRemoveIgnoredEvent((User)this, this.name);
        BukkitUtils.callEvent(event, EssentialsApi.getInstance());
        if (!event.isCancelled()) {
            this.ignores.remove(ignored);
            EssentialsData.removeIgnoredFromSql(this.getUsername(), ignored);
        }
    }

    @Override
    public Location getLastLocation() {
        return this.lastLocation;
    }

    @Override
    public TrustCustomEnderChest getEnderChest() {
        return this.enderChest;
    }

    @Override
    public boolean isFirstJoin() {
        return this.firstJoin;
    }

    @Override
    public boolean isOnline() {
        return false;
    }

    @Override
    public boolean isFly() {
        return this.fly;
    }

    @Override
    public boolean isGod() {
        return this.god;
    }

    @Override
    public boolean isTpToggle() {
        return this.tpToggle;
    }

    @Override
    public boolean isVanished() {
        boolean vanishMetaStatus;
        if (this.getPlayer() == null) {
            return this.vanish;
        }
        List values = this.getPlayer().getMetadata("trustessentials_vanished");
        this.vanish = vanishMetaStatus = !values.isEmpty() && ((MetadataValue)values.get(0)).asBoolean();
        return vanishMetaStatus;
    }

    @Override
    public void setFakeName(String name, boolean message) {
        Player player = this.getPlayer();
        if (player != null && player.isOnline()) {
            UserFakeNameChangeEvent event = new UserFakeNameChangeEvent((User)this, name);
            BukkitUtils.callEvent(event, EssentialsApi.getInstance());
            if (!event.isCancelled()) {
                this.saved = true;
                player.setDisplayName(name);
                player.setCustomName(name);
                player.setPlayerListName(name == null ? this.getUsername() : name);
                player.setCustomNameVisible(name != null);
                this.fakeName = name;
                if (message) {
                    player.sendMessage(EssentialsApi.getInstance().getLang().getString("NICKNAME", new String[0]));
                }
            }
        }
    }

    @Override
    public void clearInventory(boolean message) {
        Player player;
        UserInventoryClearEvent event = new UserInventoryClearEvent(this);
        BukkitUtils.callEvent(event, EssentialsApi.getInstance());
        if (!event.isCancelled() && (player = this.getPlayer()) != null && player.isOnline()) {
            player.getInventory().clear();
            player.sendMessage(EssentialsApi.getInstance().getLang().getString("CLEAR_INVENTORY", new String[0]));
        }
    }

    @Override
    public void setGod(boolean bool, boolean message) {
        if (this.god != bool) {
            this.saved = true;
        }
        UserGodStatusChangeEvent event = new UserGodStatusChangeEvent(this, bool);
        BukkitUtils.callEvent(event, EssentialsApi.getInstance());
        if (!event.isCancelled()) {
            this.god = bool;
            Player player = this.getPlayer();
            if (player != null && player.isOnline()) {
                Config lang = EssentialsApi.getInstance().getLang();
                if (this.god) {
                    if (player.getHealth() != 0.0) {
                        player.setHealth(player.getMaxHealth());
                        player.setFoodLevel(20);
                    }
                    if (message) {
                        MessagesUtils.sendMessages((CommandSender)player, lang.getStringList("GOD_ON", new String[0]));
                    }
                } else if (message) {
                    MessagesUtils.sendMessages((CommandSender)player, lang.getStringList("GOD_OFF", new String[0]));
                }
            }
        }
    }

    @Override
    public void setFly(boolean bool, boolean message) {
        if (this.fly != bool) {
            this.saved = true;
        }
        UserFlyStatusChangeEvent event = new UserFlyStatusChangeEvent(this, bool);
        BukkitUtils.callEvent(event, EssentialsApi.getInstance());
        if (!event.isCancelled()) {
            this.fly = bool;
            Player player = this.getPlayer();
            if (player != null && player.isOnline()) {
                Config lang = EssentialsApi.getInstance().getLang();
                player.setAllowFlight(this.fly);
                if (!player.isOnGround()) {
                    player.setFlying(this.fly);
                }
                if (message) {
                    player.sendMessage(this.fly ? lang.getString("FLY_ON", new String[0]) : lang.getString("FLY_OFF", new String[0]));
                }
            }
        }
    }

    @Override
    public void setVanished(boolean bool, boolean message) {
        if (this.vanish != bool) {
            this.saved = true;
        }
        UserVanishStatusChangeEvent event = new UserVanishStatusChangeEvent(this, bool);
        BukkitUtils.callEvent(event, EssentialsApi.getInstance());
        if (!event.isCancelled()) {
            this.vanish = bool;
            Player player = this.getPlayer();
            if (player != null && player.isOnline()) {
                if (!this.isVanished()) {
                    player.setMetadata("trustessentials_vanished", (MetadataValue)new FixedMetadataValue(Bukkit.getPluginManager().getPlugin("TrustEssentials"), (Object)true));
                    for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                        if (onlinePlayer.hasPermission("trustessentials.vanish.see") || onlinePlayer == player) continue;
                        onlinePlayer.hidePlayer(player);
                    }
                } else {
                    player.removeMetadata("trustessentials_vanished", Bukkit.getPluginManager().getPlugin("TrustEssentials"));
                    for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                        if (onlinePlayer == player) continue;
                        onlinePlayer.showPlayer(player);
                    }
                }
                if (message) {
                    Config lang = EssentialsApi.getInstance().getLang();
                    MessagesUtils.sendMessages((CommandSender)player, bool ? lang.getStringList("VANISH_ON", new String[0]) : lang.getStringList("VANISH_OFF", new String[0]));
                }
            }
        }
    }

    @Override
    public void setTpToggle(boolean bool) {
        UserTpToggleChangeEvent event = new UserTpToggleChangeEvent(this, bool);
        BukkitUtils.callEvent(event, EssentialsApi.getInstance());
        if (!event.isCancelled()) {
            if (this.tpToggle != bool) {
                this.saved = true;
            }
            this.tpToggle = bool;
        }
    }

    @Override
    public void setMsgToggle(boolean msgToggle) {
        UserMsgToggleChangeEvent event = new UserMsgToggleChangeEvent(this, msgToggle);
        BukkitUtils.callEvent(event, EssentialsApi.getInstance());
        if (!event.isCancelled()) {
            if (this.msgToggle != msgToggle) {
                this.saved = true;
            }
            this.msgToggle = msgToggle;
        }
    }

    @Override
    public void setLastLocation(Location location) {
        UserLastLocationChangeEvent userLastLocationChangeEvent = new UserLastLocationChangeEvent((User)this, location);
        BukkitUtils.callEvent(userLastLocationChangeEvent, EssentialsApi.getInstance());
        if (!userLastLocationChangeEvent.isCancelled()) {
            this.saved = true;
            this.lastLocation = location;
        }
    }

    @Override
    public void setGameMode(GameMode gameMode) {
        Player player = this.getPlayer();
        if (player != null && player.isOnline()) {
            UserChangeGamemodeEvent event = new UserChangeGamemodeEvent((User)this, gameMode);
            BukkitUtils.callEvent(event, EssentialsApi.getInstance());
            if (!event.isCancelled()) {
                player.setGameMode(event.getGameMode());
            }
        }
    }

    @Override
    public void remove() {
        MANAGER.removeUser(this);
    }

    public void save() {
        if (this.saved) {
            EssentialsData.saveData(this.getUsername(), "fly", String.valueOf(this.fly ? 1 : 0));
            EssentialsData.saveData(this.getUsername(), "fake_name", this.fakeName == null ? "" : this.fakeName);
            EssentialsData.saveData(this.getUsername(), "god", String.valueOf(this.god ? 1 : 0));
            EssentialsData.saveData(this.getUsername(), "tpToggle", String.valueOf(this.tpToggle ? 1 : 0));
            EssentialsData.saveData(this.getUsername(), "msgToggle", String.valueOf(this.msgToggle ? 1 : 0));
            EssentialsData.saveData(this.getUsername(), "bossbarToggle", String.valueOf(this.bossbarToggle ? 1 : 0));
            EssentialsData.saveData(this.getUsername(), "chatToggle", String.valueOf(this.chatToggle ? 1 : 0));
            if (this.lastLocation != null) {
                EssentialsData.saveData(this.getUsername(), "lastLocation", LocationUtils.stringFromLocation(this.lastLocation, true));
            }
        }
    }

    public void addWaitTeleport(String location, String message) {
        int teleportingTime = EssentialsApi.getInstance().getSettings().getInt("teleportingTime");
        if (teleportingTime > 0) {
            this.waitTeleport.put(location + "[|]" + (message == null ? " " : message), System.currentTimeMillis() + (long)teleportingTime);
            for (String line : EssentialsApi.getInstance().getLang().getStringList("TELEPORTING_WARN", new String[0])) {
                this.getPlayer().spigot().sendMessage(new ComponentBuilder(TextComponent.fromLegacyText((String)line)).event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("\u041d\u0430\u0436\u043c\u0438\u0442\u0435, \u0447\u0442\u043e\u0431\u044b \u043e\u0442\u043c\u0435\u043d\u0438\u0442\u044c").color(ChatColor.GRAY).create())).event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/teleportcancel " + location)).create());
            }
        } else if (this.teleport(LocationUtils.stringToLocation(location, true))) {
            this.getPlayer().playSound(this.getPlayer().getEyeLocation(), BukkitUtils.getSoundOfName("ENTITY_ENDEREYE_DEATH") == null ? BukkitUtils.getSoundOfName("ENTITY_ENDER_EYE_DEATH") : BukkitUtils.getSoundOfName("ENTITY_ENDEREYE_DEATH"), 1.0f, 1.0f);
        }
    }

    public String getWaitTeleportationKeyOfLocation(String location) {
        for (Map.Entry<String, Long> stringLongEntry : this.getWaitTeleport().entrySet()) {
            String loc = stringLongEntry.getKey().split("\\[\\|]")[0];
            if (!loc.equals(location)) continue;
            return stringLongEntry.getKey();
        }
        return null;
    }

    public void addWaitTeleport(String location) {
        this.addWaitTeleport(location, null);
    }

    public void removeWaitTeleport(String key) {
        this.waitTeleport.put(key, System.currentTimeMillis() + 2000000L);
    }

    public Map<String, Long> getWaitTeleport() {
        return this.waitTeleport;
    }

    public boolean addCallRequest(Player player) {
        long time = System.currentTimeMillis();
        String name = player.getName();
        if (this.callReguests.containsKey(name) && this.callReguests.get(name) + 120000L > System.currentTimeMillis()) {
            return false;
        }
        this.callReguests.put(name, time);
        return true;
    }

    @Override
    public Map<String, Long> getCallRequests() {
        return this.callReguests;
    }
}
