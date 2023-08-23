package org.macausmp.sportsday.competition;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.event.PlayerFinishCompetitionEvent;

public class Parkour extends AbstractTrackEvent {
    public Parkour() {
        super("parkour");
    }

    @Override
    public void onSetup() {
        getPlayerDataList().forEach(data -> data.getPlayer().setCollidable(false));
    }

    @Override
    public void onStart() {
        Competitions.getOnlinePlayers().forEach(d -> {
            d.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 0, false, false, false));
            d.getPlayer().getInventory().setHelmet(null);
            d.getPlayer().getInventory().setChestplate(null);
            d.getPlayer().getInventory().setLeggings(null);
        });
    }

    @Override
    public void onEnd(boolean force) {
        Competitions.getOnlinePlayers().forEach(d -> {
            d.getPlayer().setCollidable(true);
            d.getPlayer().removePotionEffect(PotionEffectType.INVISIBILITY);
        });
    }

    @EventHandler
    public void onFinished(@NotNull PlayerFinishCompetitionEvent e) {
        if (e.getCompetition() == this) e.getPlayer().removePotionEffect(PotionEffectType.INVISIBILITY);
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {
        if (Competitions.getCurrentlyCompetition() == null || Competitions.getCurrentlyCompetition() != this || getStage() != Stage.STARTED) return;
        Player p = e.getPlayer();
        if (getLeaderboard().contains(Competitions.getPlayerData(p.getUniqueId()))) return;
        addRunnable(new BukkitRunnable() {
            @Override
            public void run() {
                p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 0, false, false, false));
                cancel();
            }
        }.runTaskLater(PLUGIN, 5L));
    }
}
