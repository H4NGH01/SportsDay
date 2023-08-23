package org.macausmp.sportsday.util;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * {@link Player} extra data for the plugin
 */
public final class PlayerData implements PlayerHolder {
    private final UUID uuid;
    private final int number;
    private int score = 0;

    public PlayerData(UUID uuid, int number) {
        this.uuid = uuid;
        this.number = number;
    }

    public PlayerData(UUID uuid, int number, int score) {
        this.uuid = uuid;
        this.number = number;
        this.score = score;
    }

    /**
     * Returns the UUID of the {@link Player}
     * @return {@link Player#getUniqueId()}
     */
    @Override
    public @NotNull UUID getUUID() {
        return uuid;
    }

    /**
     * Returns the name of the {@link Player}
     * @return {@link Player#getName()} ()}
     */
    public String getName() {
        return getOfflinePlayer().getName();
    }

    /**
     * Returns the {@link #number} of the {@link Player}
     * @return {@link Player} {@link #number}
     */
    public int getNumber() {
        return number;
    }

    /**
     * Returns the {@link #score} of the {@link Player}
     * @return {@link Player} {@link #score}
     */
    public int getScore() {
        return score;
    }

    /**
     * Sets the {@link #score} of the {@link Player}
     * @param score new {@link #score} set for the {@link Player}
     */
    public void setScore(int score) {
        this.score = score;
    }

    /**
     * Add the value of the {@link #score}
     * @param score value to increase of the {@link #score}
     */
    public void addScore(int score) {
        this.score += score;
    }
}
