package org.macausmp.sportsday.competition;

public interface IRoundGame extends ICompetition {
    void onRoundStart();

    void onRoundEnd();

    void nextRound();
}
