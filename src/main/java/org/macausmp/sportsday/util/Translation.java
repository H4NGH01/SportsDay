package org.macausmp.sportsday.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.SportsDay;

import java.util.Objects;

public class Translation {
    private static final FileConfiguration lang = SportsDay.getInstance().getLangConfig();

    public static @NotNull TranslatableComponent translatable(String key) {
        return lang.getString(key) != null ? Component.translatable(Objects.requireNonNull(lang.getString(key))) : Component.translatable(key);
    }
}
