package org.macausmp.sportsday;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class PlayerConfig {
    private FileConfiguration playerConfig;
    private File playerFile;

    @SuppressWarnings("all")
    public void setup() {
        if (!SportsDay.getInstance().getDataFolder().exists()) {
            SportsDay.getInstance().getDataFolder().mkdir();
        }
        playerFile = new File(SportsDay.getInstance().getDataFolder(), "player.yml");
        if (!playerFile.exists()) {
            try {
                playerFile.createNewFile();
                Bukkit.getConsoleSender().sendMessage("§aplayer.yml file has been created");
            } catch (IOException e) {
                Bukkit.getConsoleSender().sendMessage("§cCould not create the player.yml file");
            }
        }
        playerConfig = YamlConfiguration.loadConfiguration(playerFile);
    }

    public FileConfiguration getPlayerConfig() {
        return this.playerConfig;
    }

    public void saveConfig() {
        try {
            playerConfig.save(playerFile);
        } catch (IOException e) {
            Bukkit.getConsoleSender().sendMessage("§cCould not save the player.yml file");
        }
    }
}
