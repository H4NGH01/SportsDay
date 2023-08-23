package org.macausmp.sportsday.util;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.KeyPattern;
import net.kyori.adventure.text.Component;

public enum CustomizeMusickit {
    TAKE_ON_ME("customize.musickit.take_on_me", "take_on_me"),
    BAKA_MITAI("customize.musickit.baka_mitai", "baka_mitai"),
    VIRTUAL_INSANITY("customize.musickit.virtual_insanity", "virtual_insanity"),
    CANT_TOUCH_THIS("customize.musickit.cant_touch_this", "cant_touch_this"),
    YOU_SPIN_ME_ROUND("customize.musickit.you_spin_me_round", "you_spin_me_round"),
    BONES("customize.musickit.bones", "bones"),
    MAPS("customize.musickit.maps", "maps"),
    TURN_DOWN_FOR_WHAT("customize.musickit.turn_down_for_what", "turn_down_for_what"),
    THE_GOOD_YOUTH("customize.musickit.the_good_youth", "the_good_youth"),
    SUBHUMAN("customize.musickit.subhuman", "subhuman"),
    BURY_THE_LIGHT("customize.musickit.bury_the_light", "bury_the_light"),
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
