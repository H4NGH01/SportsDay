package org.macausmp.sportsday;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.logging.Level;

/**
 * New config file for storing players' competition data
 */
public class ConfigManager {
    private File playerFile;
    private FileConfiguration playerConfig;
    private File languageFile;
    private FileConfiguration languageConfig;

    public void setup() {
        if (!SportsDay.getInstance().getDataFolder().exists()) {
            if (SportsDay.getInstance().getDataFolder().mkdir()) {
                SportsDay.getInstance().getLogger().log(Level.INFO, "Data folder created");
            }
        }
        playerFile = new File(SportsDay.getInstance().getDataFolder(), "player.yml");
        languageFile = new File(SportsDay.getInstance().getDataFolder(), "lang.json");
        playerConfig = loadFile(playerFile, "player.yml");
        loadLanguageFile();
    }

    @SuppressWarnings("SameParameterValue")
    private @NotNull FileConfiguration loadFile(@NotNull File file, String name) {
        if (!file.exists()) {
            try {
                if (file.createNewFile()) {
                    SportsDay.getInstance().getLogger().log(Level.INFO, name + " file has been created");
                }
            } catch (IOException e) {
                SportsDay.getInstance().getLogger().log(Level.SEVERE, "Could not create the " + name + " file");
            }
        }
        return YamlConfiguration.loadConfiguration(file);
    }

    private void loadLanguageFile() {
        if (!languageFile.exists()) {
            try {
                if (languageFile.createNewFile()) {
                    SportsDay.getInstance().getLogger().log(Level.INFO, "lang.json file has been created");
                    final InputStream defLangStream = SportsDay.getInstance().getResource("lang.json");
                    if (defLangStream == null) {
                        return;
                    }
                    StringBuilder defaults = new StringBuilder();
                    try (BufferedReader br = new BufferedReader(new InputStreamReader(defLangStream))) {
                        String line;
                        while ((line = br.readLine()) != null) {
                            defaults.append(line).append("\n");
                        }
                        BufferedWriter writer = new BufferedWriter(new FileWriter(languageFile));
                        writer.write(defaults.toString());
                        writer.close();
                    } catch (IOException e) {
                        SportsDay.getInstance().getLogger().log(Level.SEVERE, "Could not write the lang.json file", e);
                    }
                }
            } catch (IOException e) {
                SportsDay.getInstance().getLogger().log(Level.SEVERE, "Could not create the lang.json file", e);
            }
        }
        languageConfig = YamlConfiguration.loadConfiguration(languageFile);
    }

    public FileConfiguration getPlayerConfig() {
        return playerConfig;
    }

    public void saveConfig() {
        try {
            playerConfig.save(playerFile);
        } catch (IOException e) {
            SportsDay.getInstance().getLogger().log(Level.SEVERE, "Could not save the player.yml file", e);
        }
    }

    public FileConfiguration getLanguageConfig() {
        return languageConfig;
    }
}
