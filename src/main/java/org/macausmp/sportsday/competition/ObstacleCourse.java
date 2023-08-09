package org.macausmp.sportsday.competition;

import org.bukkit.potion.PotionEffectType;

public class ObstacleCourse extends AbstractTrackCompetition {
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
        getPlayerDataList().forEach(data -> {
            if (data.isPlayerOnline()) {
                data.getPlayer().setCollidable(true);
                data.getPlayer().removePotionEffect(PotionEffectType.INVISIBILITY);
            }
        });
    }
}
