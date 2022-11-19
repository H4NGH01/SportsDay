package org.macausmp.sportsday.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.competition.ICompetition;

public class CompetitionSetupEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final ICompetition competition;

    public CompetitionSetupEvent(ICompetition competition) {
        this.competition = competition;
    }

    public ICompetition getCompetition() {
        return competition;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return handlers;
    }
}
