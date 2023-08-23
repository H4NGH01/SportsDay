package org.macausmp.sportsday.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.competition.IEvent;

public class CompetitionEndEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final IEvent competition;
    private final boolean force;

    public CompetitionEndEvent(IEvent competition, boolean force) {
        this.competition = competition;
        this.force = force;
    }

    public boolean isForce() {
        return force;
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
