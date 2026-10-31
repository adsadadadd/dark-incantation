package me.x1no4ekk.core.reward;

import me.x1no4ekk.core.CorePlugin;
import me.x1no4ekk.core.config.Messages;
import me.x1no4ekk.core.hook.EconomyHook;
import me.x1no4ekk.core.user.User;
import me.x1no4ekk.core.util.Text;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.math.BigDecimal;
import java.util.List;
import java.util.logging.Level;

/**
 * Kill statistics (kills, deaths, mob kills in {@link User}) and kill rewards from killermoney.yml.
 */
final class RewardListener implements Listener {

    static final String BYPASS_LOSS_PERMISSION = "core.reward.bypass-loss";

    private final CorePlugin plugin;
    private final RewardModule module;

    RewardListener(CorePlugin plugin, RewardModule module) {
        this.plugin = plugin;
        this.module = module;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();
        User victimUser = plugin.userManager().getUser(victim);
        if (victimUser != null) {
            victimUser.addDeath();
        }
        if (killer == null || killer.equals(victim)) {
            return;
        }
        User killerUser = plugin.userManager().getUser(killer);
        if (killerUser != null) {
            killerUser.addKill();
        }
        rewardPlayerKill(killer, victim);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity instanceof Player) {
            return; // PlayerDeathEvent
        }
        Player killer = entity.getKiller();
        if (killer == null) {
            return;
        }
        if (entity instanceof Mob) {
            User user = plugin.userManager().getUser(killer);
            if (user != null) {
                user.addMobKill();
            }
        }
        rewardMobKill(killer, entity);
    }

    private void rewardPlayerKill(Player killer, Player victim) {
        RewardSettings settings = module.settings();
        RewardSettings.PlayerReward reward = settings.player();
        if (!settings.enabled() || !reward.enabled() || reward.percent() <= 0 && reward.min() <= 0
                || settings.isDisabledWorld(victim.getWorld().getName())
                || victim.hasPermission(BYPASS_LOSS_PERMISSION)) {
            return;
        }
        EconomyHook economy = plugin.hooks().economy();
        if (!economy.isAvailable()) {
            return;
        }
        double balance = economy.getBalance(victim);
        if (balance <= 0) {
            return;
        }
        double amount = balance * reward.percent() / 100D;
        if (reward.max() > 0) {
            amount = Math.min(amount, reward.max());
        }
        amount = cents(Math.min(Math.max(amount, reward.min()), balance));
        if (amount <= 0 || !economy.withdraw(victim, amount)) {
            return;
        }
        if (!economy.deposit(killer, amount)) {
            economy.deposit(victim, amount);
            plugin.getLogger().warning("[rewards] Could not pay " + amount + " to " + killer.getName() + "; refunded " + victim.getName());
            return;
        }

        Messages messages = plugin.messages();
        TagResolver money = Placeholder.unparsed("amount", economy.format(amount));
        messages.send(killer, "reward.player.killer", money, Placeholder.component("player", victim.displayName()));
        messages.sendActionBar(killer, "reward.player.killer-actionbar", money, Placeholder.component("player", victim.displayName()));
        messages.send(victim, "reward.player.victim", money, Placeholder.component("player", killer.displayName()));
        messages.sendActionBar(victim, "reward.player.victim-actionbar", money, Placeholder.component("player", killer.displayName()));
        reward.killerSound().play(killer);
        reward.victimSound().play(victim);
        runCommands(reward.commands(), killer.getName(), victim.getName(), amount);
    }

    private void rewardMobKill(Player killer, LivingEntity entity) {
        RewardSettings settings = module.settings();
        if (!settings.enabled() || !settings.mobsEnabled() || settings.isDisabledWorld(entity.getWorld().getName())) {
            return;
        }
        RewardSettings.MobReward reward = settings.mobs().get(entity.getType());
        if (reward == null || settings.ignoredSpawnReasons().contains(entity.getEntitySpawnReason())) {
            return;
        }
        EconomyHook economy = plugin.hooks().economy();
        if (!economy.isAvailable()) {
            return;
        }
        double amount = reward.roll();
        if (amount <= 0 || !economy.deposit(killer, amount)) {
            return;
        }
        Messages messages = plugin.messages();
        TagResolver placeholders = TagResolver.resolver(
                Placeholder.unparsed("amount", economy.format(amount)),
                Placeholder.component("mob", Component.translatable(entity.getType())));
        if (reward.message() == null) {
            messages.send(killer, "reward.mob.message", placeholders);
        } else if (!reward.message().isEmpty()) {
            killer.sendMessage(Text.parse(reward.message(), messages.prefixResolver(), placeholders));
        }
        if (reward.actionBar() == null) {
            messages.sendActionBar(killer, "reward.mob.actionbar", placeholders);
        } else if (!reward.actionBar().isEmpty()) {
            killer.sendActionBar(Text.parse(reward.actionBar(), messages.prefixResolver(), placeholders));
        }
        reward.sound().play(killer);
        runCommands(reward.commands(), killer.getName(), entity.getType().getKey().getKey(), amount);
    }

    private void runCommands(List<String> commands, String killer, String target, double amount) {
        String plainAmount = BigDecimal.valueOf(amount).stripTrailingZeros().toPlainString();
        for (String command : commands) {
            String line = command.replace("%killer%", killer).replace("%target%", target).replace("%amount%", plainAmount);
            if (line.startsWith("/")) {
                line = line.substring(1);
            }
            try {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), line);
            } catch (RuntimeException e) {
                plugin.getLogger().log(Level.WARNING, "[rewards] Command failed: " + line, e);
            }
        }
    }

    private static double cents(double value) {
        return Math.floor(value * 100D) / 100D;
    }
}
