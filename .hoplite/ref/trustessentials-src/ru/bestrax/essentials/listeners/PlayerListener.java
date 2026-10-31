/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  me.clip.placeholderapi.PlaceholderAPI
 *  net.md_5.bungee.api.ChatColor
 *  net.md_5.bungee.api.ChatMessageType
 *  net.md_5.bungee.api.chat.BaseComponent
 *  net.md_5.bungee.api.chat.ClickEvent
 *  net.md_5.bungee.api.chat.ClickEvent$Action
 *  net.md_5.bungee.api.chat.ComponentBuilder
 *  net.md_5.bungee.api.chat.HoverEvent
 *  net.md_5.bungee.api.chat.HoverEvent$Action
 *  net.md_5.bungee.api.chat.TextComponent
 *  org.bukkit.Bukkit
 *  org.bukkit.ChatColor
 *  org.bukkit.GameMode
 *  org.bukkit.World
 *  org.bukkit.command.CommandSender
 *  org.bukkit.configuration.ConfigurationSection
 *  org.bukkit.entity.Entity
 *  org.bukkit.entity.LivingEntity
 *  org.bukkit.entity.Player
 *  org.bukkit.entity.Projectile
 *  org.bukkit.event.EventHandler
 *  org.bukkit.event.EventPriority
 *  org.bukkit.event.Listener
 *  org.bukkit.event.block.Action
 *  org.bukkit.event.block.BlockBreakEvent
 *  org.bukkit.event.block.BlockExplodeEvent
 *  org.bukkit.event.block.BlockIgniteEvent
 *  org.bukkit.event.block.BlockPlaceEvent
 *  org.bukkit.event.block.SignChangeEvent
 *  org.bukkit.event.entity.EntityDamageByEntityEvent
 *  org.bukkit.event.entity.EntityDamageEvent
 *  org.bukkit.event.entity.EntityDeathEvent
 *  org.bukkit.event.entity.EntityExplodeEvent
 *  org.bukkit.event.entity.FoodLevelChangeEvent
 *  org.bukkit.event.entity.PlayerDeathEvent
 *  org.bukkit.event.inventory.InventoryClickEvent
 *  org.bukkit.event.inventory.InventoryType
 *  org.bukkit.event.player.AsyncPlayerChatEvent
 *  org.bukkit.event.player.PlayerCommandPreprocessEvent
 *  org.bukkit.event.player.PlayerDropItemEvent
 *  org.bukkit.event.player.PlayerGameModeChangeEvent
 *  org.bukkit.event.player.PlayerInteractAtEntityEvent
 *  org.bukkit.event.player.PlayerInteractEvent
 *  org.bukkit.event.player.PlayerJoinEvent
 *  org.bukkit.event.player.PlayerKickEvent
 *  org.bukkit.event.player.PlayerPickupItemEvent
 *  org.bukkit.event.player.PlayerQuitEvent
 *  org.bukkit.event.player.PlayerRespawnEvent
 *  org.bukkit.event.server.TabCompleteEvent
 *  org.bukkit.event.weather.WeatherChangeEvent
 *  org.bukkit.event.world.PortalCreateEvent
 *  org.bukkit.inventory.Inventory
 *  org.bukkit.plugin.Plugin
 *  org.spigotmc.event.player.PlayerSpawnLocationEvent
 */
package ru.bestrax.essentials.listeners;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import me.clip.placeholderapi.PlaceholderAPI;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.apache.commons.lang3.text.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.server.TabCompleteEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.event.world.PortalCreateEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.Plugin;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;
import ru.bestrax.essentials.api.EssentialsApi;
import ru.bestrax.essentials.api.User;
import ru.bestrax.essentials.api.config.Config;
import ru.bestrax.essentials.api.cooldown.Cooldowns;
import ru.bestrax.essentials.api.manager.MoneyKillerManager;
import ru.bestrax.essentials.api.manager.SpectateManager;
import ru.bestrax.essentials.executors.SpectateExecutor;
import ru.bestrax.essentials.utils.BukkitUtils;
import ru.bestrax.essentials.utils.MessagesUtils;
import ru.bestrax.essentials.utils.NMSUtils;
import ru.bestrax.essentials.utils.TeleportingUtils;
import ru.bestrax.essentials.utils.TimeUtils;

