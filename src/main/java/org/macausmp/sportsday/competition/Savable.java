package org.macausmp.sportsday.competition;

import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

public interface Savable {
    void load(@NotNull FileConfiguration config);

    void save(@NotNull FileConfiguration config);
}
