package org.macausmp.sportsday.customize;

import net.kyori.adventure.text.Component;
import org.macausmp.sportsday.util.TextUtil;

public enum CustomizeGraffitiSpray {
    GGWP("customize.graffiti_spray.ggwp"),
    DISMAL("customize.graffiti_spray.d"),
    CRAZY("customize.graffiti_spray.c"),
    BADASS("customize.graffiti_spray.b"),
    APOCALYPTIC("customize.graffiti_spray.a"),
    SAVAGE("customize.graffiti_spray.s"),
    SICK_SKILLS("customize.graffiti_spray.ss"),
    SMOKIN_SEXY_STYLE("customize.graffiti_spray.sss");

    private final Component name;

    CustomizeGraffitiSpray(String code) {
        this.name = TextUtil.text(Component.translatable(code));
    }

    public Component getName() {
        return name;
    }
}
