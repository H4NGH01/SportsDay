package org.macausmp.sportsday.competition;

public interface IFieldEvent extends IEvent {
    void onRoundStart();

    void onRoundEnd();

    void nextRound();
}
