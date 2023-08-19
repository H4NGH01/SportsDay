package org.macausmp.sportsday;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.logging.Level;

/**
 * New config file for storing players' competition data
 */
public final class ConfigManager {
    private final SportsDay plugin = SportsDay.getInstance();
    private File playerFile;
    private FileConfiguration playerConfig;
    private File customizeFile;
    private FileConfiguration customizeConfig;
    private File languageFile;
    private FileConfiguration languageConfig;

    public void setup() {
        if (!plugin.getDataFolder().exists()) {
            if (plugin.getDataFolder().mkdir()) {
                plugin.getLogger().log(Level.INFO, "Data folder created");
            }
        }
        playerFile = new File(plugin.getDataFolder(), "player.yml");
        customizeFile = new File(plugin.getDataFolder(), "customize.yml");
        languageFile = new File(plugin.getDataFolder(), "lang.json");
        playerConfig = loadFile(playerFile, "player.yml");
        customizeConfig = loadFile(customizeFile, "customize.yml");
        loadLanguageFile();
    }

    private @NotNull FileConfiguration loadFile(@NotNull File file, String name) {
        if (!file.exists()) {
            try {
                if (file.createNewFile()) {
                    plugin.getLogger().log(Level.INFO, name + " file has been created");
                }
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Could not create the " + name + " file");
            }
        }
        return YamlConfiguration.loadConfiguration(file);
    }

    private void loadLanguageFile() {
        if (!languageFile.exists()) {
            try {
                if (languageFile.createNewFile()) {
                    plugin.getLogger().log(Level.INFO, "lang.json file has been created");
                    final InputStream defLangStream = plugin.getResource("lang.json");
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
                        plugin.getLogger().log(Level.SEVERE, "Could not write the lang.json file", e);
                    }
                }
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Could not create the lang.json file", e);
            }
        }
        languageConfig = YamlConfiguration.loadConfiguration(languageFile);
    }

    public FileConfiguration getPlayerConfig() {
        return playerConfig;
    }

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

    public FileConfiguration getLanguageConfig() {
        return languageConfig;
    }
}
