package org.macausmp.sportsday.competition;

import java.util.ArrayList;
import java.util.List;

public class Leaderboard<T> {
    private final List<T> entry = new ArrayList<>();

    public List<T> getEntry() {
        return entry;
    }
}
