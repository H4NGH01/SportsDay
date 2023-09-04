package org.macausmp.sportsday;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

/**
 * Config files for storing players' plugin data
 */
public final class ConfigManager {
    private final SportsDay plugin = SportsDay.getInstance();
    private File competitorFile;
    private FileConfiguration competitorConfig;
    private File playerdataFile;
    private FileConfiguration playerdataConfig;

    void setup() {
        if (!plugin.getDataFolder().exists() && plugin.getDataFolder().mkdir()) plugin.getLogger().log(Level.INFO, "Data folder created");
        competitorFile = new File(plugin.getDataFolder(), "competitor.yml");
        playerdataFile = new File(plugin.getDataFolder(), "playerdata.yml");
        competitorConfig = loadFile(competitorFile, "competitor.yml");
        playerdataConfig = loadFile(playerdataFile, "playerdata.yml");
    }

    private @NotNull FileConfiguration loadFile(@NotNull File file, String name) {
        if (!file.exists()) {
            try {
                if (file.createNewFile()) plugin.getLogger().log(Level.INFO, name + " file has been created");
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Could not create the " + name + " file");
            }
        }
        return YamlConfiguration.loadConfiguration(file);
    }

    /**
     * Get the config that store competitors' data
     * @return competitors data file
     */
    public FileConfiguration getCompetitorConfig() {
        return competitorConfig;
    }

    /**
     * Get the config that store players' data
     * @return players data file
     */
    public FileConfiguration getPlayerdataConfig() {
        return playerdataConfig;
    }

    public void saveConfig() {
        try {
            competitorConfig.save(competitorFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save the competitor.yml file", e);
        }
        try {
            playerdataConfig.save(playerdataFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save the playerdata.yml file", e);
        }
    }
}
