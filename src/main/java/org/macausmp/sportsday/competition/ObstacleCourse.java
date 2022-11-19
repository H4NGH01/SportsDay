package org.macausmp.sportsday.competition;

import org.bukkit.potion.PotionEffectType;

public class ObstacleCourse extends AbstractTrackCompetition {
    @Override
    public String getID() {
        return "obstacle_course";
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

    @Override
    public int getMaxLaps() {
        return 2;
    }
}
