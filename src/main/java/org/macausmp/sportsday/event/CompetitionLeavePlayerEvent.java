package org.macausmp.sportsday.event;

import org.bukkit.OfflinePlayer;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class CompetitionLeavePlayerEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    private final OfflinePlayer player;

    public CompetitionLeavePlayerEvent(@NotNull OfflinePlayer who) {
        this.player = who;
    }

    public final OfflinePlayer getPlayer() {
        return player;
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
