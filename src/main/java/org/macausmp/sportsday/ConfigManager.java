package org.macausmp.sportsday;

import com.google.common.base.Charsets;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

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
        langFile = new File(SportsDay.getInstance().getDataFolder(), "lang.yml");
        playerConfig = loadFile(playerFile, "player.yml");
        langConfig = loadFile(langFile, "lang.yml");
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

    private void loadLang() {
        final InputStream defConfigStream = SportsDay.getInstance().getResource("lang.yml");
        if (defConfigStream == null) {
            return;
        }
        langConfig.setDefaults(YamlConfiguration.loadConfiguration(new InputStreamReader(defConfigStream, Charsets.UTF_8)));
        langConfig.options().copyDefaults(true);
        try {
            langConfig.save(langFile);
        } catch (IOException e) {
            Bukkit.getConsoleSender().sendMessage("§cCould not save the lang.yml file");
        }
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
