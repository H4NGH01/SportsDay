package org.macausmp.sportsday.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.competition.IEvent;

public class CompetitionSetupEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final IEvent competition;

    public CompetitionSetupEvent(IEvent competition) {
        this.competition = competition;
    }

    public IEvent getCompetition() {
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
