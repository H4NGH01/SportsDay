package org.macausmp.sportsday;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

/**
 * Config files for storing players' plugin data
 */
public final class ConfigManager {
    private static final SportsDay PLUGIN = SportsDay.getInstance();
    private File contestantsFile;
    private FileConfiguration contestantsConfig;

    void setup() {
        if (!PLUGIN.getDataFolder().exists() && PLUGIN.getDataFolder().mkdir())
            PLUGIN.getLogger().log(Level.INFO, "Data folder created");
        contestantsFile = new File(PLUGIN.getDataFolder(), "contestants.yml");
        if (!contestantsFile.exists()) {
            try {
                if (contestantsFile.createNewFile())
                    PLUGIN.getLogger().log(Level.INFO, "contestants.yml file has been created");
            } catch (IOException e) {
                PLUGIN.getLogger().log(Level.SEVERE, "Could not create the contestants.yml file");
            }
        }
        contestantsConfig = YamlConfiguration.loadConfiguration(contestantsFile);
    }

    /**
     * Get the config that store contestants data
     * @return contestants data config
     */
    public FileConfiguration getContestantsConfig() {
        return contestantsConfig;
    }

    public void saveConfig() {
        try {
            contestantsConfig.save(contestantsFile);
        } catch (IOException e) {
            PLUGIN.getLogger().log(Level.SEVERE, "Could not save the contestants.yml file", e);
        }
    }
}
