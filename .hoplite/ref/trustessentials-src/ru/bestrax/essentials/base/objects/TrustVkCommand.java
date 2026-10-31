/*
 * Decompiled with CFR 0.152.
 */
package ru.bestrax.essentials.base.objects;

import java.util.function.Function;
import ru.bestrax.essentials.api.CommandType;
import ru.bestrax.essentials.api.VkCommand;
import ru.bestrax.essentials.api.vk.messages.ReceiveMessage;

public class TrustVkCommand
implements VkCommand {
    private String mainCommand;
    private String[] viewAliases;
    private Function<ReceiveMessage, Object> executor;
    private CommandType commandType = CommandType.EQUALS;

    public TrustVkCommand(CommandType vkCommandType, Function<ReceiveMessage, Object> function, String mainCommand, String ... viewAliases) {
        this.commandType = vkCommandType;
        this.executor = function;
        this.mainCommand = mainCommand;
        this.viewAliases = viewAliases;
    }

    public TrustVkCommand(Function<ReceiveMessage, Object> function, String mainCommand, String ... viewAliases) {
        this.executor = function;
        this.mainCommand = mainCommand;
        this.viewAliases = viewAliases;
    }

    @Override
    public String getMainCommand() {
        return this.mainCommand;
    }

    @Override
    public String[] getViewAliases() {
        return this.viewAliases;
    }

    @Override
    public Function<ReceiveMessage, Object> getExecutor() {
        return this.executor;
    }

    @Override
    public CommandType getType() {
        return this.commandType;
    }
}
