package org.macausmp.sportsday.competition;

import java.util.List;

public interface IRoundGame extends ICompetition {
    void onRoundStart();

    void onRoundEnd();

    void nextRound();

    List<?> getQueue();
}
