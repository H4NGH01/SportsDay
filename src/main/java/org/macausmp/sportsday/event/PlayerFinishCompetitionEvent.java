package org.macausmp.sportsday.event;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.competition.ICompetition;

public class PlayerFinishCompetitionEvent extends PlayerEvent {
    private static final HandlerList handlers = new HandlerList();
    private final ICompetition competition;

    public PlayerFinishCompetitionEvent(@NotNull Player who, ICompetition competition) {
        super(who);
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
