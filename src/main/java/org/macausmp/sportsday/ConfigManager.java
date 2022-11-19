package org.macausmp.sportsday;

import com.google.common.base.Charsets;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.*;

/**
 * New config file for storing player plugin data
 */
public class ConfigManager {
    private FileConfiguration playerConfig;
    private FileConfiguration langConfig;
    private File playerFile;
    private File langFile;

    @SuppressWarnings("all")
    public void setup() {
        if (!SportsDay.getInstance().getDataFolder().exists()) {
            SportsDay.getInstance().getDataFolder().mkdir();
        }
        playerFile = new File(SportsDay.getInstance().getDataFolder(), "player.yml");
        langFile = new File(SportsDay.getInstance().getDataFolder(), "lang.json");
        playerConfig = loadFile(playerFile, "player.yml");
        loadLang();
    }

    @SuppressWarnings("all")
    private @NotNull FileConfiguration loadFile(@NotNull File file, String name) {
        if (!file.exists()) {
            try {
                file.createNewFile();
                Bukkit.getConsoleSender().sendMessage("§a" + name + " file has been created");
            } catch (IOException e) {
                Bukkit.getConsoleSender().sendMessage("§cCould not create the " + name + " file");
            }
        }
        return YamlConfiguration.loadConfiguration(file);
    }

    @SuppressWarnings("all")
    private void loadLang() {
        if (!langFile.exists()) {
            try {
                langFile.createNewFile();
                Bukkit.getConsoleSender().sendMessage("§a" + langFile.getName() + " file has been created");
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
                    BufferedWriter writer = new BufferedWriter(new FileWriter(langFile, Charsets.UTF_8));
                    writer.write(defaults.toString());
                    writer.close();
                } catch (IOException e) {
                    Bukkit.getConsoleSender().sendMessage("§cCould not write the lang.json file");
                }
            } catch (IOException e) {
                Bukkit.getConsoleSender().sendMessage("§cCould not create the " + langFile.getName() + " file");
            }
        }
        langConfig = YamlConfiguration.loadConfiguration(langFile);
    }

    public FileConfiguration getPlayerConfig() {
        return playerConfig;
    }

    public void saveConfig() {
        try {
            playerConfig.save(playerFile);
        } catch (IOException e) {
            Bukkit.getConsoleSender().sendMessage("§cCould not save the player.yml file");
        }
    }

    public FileConfiguration getLangConfig() {
        return langConfig;
    }
}
