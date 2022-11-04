package org.macausmp.sportsday.event;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

public class CompetitionJoinPlayerEvent extends PlayerEvent {
    private static final HandlerList handlers = new HandlerList();

    public CompetitionJoinPlayerEvent(@NotNull Player who) {
        super(who);
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }
}
