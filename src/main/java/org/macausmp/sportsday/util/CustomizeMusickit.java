package org.macausmp.sportsday.util;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.KeyPattern;
import net.kyori.adventure.text.Component;

public enum CustomizeMusickit {
    TAKE_ON_ME("customize.musickit.take_on_me", "take_on_me"),
    BONES("customize.musickit.bones", "bones"),
    CANT_TOUCH_THIS("customize.musickit.cant_touch_this", "cant_touch_this"),
    BAKA_MITAI("customize.musickit.baka_mitai", "baka_mitai"),
    VIRTUAL_INSANITY("customize.musickit.virtual_insanity", "virtual_insanity"),
    MAPS("customize.musickit.maps", "maps"),
    THE_GOOD_YOUTH("customize.musickit.the_good_youth", "the_good_youth"),
    SUBHUMAN("customize.musickit.subhuman", "subhuman"),
    STORM("customize.musickit.storm", "i_am_the_storm_that_is_approaching"),
    DEVIL_TRIGGER("customize.musickit.devil_trigger", "devil_trigger"),
    ;

    private final Component name;
    private final Key key;

    CustomizeMusickit(String code, @KeyPattern.Value String key) {
        this.name = TextUtil.text(Component.translatable(code));
        this.key = Key.key("minecraft", "mvp." + key);
    }

    public Component getName() {
        return name;
    }

    public Key getKey() {
        return key;
    }
}
