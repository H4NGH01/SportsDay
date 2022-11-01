package org.macausmp.sportsday;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

public class PlayerData {
    private final UUID uuid;
    private final String name;
    private final int number;
    private int score = 0;

    public PlayerData(UUID uuid, int number) {
        this.uuid = uuid;
        this.name = Bukkit.getOfflinePlayer(uuid).getName();
        this.number = number;
    }

    public PlayerData(UUID uuid, int number, int score) {
        this.uuid = uuid;
        this.name = Bukkit.getOfflinePlayer(uuid).getName();
        this.number = number;
        this.score = score;
    }

    public UUID getUUID() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public Player getPlayer() {
        return Bukkit.getOfflinePlayer(uuid).getPlayer();
    }

    public boolean isPlayerOnline() {
        return Bukkit.getOfflinePlayer(uuid).isOnline();
    }

    public int getNumber() {
        return number;
    }

    public int getScore() {
        return score;
    }
}
