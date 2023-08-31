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
    private File playerFile;
    private FileConfiguration playerConfig;
    private File customizeFile;
    private FileConfiguration customizeConfig;

    void setup() {
        if (!plugin.getDataFolder().exists() && plugin.getDataFolder().mkdir()) plugin.getLogger().log(Level.INFO, "Data folder created");
        playerFile = new File(plugin.getDataFolder(), "player.yml");
        customizeFile = new File(plugin.getDataFolder(), "customize.yml");
        playerConfig = loadFile(playerFile, "player.yml");
        customizeConfig = loadFile(customizeFile, "customize.yml");
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
     * Get the config that store players' data
     * @return players config file
     */
    public FileConfiguration getPlayerConfig() {
        return playerConfig;
    }

    /**
     * Get the config that store players' customize data
     * @return players' customize config file
     */
    public FileConfiguration getCustomizeConfig() {
        return customizeConfig;
    }

    public void saveConfig() {
        try {
            playerConfig.save(playerFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save the player.yml file", e);
        }
        try {
            customizeConfig.save(customizeFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save the customize.yml file", e);
        }
    }
}
