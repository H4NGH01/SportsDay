package org.macausmp.sportsday.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.jetbrains.annotations.NotNull;

public final class TextUtil {

    /**
     * Convert {@link net.kyori.adventure.text.TranslatableComponent} to {@link net.kyori.adventure.text.TextComponent}
     * @param origin Origin component
     * @return Component with color and style
     */
    public static @NotNull Component convert(@NotNull Component origin) {
        return LegacyComponentSerializer.legacySection().deserialize(LegacyComponentSerializer.legacySection().serialize(origin)).decoration(TextDecoration.ITALIC, false);
    }
}
