package org.macausmp.sportsday.competition;

public interface IFieldEvent extends IEvent {
    /**
     * Called when the round starts
     */
    void onRoundStart();

    /**
     * Called when the round ends
     */
    void onRoundEnd();

    /**
     * Called when entering the next round
     */
    void nextRound();
}
