package org.macausmp.sportsday.competition;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

/**
 * Competition stage
 */
public enum Stage {
    IDLE(Component.text("§c待開始").color(NamedTextColor.GRAY)),
    COMING(Component.text("即將開始").color(NamedTextColor.GREEN)),
    STARTED(Component.text("開始中").color(NamedTextColor.YELLOW)),
    ENDED(Component.text("已結束").color(NamedTextColor.RED));

    private final Component name;

    Stage(Component name) {
        this.name = name;
    }

    public Component getName() {
        return name;
    }
}
