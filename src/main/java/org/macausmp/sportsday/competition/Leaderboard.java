package org.macausmp.sportsday.competition;

import java.util.ArrayList;
import java.util.List;

public class Leaderboard<T> {
    private final List<T> entry = new ArrayList<>();

    public List<T> getEntry() {
        return entry;
    }

    public int size() {
        return entry.size();
    }

    public boolean contains(T e) {
        return entry.contains(e);
    }

    public boolean add(T e) {
        return entry.add(e);
    }

    public void clear() {
        entry.clear();
    }
}
