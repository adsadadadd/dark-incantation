/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Material
 *  org.bukkit.command.CommandSender
 *  org.bukkit.entity.Player
 *  org.bukkit.inventory.ItemStack
 */
package ru.bestrax.essentials.executors;

import me.emine.anvil.Anvil;
import me.emine.anvil.AnvilGUI;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ru.bestrax.essentials.api.EssentialsApi;
import ru.bestrax.essentials.api.commands.TrustCommand;
import ru.bestrax.essentials.api.provider.ContainerProvider;

public class AnvilExecutor
extends TrustCommand {
    public AnvilExecutor() {
        super("anvil", "\u043d\u0430\u043a\u043e\u0432\u0430\u043b\u044c\u043d\u044f", "anvils");
        this.setPermission("trustessentials.anvil");
        this.setPlayersOnly(true);
        this.register();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        ContainerProvider containerProvider = EssentialsApi.getProviderManager().getContainerProvider();
        Player player = (Player)sender;
        if (containerProvider == null) {
            new AnvilGUI.Builder().itemLeft(new ItemStack(Material.AIR)).itemRight(new ItemStack(Material.AIR)).title("\u041d\u0430\u043a\u043e\u0432\u0430\u043b\u044c\u043d\u044f").open(player);
            Anvil.INSTANCE.getOpenPlayers().add(player.getUniqueId());
        } else {
            containerProvider.openAnvil(player);
        }
    }
}
