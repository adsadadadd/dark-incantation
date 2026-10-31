package me.x1no4ekk.core.gameplay.command;

import me.x1no4ekk.core.CorePlugin;
import me.x1no4ekk.core.command.BaseCommand;
import me.x1no4ekk.core.gameplay.Cooldowns;
import me.x1no4ekk.core.gameplay.GameplayModule;
import me.x1no4ekk.core.gameplay.GameplaySettings;
import me.x1no4ekk.core.util.Durations;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

/**
 * /firework [power] — launches a salvo of random fireworks around the player.
 */
public final class FireworkCommand extends BaseCommand {

    private static final String BYPASS_COOLDOWN = "core.firework.bypass-cooldown";
    private static final FireworkEffect.Type[] TYPES = FireworkEffect.Type.values();

    private final GameplayModule module;

    public FireworkCommand(CorePlugin plugin, GameplayModule module) {
        super(plugin);
        this.module = module;
    }

    @Override
    protected void execute(CommandSender sender, String label, String[] args) {
        Player player = playerOnly(sender);
        requirePermission(sender, "core.firework");
        if (args.length > 1) {
            throw usage("gameplay.firework.usage");
        }
        GameplaySettings.Firework config = module.settings().firework();
        int power = args.length == 1 ? parseInt(args[0], 0, config.maxPower()) : config.defaultPower();
        Cooldowns cooldowns = module.fireworkCooldowns();
        if (!player.hasPermission(BYPASS_COOLDOWN)) {
            long left = cooldowns.remaining(player.getUniqueId());
            if (left > 0) {
                throw fail("general.cooldown", placeholder("time", Durations.format(left)));
            }
            cooldowns.start(player.getUniqueId(), config.cooldownMillis());
        }

        ThreadLocalRandom random = ThreadLocalRandom.current();
        Location base = player.getLocation();
        for (int i = 0; i < config.count(); i++) {
            Location at = base.clone().add(random.nextDouble(-1.5, 1.5), 0.5, random.nextDouble(-1.5, 1.5));
            player.getWorld().spawn(at, Firework.class, firework -> {
                FireworkMeta meta = firework.getFireworkMeta();
                meta.addEffect(randomEffect(random));
                meta.setPower(power);
                firework.setFireworkMeta(meta);
                if (config.harmless()) {
                    firework.getPersistentDataContainer().set(module.harmlessFireworkKey(), PersistentDataType.BOOLEAN, true);
                }
            });
        }
        send(player, "gameplay.firework.launched", placeholder("amount", config.count()));
    }

    private static FireworkEffect randomEffect(ThreadLocalRandom random) {
        return FireworkEffect.builder()
                .with(TYPES[random.nextInt(TYPES.length)])
                .withColor(randomColors(random))
                .withFade(randomColors(random))
                .flicker(random.nextBoolean())
                .trail(random.nextBoolean())
                .build();
    }

    private static List<Color> randomColors(ThreadLocalRandom random) {
        int count = 1 + random.nextInt(3);
        List<Color> colors = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            colors.add(Color.fromRGB(random.nextInt(0x1000000)));
        }
        return colors;
    }

    @Override
    protected List<String> complete(CommandSender sender, String label, String[] args) {
        if (args.length != 1 || !sender.hasPermission("core.firework")) {
            return List.of();
        }
        return filter(IntStream.rangeClosed(0, module.settings().firework().maxPower()).mapToObj(String::valueOf), args[0]);
    }
}
