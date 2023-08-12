package org.macausmp.sportsday.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class ColorTextUtil {
    private static final Map<Character, NamedTextColor> COLOR_MAP = new HashMap<>();
    private static final Map<Character, TextDecoration> STYLE_MAP = new HashMap<>();

    static {
        COLOR_MAP.put('0', NamedTextColor.BLACK);
        COLOR_MAP.put('1', NamedTextColor.DARK_BLUE);
        COLOR_MAP.put('2', NamedTextColor.DARK_GREEN);
        COLOR_MAP.put('3', NamedTextColor.DARK_AQUA);
        COLOR_MAP.put('4', NamedTextColor.DARK_RED);
        COLOR_MAP.put('5', NamedTextColor.DARK_PURPLE);
        COLOR_MAP.put('6', NamedTextColor.GOLD);
        COLOR_MAP.put('7', NamedTextColor.GRAY);
        COLOR_MAP.put('8', NamedTextColor.DARK_GRAY);
        COLOR_MAP.put('9', NamedTextColor.BLUE);
        COLOR_MAP.put('a', NamedTextColor.GREEN);
        COLOR_MAP.put('b', NamedTextColor.AQUA);
        COLOR_MAP.put('c', NamedTextColor.RED);
        COLOR_MAP.put('d', NamedTextColor.LIGHT_PURPLE);
        COLOR_MAP.put('e', NamedTextColor.YELLOW);
        COLOR_MAP.put('f', NamedTextColor.WHITE);
        STYLE_MAP.put('k', TextDecoration.OBFUSCATED);
        STYLE_MAP.put('l', TextDecoration.BOLD);
        STYLE_MAP.put('m', TextDecoration.STRIKETHROUGH);
        STYLE_MAP.put('n', TextDecoration.UNDERLINED);
        STYLE_MAP.put('o', TextDecoration.ITALIC);
    }

    /**
     * Convert {@link Component} with '§' to {@link Component} with {@link net.kyori.adventure.text.format.TextColor}
     * @param origin Origin component
     * @return Component with color and style
     */
    public static @NotNull Component convert(@NotNull Component origin) {
        Component component = Component.text().decoration(TextDecoration.ITALIC, false).build();
        String[] sa = LegacyComponentSerializer.legacyAmpersand().serialize(origin).split("§");
        NamedTextColor color = null;
        TextDecoration decoration = null;
        for (String s : sa) {
            char code = s.charAt(1);
            if (code == 'r') {
                color = null;
                decoration = null;
                if (s.length() > 2) {
                    component = component.append(Component.text(s.substring(2)));
                }
                continue;
            }
            if (COLOR_MAP.containsKey(code)) {
                color = COLOR_MAP.get(code);
                decoration = null;
            }
            if (STYLE_MAP.containsKey(code)) {
                decoration = STYLE_MAP.get(code);
            }
            if (s.length() == 2) continue;
            Component c = Component.text(s.substring(2));
            if (color != null) c = c.color(color);
            if (decoration != null) c = c.decoration(decoration, true);
            component = component.append(c);
        }
        return component;
    }
}
