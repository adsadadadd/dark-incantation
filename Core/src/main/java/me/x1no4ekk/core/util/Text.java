package me.x1no4ekk.core.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

import java.util.ArrayList;
import java.util.List;

/**
 * MiniMessage helpers shared by the plugin.
 */
public final class Text {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    private Text() {
    }

    public static MiniMessage miniMessage() {
        return MINI_MESSAGE;
    }

    public static Component parse(String input, TagResolver... resolvers) {
        if (input == null || input.isEmpty()) {
            return Component.empty();
        }
        return MINI_MESSAGE.deserialize(input, resolvers);
    }

    /**
     * Parses text for item names/lore: italic is disabled unless the template explicitly enables it.
     */
    public static Component item(String input, TagResolver... resolvers) {
        return parse(input, resolvers).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE);
    }

    public static List<Component> itemLines(List<String> lines, TagResolver... resolvers) {
        List<Component> result = new ArrayList<>(lines.size());
        for (String line : lines) {
            result.add(item(line, resolvers));
        }
        return result;
    }

    public static String plain(Component component) {
        return PlainTextComponentSerializer.plainText().serialize(component);
    }

    /**
     * Escapes MiniMessage tags in user input (chat messages, nicknames without colour permission...).
     */
    public static String escape(String input) {
        return input == null ? "" : MINI_MESSAGE.escapeTags(input);
    }

    /**
     * Converts legacy colour codes ({@code &a}, {@code §l}, {@code &#ff8800}, {@code §x§f§f§8§8§0§0}) to MiniMessage
     * tags, so prefixes from Vault/LuckPerms or PlaceholderAPI output can be embedded in MiniMessage templates.
     * As in legacy formatting, a colour code resets previous decorations.
     */
    public static String legacyToMiniMessage(String input) {
        if (input == null || input.isEmpty() || (input.indexOf('&') < 0 && input.indexOf('§') < 0)) {
            return input == null ? "" : input;
        }
        StringBuilder out = new StringBuilder(input.length() + 16);
        int length = input.length();
        for (int i = 0; i < length; i++) {
            char c = input.charAt(i);
            if ((c == '&' || c == '§') && i + 1 < length) {
                char code = Character.toLowerCase(input.charAt(i + 1));
                // &#RRGGBB
                if (code == '#' && i + 7 < length && isHex(input, i + 2, 6)) {
                    out.append("<reset><#").append(input, i + 2, i + 8).append('>');
                    i += 7;
                    continue;
                }
                // §x§R§R§G§G§B§B
                if (code == 'x' && i + 13 < length && isSpigotHex(input, i + 2)) {
                    out.append("<reset><#");
                    for (int j = 0; j < 6; j++) {
                        out.append(input.charAt(i + 3 + j * 2));
                    }
                    out.append('>');
                    i += 13;
                    continue;
                }
                String tag = legacyTag(code);
                if (tag != null) {
                    out.append(tag);
                    i++;
                    continue;
                }
            }
            out.append(c);
        }
        return out.toString();
    }

    private static boolean isHex(String input, int from, int count) {
        for (int i = from; i < from + count; i++) {
            if (Character.digit(input.charAt(i), 16) < 0) {
                return false;
            }
        }
        return true;
    }

    private static boolean isSpigotHex(String input, int from) {
        for (int j = 0; j < 6; j++) {
            char marker = input.charAt(from + j * 2);
            if ((marker != '&' && marker != '§') || Character.digit(input.charAt(from + j * 2 + 1), 16) < 0) {
                return false;
            }
        }
        return true;
    }

    private static String legacyTag(char code) {
        return switch (code) {
            case '0' -> "<reset><black>";
            case '1' -> "<reset><dark_blue>";
            case '2' -> "<reset><dark_green>";
            case '3' -> "<reset><dark_aqua>";
            case '4' -> "<reset><dark_red>";
            case '5' -> "<reset><dark_purple>";
            case '6' -> "<reset><gold>";
            case '7' -> "<reset><gray>";
            case '8' -> "<reset><dark_gray>";
            case '9' -> "<reset><blue>";
            case 'a' -> "<reset><green>";
            case 'b' -> "<reset><aqua>";
            case 'c' -> "<reset><red>";
            case 'd' -> "<reset><light_purple>";
            case 'e' -> "<reset><yellow>";
            case 'f' -> "<reset><white>";
            case 'k' -> "<obfuscated>";
            case 'l' -> "<bold>";
            case 'm' -> "<strikethrough>";
            case 'n' -> "<underlined>";
            case 'o' -> "<italic>";
            case 'r' -> "<reset>";
            default -> null;
        };
    }
}
