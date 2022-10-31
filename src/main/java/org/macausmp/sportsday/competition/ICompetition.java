package org.macausmp.sportsday.competition;

import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.event.Event;

public interface ICompetition {
    String getID();

    Component getName();

    int getLeastPlayerRequired();

    Location getLocation();

    boolean isEnable();

    void setup();

    void start();

    void end(boolean force);

    <T extends Event> void onEvent(T event);

    Stage getStage();

    void setStage(Stage stage);

    Leaderboard<?> getLeaderboard();

    enum Stage {
        IDLE,
        COMING,
        STARTED,
        ENDED
    }
}
