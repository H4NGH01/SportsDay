package org.macausmp.sportsday.competition;

import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;

/**
 * Competition interface
 */
public interface ICompetition extends Listener {
    /**
     * Get the competition's id from the config file
     * @return Competition id
     */
    String getID();

    /**
     * Get the competition's name from the config file
     * @return Competition name
     */
    Component getName();

    /**
     * Get the competition's minimum required number of players from the config file
     * @return minimum required number of players of competition
     */
    int getLeastPlayersRequired();

    /**
     * Get the competition's location from the config file
     * @return location of competition
     */
    Location getLocation();

    /**
     * Get the competition's world
     * @return world of competition
     */
    World getWorld();

    /**
     * Return true if competition is enabled
     * @return True if competition is enabled
     */
    boolean isEnable();

    /**
     * Set up the competition and make it get ready to start
     */
    void setup();

    /**
     * Start the competition
     */
    void start();

    /**
     * End the competition
     * @param force True if end the competition via command or gui instead of natural end
     */
    void end(boolean force);

    /**
     * Listener call from {@link CompetitionListener}
     * @param event event given from {@link CompetitionListener}
     */
    <T extends Event> void onEvent(T event);

    /**
     * Get the current competition stage
     * @return current competition stage
     */
    Stage getStage();

    /**
     * Set the current competition stage
     * @param stage new stage
     */
    void setStage(Stage stage);

    /**
     * Get the leaderboard of competition
     * @return leaderboard of competition
     */
    Leaderboard<?> getLeaderboard();
}
