/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.inventory.ItemStack
 */
package ru.bestrax.essentials.base.objects;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.bukkit.inventory.ItemStack;
import ru.bestrax.essentials.api.EssentialsApi;
import ru.bestrax.essentials.api.Kit;
import ru.bestrax.essentials.api.manager.KitManager;

public class TrustKit
implements Kit {
    private static final KitManager KIT_MANAGER = EssentialsApi.getKitManager();
    private boolean start;
    private final ItemStack icon;
    private final String name;
    private final String localizedName;
    private final String group;
    private final int guiSlot;
    private final List<String> defaultGroup;
    private int cooldown;
    private boolean canBuy;
    private boolean canGet;
    private final String groupPrefix;
    private boolean autoEquipment;
    private final int price;
    private final List<ItemStack> itemStacks;

    @Override
    public int getPrice() {
        return this.price;
    }

    public TrustKit(String name, String localizedName, Collection<ItemStack> itemStacks, boolean canBuy, boolean canGet, boolean autoEquipment, ItemStack icon, int cooldown, String groupPrefix, int price, int guiSlot) {
        this(name, localizedName, itemStacks, canBuy, canGet, autoEquipment, icon, cooldown, "default", null, groupPrefix, price, guiSlot);
    }

    public TrustKit(String name, String localizedName, Collection<ItemStack> itemStacks, boolean canBuy, boolean canGet, boolean autoEquipment, ItemStack icon, int cooldown, String group, List<String> defaultGroup, String groupPrefix, int price, int guiSlot) {
        this.canBuy = canBuy;
        this.canGet = canGet;
        this.itemStacks = Collections.synchronizedList(new ArrayList());
        this.name = name;
        this.localizedName = localizedName;
        this.autoEquipment = autoEquipment;
        this.group = group;
        this.icon = icon;
        this.cooldown = cooldown;
        this.price = price;
        this.groupPrefix = groupPrefix;
        this.itemStacks.addAll(itemStacks);
        this.start = false;
        this.guiSlot = guiSlot;
        this.defaultGroup = defaultGroup;
    }

    @Override
    public boolean isAutoEquipment() {
        return this.autoEquipment;
    }

    @Override
    public boolean isCanBuy() {
        return this.canBuy;
    }

    @Override
    public boolean isCanGet() {
        return this.canGet;
    }

    @Override
    public List<ItemStack> getItems() {
        return this.itemStacks;
    }

    @Override
    public ItemStack getIcon() {
        return this.icon.clone();
    }

    @Override
    public int getGuiSlot() {
        return this.guiSlot;
    }

    @Override
    public void addItems(Collection<ItemStack> itemStacks) {
        this.itemStacks.addAll(itemStacks);
    }

    @Override
    public String getMinimalGroup() {
        return this.group;
    }

    @Override
    public List<String> getDefaultGroups() {
        return this.defaultGroup;
    }

    @Override
    public void remove() {
        KIT_MANAGER.removeKit(this);
    }

    public String toString() {
        return "EssentialsKit{name = " + this.name + ", Items = {" + this.itemStacks.toString() + "}}";
    }

    @Override
    public void setStart(boolean start) {
        this.start = start;
    }

    @Override
    public boolean isStart() {
        return this.start;
    }

    @Override
    public String getGroupPrefix() {
        return this.groupPrefix;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getLocalizedName() {
        return this.localizedName;
    }

    @Override
    public void setCooldown(int cooldown) {
        this.cooldown = cooldown;
    }

    @Override
    public int getCooldown() {
        return this.cooldown;
    }
}
