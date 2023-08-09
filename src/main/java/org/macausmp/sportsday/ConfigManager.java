package org.macausmp.sportsday;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.*;

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
                Bukkit.getConsoleSender().sendMessage("§aData folder created");
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
                    Bukkit.getConsoleSender().sendMessage(Component.text("%s file has been created".formatted(name)).color(NamedTextColor.GREEN));
                }
            } catch (IOException e) {
                Bukkit.getConsoleSender().sendMessage(Component.text("Could not create the %s file".formatted(name)).color(NamedTextColor.RED));
            }
        }
        return YamlConfiguration.loadConfiguration(file);
    }

    private void loadLanguageFile() {
        if (!languageFile.exists()) {
            try {
                if (languageFile.createNewFile()) {
                    Bukkit.getConsoleSender().sendMessage(Component.text("%s file has been created".formatted(languageFile.getName())).color(NamedTextColor.GREEN));
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
                        Bukkit.getConsoleSender().sendMessage(Component.text("Could not write the lang.json file").color(NamedTextColor.RED));
                    }
                }
            } catch (IOException e) {
                Bukkit.getConsoleSender().sendMessage(Component.text("Could not create the %s file".formatted(languageFile.getName())).color(NamedTextColor.RED));
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
            Bukkit.getConsoleSender().sendMessage(Component.text("Could not save the player.yml file").color(NamedTextColor.RED));
        }
    }

    public FileConfiguration getLanguageConfig() {
        return languageConfig;
    }
}
