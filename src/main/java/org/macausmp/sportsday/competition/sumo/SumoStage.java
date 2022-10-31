package org.macausmp.sportsday.competition.sumo;

import java.util.ArrayList;
import java.util.List;

public class SumoStage {
    public static final SumoStage FINAL = new SumoStage("決賽", null);
    public static final SumoStage THIRD_PLACE = new SumoStage("季軍賽", FINAL);
    public static final SumoStage SEMI_FINAL = new SumoStage("四強", THIRD_PLACE);
    public static final SumoStage QUARTER_FINAL = new SumoStage("八強", SEMI_FINAL);
    public static final SumoStage ELIMINATE = new SumoStage("淘汰賽", QUARTER_FINAL);

    private final List<SumoRound> roundList = new ArrayList<>();
    private final String name;
    private final SumoStage nextStage;
    private SumoRound currentRound;
    private int roundIndex = 0;

    public SumoStage(String name, SumoStage nextStage) {
        this.name = name;
        this.nextStage = nextStage;
    }

    public void nextRound() {
        currentRound = roundList.get(roundIndex++);
    }

    public List<SumoRound> getRoundList() {
        return roundList;
    }

    public int getRoundRemaining() {
        int i = 0;
        for (SumoRound r : roundList) {
            if (r.getStatus().equals(SumoRound.RoundStatus.IDLE)) {
                i++;
            }
        }
        return i;
    }

    public String getName() {
        return name;
    }

    public boolean hasNextStage() {
        return nextStage != null;
    }

    public SumoStage getNextStage() {
        return nextStage;
    }

    public SumoRound getCurrentRound() {
        return currentRound;
    }
}
