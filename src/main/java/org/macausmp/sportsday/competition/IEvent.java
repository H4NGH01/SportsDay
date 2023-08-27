package org.macausmp.sportsday.competition;

import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;
import org.macausmp.sportsday.util.PlayerHolder;

import java.util.List;

/**
 * Represents a competition event
 */
public interface IEvent extends Listener {
    /**
     * Get the event's id from the config file
     * @return ID of event
     */
    String getID();

    /**
     * Get the event's name from the config file
     * @return Name of event
     */
    Component getName();

    /**
     * Get the event's minimum required number of players from the config file
     * @return minimum required number of players of event
     */
    int getLeastPlayersRequired();

    /**
     * Get the event's location from the config file
     * @return Location of event
     */
    Location getLocation();

    /**
     * Get the event's world
     * @return World of event
     */
    World getWorld();

    /**
     * Return true if event is enabled
     * @return True if event is enabled
     */
    boolean isEnable();

    /**
     * Set up the event and make it get ready to start
     */
    void setup();

    /**
     * Start the event
     */
    void start();

    /**
     * End the event
     * @param force True if end the event via command or gui instead of natural end
     */
    void end(boolean force);

    /**
     * Listener call from {@link CompetitionListener}
     * @param event {@link Event} given from {@link CompetitionListener}
     */
    <T extends Event> void onEvent(T event);

    /**
     * Get the current event stage
     * @return Current stage of event
     */
    Stage getStage();

    /**
     * Get the leaderboard of event
     * @return Leaderboard of event
     */
    List<? extends PlayerHolder> getLeaderboard();

    /**
     * Teleport player to event location and sets up practice environment for the player
     * @param player Who going to practice this event
     */
    void practice(Player player);
}
