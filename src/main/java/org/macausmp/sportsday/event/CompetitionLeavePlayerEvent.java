package org.macausmp.sportsday.event;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

public class CompetitionLeavePlayerEvent extends PlayerEvent {
    private static final HandlerList handlers = new HandlerList();

    public CompetitionLeavePlayerEvent(@NotNull Player who) {
        super(who);
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }
}
