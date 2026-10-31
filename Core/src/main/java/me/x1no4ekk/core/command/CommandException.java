package me.x1no4ekk.core.command;

import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

/**
 * Aborts a command and shows a message from messages.yml. Thrown by {@link BaseCommand} helpers,
 * caught in {@link BaseCommand#onCommand}. Has no stack trace: it is flow control, not an error.
 */
public final class CommandException extends RuntimeException {

    private final String messageKey;
    private final transient TagResolver[] resolvers;

    public CommandException(String messageKey, TagResolver... resolvers) {
        super(messageKey, null, false, false);
        this.messageKey = messageKey;
        this.resolvers = resolvers;
    }

    public String messageKey() {
        return messageKey;
    }

    public TagResolver[] resolvers() {
        return resolvers;
    }
}
