/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  ru.bestrax.fakeonline.api.FakeApi
 *  ru.bestrax.fakeonline.base.managers.ImplBotManager
 */
package ru.bestrax.essentials.base.managers;

import java.util.ArrayList;
import java.util.List;
import ru.bestrax.essentials.api.manager.BotManager;
import ru.bestrax.fakeonline.api.FakeApi;
import ru.bestrax.fakeonline.base.managers.ImplBotManager;

public class TrustBotManager
implements BotManager {
    @Override
    public String getBot(String name) {
        if (name == null) {
            return null;
        }
        String bot = null;
        for (String usedAccount : FakeApi.getBotManager().getUsedAccounts()) {
            if (usedAccount.equalsIgnoreCase(name)) {
                bot = usedAccount;
                break;
            }
            if (name.length() < 3 || !usedAccount.startsWith(name)) continue;
            bot = usedAccount;
            break;
        }
        return bot;
    }

    @Override
    public String getPrefix(String name) {
        if (name == null) {
            return null;
        }
        ImplBotManager implBotManager = (ImplBotManager)FakeApi.getBotManager();
        return implBotManager.getPrefixOfNick(name);
    }

    @Override
    public String getChatPrefix(String name) {
        if (name == null) {
            return null;
        }
        ImplBotManager implBotManager = (ImplBotManager)FakeApi.getBotManager();
        return implBotManager.getChatPrefixOfNick(name);
    }

    @Override
    public List<String> getUsedAccounts() {
        return new ArrayList<String>(FakeApi.getBotManager().getUsedAccounts());
    }
}
