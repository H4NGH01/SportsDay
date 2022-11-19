package org.macausmp.sportsday.event;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.competition.ITrackCompetition;

public class PlayerFinishLapEvent extends PlayerEvent {
    private static final HandlerList handlers = new HandlerList();
    private final ITrackCompetition competition;

    public PlayerFinishLapEvent(@NotNull Player who, ITrackCompetition competition) {
        super(who);
        this.competition = competition;
    }

    public ITrackCompetition getCompetition() {
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
