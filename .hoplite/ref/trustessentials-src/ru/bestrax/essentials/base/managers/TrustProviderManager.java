/*
 * Decompiled with CFR 0.152.
 */
package ru.bestrax.essentials.base.managers;

import io.papermc.lib.PaperLib;
import ru.bestrax.essentials.api.EssentialsApi;
import ru.bestrax.essentials.api.provider.ContainerProvider;
import ru.bestrax.essentials.api.provider.ProviderManager;
import ru.bestrax.essentials.api.provider.providers.PaperContainerProvider;

public class TrustProviderManager
implements ProviderManager {
    private ContainerProvider containerProvider;

    public TrustProviderManager() {
        System.out.println(PaperLib.isPaper());
        if (PaperLib.isPaper() && Double.parseDouble(EssentialsApi.getInstance().getFormattedVersion()) >= 1.15) {
            this.containerProvider = new PaperContainerProvider();
        }
    }

    @Override
    public ContainerProvider getContainerProvider() {
        return this.containerProvider;
    }
}
