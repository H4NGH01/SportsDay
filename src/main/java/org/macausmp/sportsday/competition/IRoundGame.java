package org.macausmp.sportsday.competition;

import org.macausmp.sportsday.PlayerData;

import java.util.List;

public interface IRoundGame extends ICompetition {
    void onRoundStart();

    void onRoundEnd();

    void nextRound();

    List<PlayerData> getQueue();
}
