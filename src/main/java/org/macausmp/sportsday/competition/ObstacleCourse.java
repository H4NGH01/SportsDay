package org.macausmp.sportsday.competition;

import org.bukkit.potion.PotionEffectType;

public class ObstacleCourse extends AbstractTrackEvent {
    public ObstacleCourse() {
        super("obstacle_course");
    }

    @Override
    public void onSetup() {
        getPlayerDataList().forEach(data -> data.getPlayer().setCollidable(false));
    }

    @Override
    public void onStart() {
    }

    @Override
    public void onEnd(boolean force) {
        Competitions.getOnlinePlayers().forEach(d -> {
            d.getPlayer().setCollidable(true);
            d.getPlayer().removePotionEffect(PotionEffectType.INVISIBILITY);
        });
    }
}
