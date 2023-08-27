package org.macausmp.sportsday.competition;

import org.bukkit.potion.PotionEffectType;

public class ObstacleCourse extends AbstractTrackEvent {
    public ObstacleCourse() {
        super("obstacle_course");
    }

    @Override
    protected void onSetup() {
        getPlayerDataList().forEach(data -> data.getPlayer().setCollidable(false));
    }

    @Override
    protected void onStart() {
    }

    @Override
    protected void onEnd(boolean force) {
        Competitions.getOnlinePlayers().forEach(d -> {
            d.getPlayer().setCollidable(true);
            d.getPlayer().removePotionEffect(PotionEffectType.INVISIBILITY);
        });
    }
}
