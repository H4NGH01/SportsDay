package org.macausmp.sportsday.competition;

import net.kyori.adventure.text.Component;
import org.macausmp.sportsday.util.Translation;

/**
 * Competition stage
 */
public enum Stage {
    IDLE,
    COMING,
    STARTED,
    ENDED;

    private final Component name;

    Stage() {
        this.name = Translation.translatable("competition.status." + name().toLowerCase());
    }

    public Component getName() {
        return name;
    }
}