public class PlayerListener
implements Listener {
    public static Map<String, Long> gods;

    public PlayerListener() {
        gods = new ConcurrentHashMap<String, Long>();
        Bukkit.getScheduler().runTaskTimer((Plugin)EssentialsApi.getInstance(), () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                Integer voidLevel = this.getVoidLevel(player);
                if (voidLevel == null || player.getLocation().getBlockY() >= voidLevel) continue;
                TeleportingUtils.teleportRandom(player, Bukkit.getWorld((String)EssentialsApi.getInstance().getSettings().getString("rtpWorld", new String[0])), EssentialsApi.getInstance().getSettings().getInt("rtpSize"), true);
            }
        }, 20L, 20L);
    }

    @EventHandler
    public void onDamageEntity(EntityDamageEvent e) {
        long god = gods.getOrDefault(e.getEntity().getName(), 0L);
        if (System.currentTimeMillis() < god) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPvp(EntityDamageByEntityEvent e) {
        long god;
        if (e.getDamager() != null && (god = gods.getOrDefault(e.getDamager().getName(), 0L).longValue()) > 0L) {
            gods.remove(e.getDamager().getName());
        }
    }

    @EventHandler
    public void PlayerItemDropEvent(PlayerDropItemEvent e) {
        if (EssentialsApi.getInstance().getSettings().getBoolean("noDrop")) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageByEntityEvent e) {
        if (EssentialsApi.getInstance().getSettings().getBoolean("hpViewer") && e.getDamager() instanceof Player && e.getEntity() instanceof LivingEntity && (e.getEntity() instanceof Player || !EssentialsApi.getInstance().getSettings().getBoolean("hpOnlyPlayer"))) {
            Player player = (Player)e.getDamager();
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, (BaseComponent)new TextComponent(this.getOutput(((LivingEntity)e.getEntity()).getHealth() - e.getFinalDamage(), EssentialsApi.getInstance().getSettings().getString("hpViewerOutput", new String[0]), player, (LivingEntity)e.getEntity())));
        }
    }

    public String getOutput(double health, String output, Player receiver, LivingEntity entity) {
        String filledHeartIcon = EssentialsApi.getInstance().getSettings().getString("filledHpIcon", new String[0]);
        String halfHeartIcon = EssentialsApi.getInstance().getSettings().getString("halfHpIcon", new String[0]);
        String emptyHeartIcon = EssentialsApi.getInstance().getSettings().getString("emptyHpIcon", new String[0]);
        double maxHealth = entity.getMaxHealth();
        if (health < 0.0 || entity.isDead()) {
            health = 0.0;
        }
        String name = this.getName(entity);
        if (entity instanceof Player) {
            Player player = (Player)entity;
            String displayName = player.getDisplayName() == null ? name : player.getDisplayName();
            output = this.replacePlaceholders(output, "displayname", displayName);
            if (Bukkit.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
                output = this.replacePlaceholders(output, "ATTACKEDPLAYER_", "");
                output = PlaceholderAPI.setPlaceholders((Player)player, (String)output);
            }
        } else {
            output = this.replacePlaceholders(output, "displayname", name);
        }
        if (Bukkit.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            output = ru.bestrax.essentials.api.PlaceholderAPI.replacePlaceHolder(receiver, output);
        }
        output = this.replacePlaceholders(output, "name", name);
        output = this.replacePlaceholders(output, "health", String.valueOf((int)health));
        output = this.replacePlaceholders(output, "maxhealth", String.valueOf((int)maxHealth));
        output = this.replacePlaceholders(output, "percenthealth", String.valueOf((int)(health / maxHealth * 100.0)));
        if ((output = this.replacePlaceholders(output, "opponentlastdamage", String.valueOf((int)entity.getLastDamage()))).contains("usestyle")) {
            StringBuilder style = new StringBuilder();
            int left = this.getLimitHealth(maxHealth);
            double heart = maxHealth / (double)this.getLimitHealth(maxHealth);
            double halfHeart = heart / 2.0;
            double tempHealth = health;
            if (maxHealth != health && health >= 0.0 && !entity.isDead()) {
                for (int i = 0; i < this.getLimitHealth(maxHealth) && tempHealth - heart > 0.0; ++i) {
                    tempHealth -= heart;
                    style.append(filledHeartIcon);
                    --left;
                }
                if (tempHealth > halfHeart) {
                    style.append(filledHeartIcon);
                    --left;
                } else if (tempHealth > 0.0 && tempHealth <= halfHeart) {
                    style.append(halfHeartIcon);
                    --left;
                }
            }
            if (maxHealth != health) {
                for (int i = 0; i < left; ++i) {
                    style.append(emptyHeartIcon);
                }
            } else {
                for (int i = 0; i < left; ++i) {
                    style.append(filledHeartIcon);
                }
            }
            output = this.replacePlaceholders(output, "usestyle", style.toString());
        }
        return output;
    }

    public int getLimitHealth(double maxHealth) {
        int health = (int)maxHealth / 2;
        return health >= 10 ? 10 : health;
    }

    public String getName(LivingEntity entity) {
        String name = entity.getCustomName() != null ? entity.getCustomName() : this.getNameReflection(entity);
        return name;
    }

    public String replacePlaceholders(String s, String key, String value) {
        return s.replace("%" + key + "%", value).replace("%ah" + key + "%", value);
    }

    private String getNameReflection(LivingEntity entity) {
        String name;
        Method getName = null;
        try {
            if (entity.getCustomName() == null) {
                getName = entity.getClass().getMethod("getName", null);
            }
        }
        catch (NoSuchMethodException | SecurityException exception) {
            // empty catch block
        }
        if (getName != null) {
            try {
                name = (String)getName.invoke((Object)entity, (Object[])null);
            }
            catch (IllegalAccessException | InvocationTargetException var4) {
                name = this.capitalizeFully(entity.getType().name().replace("_", ""));
            }
        } else {
            name = this.capitalizeFully(entity.getType().name().replace("_", ""));
        }
        return name;
    }

    private String capitalizeFully(String words) {
        words = words.toLowerCase();
        return WordUtils.capitalizeFully(words);
    }

    @EventHandler
    public void onInventoryClickEvent(InventoryClickEvent e) {
        if (EssentialsApi.getInstance().getSettings().getBoolean("noInventoryClick")) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerCommandPreProcessEvent(PlayerCommandPreprocessEvent e) {
        String command = e.getMessage().split(" ")[0].replaceFirst("/", "").toLowerCase();
        if ((command.equalsIgnoreCase("spectate") || command.equalsIgnoreCase("spec") || command.equalsIgnoreCase("spectation")) && e.getPlayer().hasPermission("trustessentials.spectation")) {
            e.setCancelled(true);
            String[] argsSplit = e.getMessage().split(" ");
            ArrayList<String> argsList = new ArrayList<String>();
            for (int i = 0; i < argsSplit.length; ++i) {
                if (i == 0) continue;
                argsList.add(argsSplit[i]);
            }
            SpectateExecutor.executeCmd((CommandSender)e.getPlayer(), argsList.toArray(new String[0]));
        }
        if (EssentialsApi.getInstance().getSettings().getBoolean("noCommands") && !EssentialsApi.getInstance().getSettings().getStringList("whiteListCommands", new String[0]).contains(command)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityDamageEvent(EntityDamageEvent event) {
        if (EssentialsApi.getInstance().getSettings().getBoolean("noDamage")) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onFoodLevelChangeEvent(FoodLevelChangeEvent event) {
        if (EssentialsApi.getInstance().getSettings().getBoolean("noHunger")) {
            if (event.getFoodLevel() < 20) {
                event.setFoodLevel(20);
            } else {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onBlockBreakEvent(BlockBreakEvent event) {
        if (EssentialsApi.getInstance().getSettings().getBoolean("noBreak")) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPlaceEvent(BlockPlaceEvent event) {
        if (EssentialsApi.getInstance().getSettings().getBoolean("noPlace")) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onWeatherChangeEvent(WeatherChangeEvent event) {
        if (EssentialsApi.getInstance().getSettings().getBoolean("noWeather") && event.toWeatherState()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockExplodeEvent(BlockExplodeEvent event) {
        if (EssentialsApi.getInstance().getSettings().getBoolean("noExplosion")) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityExplodeEvent(EntityExplodeEvent event) {
        if (EssentialsApi.getInstance().getSettings().getBoolean("noExplosion")) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockIgniteEvent(BlockIgniteEvent event) {
        if (EssentialsApi.getInstance().getSettings().getBoolean("noFire")) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerInteractEvent(PlayerInteractEvent event) {
        if (EssentialsApi.getInstance().getSettings().getBoolean("noWheat") && event.getAction() == Action.PHYSICAL && event.getClickedBlock().getType() == BukkitUtils.getMaterial("SOIL")) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerKickEvent(PlayerKickEvent event) {
        if (EssentialsApi.getInstance().getSettings().getBoolean("noMessages")) {
            event.setLeaveMessage("");
        }
    }

    @EventHandler
    public void onPlayerDeathEvent(PlayerDeathEvent event) {
        if (EssentialsApi.getInstance().getSettings().getBoolean("noMessages")) {
            event.setDeathMessage("");
        }
    }

    @EventHandler
    public void onPlayerJoinEvent(PlayerJoinEvent event) {
        if (EssentialsApi.getInstance().getSettings().getBoolean("noMessages")) {
            event.setJoinMessage("");
        }
        if (EssentialsApi.getInstance().getSettings().getBoolean("hidePlayers")) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.hidePlayer(event.getPlayer());
                event.getPlayer().hidePlayer(player);
            }
        }
    }

    @EventHandler
    public void onPlayerQuitEvent(PlayerQuitEvent event) {
        if (EssentialsApi.getInstance().getSettings().getBoolean("noMessages")) {
            event.setQuitMessage("");
        }
    }

    @EventHandler
    public void onAsyncPlayerChatEvent(AsyncPlayerChatEvent event) {
        if (EssentialsApi.getInstance().getSettings().getBoolean("noChat")) {
            event.setCancelled(true);
        }
    }

    private List<String> getBlockCommands() {
        final Config settings = EssentialsApi.getInstance().getSettings();
        return new ArrayList<String>(this){
            final /* synthetic */ PlayerListener this$0;
            {
                this.this$0 = this$0;
                if (settings.getBoolean("helpCommand")) {
                    this.add("/?");
                }
                if (settings.getBoolean("helpCommand")) {
                    this.add("/help");
                }
                if (settings.getBoolean("pluginsCommand")) {
                    this.add("/pl");
                }
                if (settings.getBoolean("pluginsCommand")) {
                    this.add("/plugins");
                }
            }
        };
    }

    @EventHandler
    public void onTab(TabCompleteEvent e) {
        String command;
        List<String> block = this.getBlockCommands();
        if (block.contains(command = e.getBuffer().split(" ")[0].toLowerCase())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockCommands(PlayerCommandPreprocessEvent e) {
        Player player = e.getPlayer();
        List<String> block = this.getBlockCommands();
        String command = e.getMessage().split(" ")[0].toLowerCase();
        if (EssentialsApi.getInstance().getSettings().getBoolean("helpPlace") && (command.equalsIgnoreCase("/help") || command.equalsIgnoreCase("/about") || command.equalsIgnoreCase("/?") || command.equalsIgnoreCase("/version") || command.equalsIgnoreCase("/ver"))) {
            e.setCancelled(true);
            String helpCommand = EssentialsApi.getInstance().getSettings().getString("helpPlaceCommand", new String[0]);
            if (helpCommand.startsWith("console:")) {
                Bukkit.dispatchCommand((CommandSender)Bukkit.getConsoleSender(), (String)helpCommand.split("console:")[1].replace("%player%", player.getName()));
            } else {
                player.chat(helpCommand);
            }
        }
        if (block.contains(command)) {
            e.setCancelled(true);
            Config lang = EssentialsApi.getInstance().getLang();
            if (!command.equalsIgnoreCase("/?") && !command.equalsIgnoreCase("/help")) {
                if (command.equalsIgnoreCase("/pl") || command.equalsIgnoreCase("/plugins")) {
                    MessagesUtils.sendMessages((CommandSender)player, lang.getStringList("PLUGINS", new String[0]));
                }
            } else {
                MessagesUtils.sendMessages((CommandSender)player, lang.getStringList("HELP", new String[0]));
            }
        }
    }

    @EventHandler
    public void onKillEntity(EntityDeathEvent e) {
        MoneyKillerManager killerManager = EssentialsApi.getMoneyKillerManager();
        if (e.getEntity() != null && e.getEntity().getKiller() != null) {
            if (e.getEntity() instanceof Player) {
                killerManager.killPlayer(e.getEntity().getKiller(), (Player)e.getEntity());
            } else {
                killerManager.killEntity(e.getEntity().getKiller(), (Entity)e.getEntity());
            }
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (this.sendBackBlock(e.getPlayer())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent e) {
        Player player;
        if (e.getDamager() instanceof Player && this.sendBackBlock(player = (Player)e.getDamager())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onInteractEntity(PlayerInteractAtEntityEvent e) {
        if (this.sendBackBlock(e.getPlayer())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onTake(PlayerPickupItemEvent e) {
        if (this.sendBackBlock(e.getPlayer())) {
            e.setCancelled(true);
        }
    }

    public boolean sendBackBlock(Player player) {
        User user = EssentialsApi.getUserManager().getUser(player);
        if (user.getBackTime() > System.currentTimeMillis()) {
            if (!Cooldowns.has(player.getName(), "take")) {
                player.sendMessage(EssentialsApi.getInstance().getLang().getString("BACK_BLOCK", "%cooldown%", TimeUtils.getTimeInSeconds((user.getBackTime() - System.currentTimeMillis()) / 1000L)));
                Cooldowns.set(player.getName(), "take", 1000.0);
            }
            return true;
        }
        return false;
    }

    @EventHandler
    public void onGamemodeChangeEvent(PlayerGameModeChangeEvent e) {
        if (e.getNewGameMode() == GameMode.SURVIVAL) {
            User user = EssentialsApi.getUserManager().getUser(e.getPlayer());
            if (user == null) {
                return;
            }
            user.setFly(false, false);
            user.setGod(false, false);
        } else if (e.getNewGameMode() == GameMode.CREATIVE) {
            User user = EssentialsApi.getUserManager().getUser(e.getPlayer());
            if (user == null) {
                return;
            }
            user.setFly(true, false);
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        User user = EssentialsApi.getUserManager().getUser(player);
        Config settings = EssentialsApi.getInstance().getSettings();
        Config lang = EssentialsApi.getInstance().getLang();
        if (user == null) {
            player.kickPlayer(org.bukkit.ChatColor.RED + "\u041f\u0440\u043e\u0438\u0437\u043e\u0448\u043b\u0430 \u0441\u0438\u0441\u0442\u0435\u043c\u043d\u0430\u044f \u043e\u0448\u0438\u0431\u043a\u0430. \u041f\u043e\u043f\u0440\u043e\u0431\u0443\u0439\u0442\u0435 \u043f\u0435\u0440\u0435\u0437\u0430\u0439\u0442\u0438 \u0438\u043b\u0438 \u0441\u0432\u044f\u0436\u0438\u0442\u0435\u0441\u044c \u0441 \u0430\u0434\u043c\u0438\u043d\u0438\u0441\u0442\u0440\u0430\u0446\u0438\u0435\u0439");
        } else {
            for (String maxOnline : settings.getFile().getConfigurationSection("maxOnline").getKeys(false)) {
                ConfigurationSection online = settings.getFile().getConfigurationSection("maxOnline." + maxOnline);
                if (Bukkit.getOnlinePlayers().size() <= online.getInt("online") || player.hasPermission(online.getString("permission"))) continue;
                player.kickPlayer(org.bukkit.ChatColor.translateAlternateColorCodes((char)'&', (String)online.getString("kickMessage")));
                return;
            }
            for (String joinMsg : lang.getStringList("JOIN_MSG", "%online%", String.valueOf(Bukkit.getOnlinePlayers().size() + (EssentialsApi.getBotManager() != null ? EssentialsApi.getBotManager().getUsedAccounts().size() : 0)))) {
                if (joinMsg.startsWith("json:")) {
                    NMSUtils.sendMessage(player, joinMsg.replace("json:", ""));
                    continue;
                }
                ComponentBuilder componentBuilder = new ComponentBuilder("");
                String[] replaced = joinMsg.split("%");
                if (replaced.length <= 1) {
                    player.sendMessage(org.bukkit.ChatColor.translateAlternateColorCodes((char)'&', (String)joinMsg));
                    continue;
                }
                for (int i = 0; i < replaced.length; i += 2) {
                    String wordReplace = replaced[i];
                    if (replaced.length <= i + 1) {
                        componentBuilder.append(org.bukkit.ChatColor.translateAlternateColorCodes((char)'&', (String)wordReplace)).event((ClickEvent)null).event((HoverEvent)null).create();
                        break;
                    }
                    String word = replaced[i + 1];
                    if (word.equalsIgnoreCase("menu")) {
                        componentBuilder.append(wordReplace).append(lang.getString("MENU_MESSAGE", new String[0])).event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, lang.getString("MENU_COMMAND", new String[0]))).event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(lang.getString("MENU_HOVER", new String[0])).color(ChatColor.GRAY).create())).create();
                        continue;
                    }
                    if (word.equalsIgnoreCase("site")) {
                        componentBuilder.append(wordReplace).append(lang.getString("SITE_MESSAGE", new String[0])).event(new ClickEvent(ClickEvent.Action.OPEN_URL, lang.getString("SITE_LINK", new String[0]))).event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(lang.getString("SITE_HOVER", new String[0])).color(ChatColor.GRAY).create())).create();
                        continue;
                    }
                    if (word.equalsIgnoreCase("group")) {
                        componentBuilder.append(wordReplace).append(lang.getString("GROUP_MESSAGE", new String[0])).event(new ClickEvent(ClickEvent.Action.OPEN_URL, lang.getString("GROUP_LINK", new String[0]))).event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(lang.getString("GROUP_HOVER", new String[0])).color(ChatColor.GRAY).create())).create();
                        continue;
                    }
                    componentBuilder.append(org.bukkit.ChatColor.translateAlternateColorCodes((char)'&', (String)(wordReplace + word))).event((ClickEvent)null).event((HoverEvent)null).create();
                }
                player.spigot().sendMessage(componentBuilder.create());
            }
            if (!player.hasPlayedBefore()) {
                MessagesUtils.sendMessagesAll(lang.getStringList("NEWPLAYER", "%name%", player.getName(), "%count%", String.valueOf(Bukkit.getServer().getOfflinePlayers().length)));
            }
            if (user.isBossbarToggle()) {
                EssentialsApi.getBossBarManager().add(player);
            } else {
                EssentialsApi.getBossBarManager().remove(player);
            }
            player.setAllowFlight(user.isFly());
            player.setFlying(user.isFly());
            if (user.isFly() || user.isElytraFly() && settings.getBoolean("spawnFlyOff")) {
                player.setAllowFlight(false);
                player.setFlying(false);
            }
            if (player.getGameMode() != GameMode.SURVIVAL && settings.getBoolean("survivalOnSpawn")) {
                player.setGameMode(GameMode.SURVIVAL);
            }
            if (!player.hasPermission("trustessentials.gamemode") && player.getGameMode() == GameMode.CREATIVE) {
                player.setGameMode(GameMode.SURVIVAL);
            }
            if (player.getGameMode() == GameMode.CREATIVE) {
                user.setFly(true, false);
            }
            if (user.getFakeName() != null && !user.getFakeName().equalsIgnoreCase("")) {
                BukkitUtils.runTaskLater(5L, () -> user.setFakeName(user.getFakeName(), false), EssentialsApi.getInstance());
            }
        }
    }

    @EventHandler
    public void onSetSpawn(PlayerSpawnLocationEvent e) {
        Player player = e.getPlayer();
    }

    @EventHandler
    public void onPortalCreate(PortalCreateEvent e) {
        Config settings = EssentialsApi.getInstance().getSettings();
        e.setCancelled(!settings.getBoolean("portalCreate"));
    }

    @EventHandler
    public void onFood(FoodLevelChangeEvent e) {
        User user;
        if (e.getEntity() instanceof Player && (user = EssentialsApi.getUserManager().getUserUnsafe((Player)e.getEntity())) != null && user.isGod()) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        Player player;
        Inventory top = e.getView().getTopInventory();
        InventoryType type = top.getType();
        if (type == InventoryType.WORKBENCH && (player = (Player)e.getWhoClicked()).getGameMode() == GameMode.CREATIVE && !player.hasPermission("trustessentials.gamemode.inventory.bypass")) {
            e.setCancelled(true);
        }
    }

    private Integer getVoidLevel(Player player) {
        Config settings = EssentialsApi.getInstance().getSettings();
        Integer voidLevel = settings.getInt("voidLevel");
        String stringWorld = settings.getString("voidWorld", new String[0]);
        if (stringWorld == null) {
            return null;
        }
        World world = Bukkit.getWorld((String)stringWorld);
        if (world == null) {
            return null;
        }
        return !player.getWorld().getName().equalsIgnoreCase(world.getName()) ? null : voidLevel;
    }

    @EventHandler
    public void onSignColor(SignChangeEvent e) {
        for (int i = 0; i <= 3; ++i) {
            String line = e.getLine(i);
            line = org.bukkit.ChatColor.translateAlternateColorCodes((char)'&', (String)line);
            e.setLine(i, line);
        }
    }

    @EventHandler(priority=EventPriority.MONITOR)
    public void onDamage(EntityDamageEvent e) {
        Entity entity = e.getEntity();
        if (entity instanceof Player) {
            Player player = (Player)entity;
            User user = EssentialsApi.getUserManager().getUserUnsafe(player);
            if (user != null && user.isGod()) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        e.setDeathMessage(null);
        Player player = e.getEntity();
        User user = EssentialsApi.getUserManager().getUser(player);
        if (user != null) {
            user.setLastLocation(player.getLocation());
        }
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {
        e.setRespawnLocation(EssentialsApi.getSpawn());
    }

    private boolean isFlying(User user) {
        return user.isElytraFly() || user.isFly();
    }

    private void sendAntiItemPuckupMessage(Player player) {
        if (!Cooldowns.has(player.getName(), "pickup")) {
            Cooldowns.set(player.getName(), "pickup", 2000.0);
            player.sendMessage(EssentialsApi.getInstance().getLang().getString("PUCKUPITEM", new String[0]));
        }
    }

    @EventHandler
    public void onPickup(PlayerPickupItemEvent e) {
        User user = EssentialsApi.getUserManager().getUser(e.getPlayer());
        if (user.isVanished() && !e.getPlayer().hasPermission("trustessentials.pickupitem.vanish.bypass")) {
            this.sendAntiItemPuckupMessage(e.getPlayer());
            e.setCancelled(true);
        } else if ((user.isFly() || user.isElytraFly()) && !e.getPlayer().hasPermission("trustessentials.pickupitem.fly.bypass")) {
            this.sendAntiItemPuckupMessage(e.getPlayer());
            e.setCancelled(true);
        } else if (user.isGod() && !e.getPlayer().hasPermission("trustessentials.pickupitem.god.bypass")) {
            this.sendAntiItemPuckupMessage(e.getPlayer());
            e.setCancelled(true);
        }
    }

    @EventHandler(priority=EventPriority.MONITOR)
    public void onDamageByEntity(EntityDamageByEntityEvent e) {
        if (!e.isCancelled()) {
            Projectile projectile;
            User target;
            User user;
            Entity damager = e.getDamager();
            Entity entity = e.getEntity();
            if (damager instanceof Player && entity instanceof Player) {
                if (entity.getName().equals(damager.getName())) {
                    return;
                }
                Player player = (Player)damager;
                user = EssentialsApi.getUserManager().getUser(player);
                target = EssentialsApi.getUserManager().getUser((Player)entity);
                if (player.getGameMode() == GameMode.CREATIVE && user.isGod()) {
                    player.setGameMode(GameMode.SURVIVAL);
                    user.setGod(false, false);
                }
                if (user != null && user.isGod() && entity instanceof Player) {
                    if (player.getGameMode() == GameMode.CREATIVE) {
                        return;
                    }
                    e.setCancelled(true);
                } else if (user != null && entity instanceof Player) {
                    if (player.getGameMode() == GameMode.CREATIVE) {
                        return;
                    }
                    if (target.isGod()) {
                        return;
                    }
                    if (user.isFly()) {
                        user.setFly(false, false);
                    }
                    if (user.isElytraFly()) {
                        user.setElytraFly(false, false);
                    }
                    if (target.isFly()) {
                        target.setFly(false, false);
                    }
                    if (target.isElytraFly()) {
                        target.setElytraFly(false, false);
                    }
                }
            }
            if (damager instanceof Projectile && (projectile = (Projectile)damager).getShooter() instanceof Player) {
                Player damagerPlayer = (Player)projectile.getShooter();
                user = EssentialsApi.getUserManager().getUser(damagerPlayer.getName());
                if (damagerPlayer.getGameMode() == GameMode.CREATIVE && user.isGod()) {
                    damagerPlayer.setGameMode(GameMode.SURVIVAL);
                    user.setGod(false, false);
                }
                if (user != null && user.isGod() && entity instanceof Player) {
                    if (damagerPlayer.getGameMode() == GameMode.CREATIVE) {
                        return;
                    }
                    e.setCancelled(true);
                } else if (user != null && entity instanceof Player) {
                    if (damagerPlayer.getGameMode() == GameMode.CREATIVE) {
                        return;
                    }
                    target = EssentialsApi.getUserManager().getUser((Player)entity);
                    if (target.isGod()) {
                        return;
                    }
                    if (target.isFly()) {
                        target.setFly(false, false);
                    }
                    if (target.isElytraFly()) {
                        target.setElytraFly(false, false);
                    }
                    if (target.isFly()) {
                        target.setFly(false, false);
                    }
                    if (target.isElytraFly()) {
                        target.setElytraFly(false, false);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        Player player = e.getPlayer();
        player.setWalkSpeed(0.2f);
        player.setFlySpeed(0.1f);
        SpectateManager spectateManager = EssentialsApi.getSpectateManager();
        if (spectateManager.isSpectation(player)) {
            spectateManager.stop(player);
        }
    }
}
