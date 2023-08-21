package org.macausmp.sportsday.competition;

import net.kyori.adventure.text.Component;
import org.macausmp.sportsday.util.TextUtil;

/**
 * Represents the competition stage
 */
public enum Stage {
    IDLE("competition.status.idle"),
    COMING("competition.status.coming"),
    STARTED("competition.status.started"),
    ENDED("competition.status.ended");

    private final Component name;

    Stage(String name) {
        this.name = TextUtil.convert(Component.translatable(name));
    }

    public Component getName() {
        return name;
    }
}
