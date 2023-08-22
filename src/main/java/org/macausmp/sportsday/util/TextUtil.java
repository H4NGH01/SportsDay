package org.macausmp.sportsday.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.jetbrains.annotations.NotNull;

public final class TextUtil {
    /**
     * Convert legacy color code '§' to {@link net.kyori.adventure.text.format.TextColor}
     * @param origin Origin component
     * @return Component with color and style
     */
    public static @NotNull Component convert(@NotNull Component origin) {
        return LegacyComponentSerializer.legacySection().deserialize(LegacyComponentSerializer.legacySection().serialize(origin)).decoration(TextDecoration.ITALIC, false);
    }

    /**
     * Convert class instanceof {@link Component} to {@link net.kyori.adventure.text.TextComponent}
     * @param component Component
     * @return Text component
     */
    public static @NotNull TextComponent text(@NotNull Component component) {
        return Component.text(LegacyComponentSerializer.legacySection().serialize(component)).decoration(TextDecoration.ITALIC, false);
    }
}
