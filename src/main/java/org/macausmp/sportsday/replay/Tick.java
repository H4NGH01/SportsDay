package org.macausmp.sportsday.replay;

import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class Tick {
    private final List<Event> actions = new ArrayList<>();

    public List<Event> getActions() {
        return actions;
    }

    public void addEvent(@NotNull Event event) {
        actions.add(event);
    }
}
