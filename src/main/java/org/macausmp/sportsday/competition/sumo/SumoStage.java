package org.macausmp.sportsday.competition.sumo;

import net.kyori.adventure.text.Component;

import java.util.ArrayList;
import java.util.List;

public class SumoStage {
    public static final SumoStage FINAL = new SumoStage(Component.translatable("competition.sumo.final"), null);
    public static final SumoStage THIRD_PLACE = new SumoStage(Component.translatable("competition.sumo.third_place"), FINAL);
    public static final SumoStage SEMI_FINAL = new SumoStage(Component.translatable("competition.sumo.semi_final"), THIRD_PLACE);
    public static final SumoStage QUARTER_FINAL = new SumoStage(Component.translatable("competition.sumo.quarter_final"), SEMI_FINAL);
    public static final SumoStage ELIMINATE = new SumoStage(Component.translatable("competition.sumo.eliminate"), QUARTER_FINAL);

    private final List<SumoRound> roundList = new ArrayList<>();
    private final Component name;
    private final SumoStage nextStage;
    private SumoRound currentRound;
    private int roundIndex = 0;

    public SumoStage(Component name, SumoStage nextStage) {
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
        return roundList.size() - roundIndex;
    }

    public int getRoundIndex() {
        return roundIndex;
    }

    public void resetRoundIndex() {
        roundIndex = 0;
    }

    public Component getName() {
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

    public void resetStage() {
        roundList.clear();
        currentRound = null;
        roundIndex = 0;
    }
}
