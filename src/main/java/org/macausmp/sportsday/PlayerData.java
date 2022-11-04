package org.macausmp.sportsday;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Player extra data for the plugin
 */
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

    /**
     * Returns the UUID of the player
     * @return Player UUID
     */
    public UUID getUUID() {
        return uuid;
    }

    /**
     * Returns the name of the player
     * @return Player name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the player of this data
     * @return Player of this data
     */
    public Player getPlayer() {
        return Bukkit.getOfflinePlayer(uuid).getPlayer();
    }

    /**
     * Checks if this player is currently online
     * @return True if the player is online
     */
    public boolean isPlayerOnline() {
        return Bukkit.getOfflinePlayer(uuid).isOnline();
    }

    /**
     * Returns the number of the player
     * @return Player number
     */
    public int getNumber() {
        return number;
    }

    /**
     * Returns the score of the player
     * @return Player score
     */
    public int getScore() {
        return score;
    }

    /**
     * Sets the score of the player
     * @param score new score to the player
     */
    public void setScore(int score) {
        this.score = score;
    }

    /**
     * Add the value of the score
     * @param score value to increase of the score
     */
    public void addScore(int score) {
        this.score += score;
    }
}
