package org.macausmp.sportsday;

import org.bukkit.entity.Player;

import java.util.Objects;
import java.util.UUID;

public class PlayerData {
    private final UUID uuid;
    private final String name;
    private final int number;
    private int score = 0;

    public PlayerData(UUID uuid, int number) {
        this.uuid = uuid;
        this.name = Objects.requireNonNull(SportsDay.getInstance().getServer().getPlayer(uuid)).getName();
        this.number = number;
    }

    public PlayerData(UUID uuid, int number, int score) {
        this.uuid = uuid;
        this.name = SportsDay.getInstance().getServer().getOfflinePlayer(uuid).getName();
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
        return SportsDay.getPlayer(uuid).getPlayer();
    }

    public boolean isPlayerOnline() {
        return SportsDay.getInstance().getServer().getOfflinePlayer(uuid).isOnline();
    }

    public int getNumber() {
        return number;
    }

    public int getScore() {
        return score;
    }
}
