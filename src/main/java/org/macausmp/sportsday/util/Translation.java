package org.macausmp.sportsday.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.SportsDay;

import java.util.Objects;

public class Translation {
    private static final FileConfiguration LANGUAGE_CONFIG = SportsDay.getInstance().getLanguageConfig();

    public static @NotNull TranslatableComponent translatable(String key) {
        return Component.translatable(LANGUAGE_CONFIG.getString(key) != null ? Objects.requireNonNull(LANGUAGE_CONFIG.getString(key)) : key);
    }
}
