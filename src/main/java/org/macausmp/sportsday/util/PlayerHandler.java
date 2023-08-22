package org.macausmp.sportsday.util;

import org.bukkit.entity.Player;

import java.util.UUID;

public interface PlayerHandler {
    /**
     * Get the player's uuid.
     *
     * @return The player's uuid.
     */
    UUID getUUID();

    /**
     * Get the player.
     *
     * @return The player.
     */
    Player getPlayer();
}
