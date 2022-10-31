package org.macausmp.sportsday;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
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
            } catch (IOException e) {
                Bukkit.getConsoleSender().sendMessage(Component.text("Could not create the player.yml file").color(NamedTextColor.RED));
            }
        }
        playerConfig = YamlConfiguration.loadConfiguration(playerFile);
        Bukkit.getConsoleSender().sendMessage(Component.text("player.yml file has been created"));
    }

    public FileConfiguration getPlayerConfig() {
        return this.playerConfig;
    }

    public void saveConfig() {
        try {
            playerConfig.save(playerFile);
        } catch (IOException e) {
            Bukkit.getConsoleSender().sendMessage(Component.text("Could not save the player.yml file").color(NamedTextColor.RED));
        }
    }
}
