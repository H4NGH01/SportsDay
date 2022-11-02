package org.macausmp.sportsday.competition;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.SportsDay;
import org.macausmp.sportsday.competition.sumo.Sumo;
import org.macausmp.sportsday.competition.sumo.SumoRound;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class CompetitionListener implements Listener {
    private static final List<UUID> SPAWNPOINT_LIST = new ArrayList<>();
    public static final Material FINISH_LINE = Material.getMaterial(Objects.requireNonNull(SportsDay.getInstance().getConfig().getString("finish_line_block")));
    public static final Material CHECKPOINT = Material.getMaterial(Objects.requireNonNull(SportsDay.getInstance().getConfig().getString("checkpoint_block")));
    public static final Material DEATH = Material.getMaterial(Objects.requireNonNull(SportsDay.getInstance().getConfig().getString("death_block")));

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        if (Competitions.getCurrentlyCompetition() == null || Competitions.getCurrentlyCompetition().getStage() != ICompetition.Stage.STARTED) return;
        Player p = e.getPlayer();
        if (p.getGameMode() != GameMode.ADVENTURE || !Competitions.containPlayer(p)) return;
        Location loc = e.getTo().clone();
        loc.setY(loc.getY() - 0.5f);
        Competitions.getCurrentlyCompetition().onEvent(e);
        if (SPAWNPOINT_LIST.contains(p.getUniqueId()) && loc.getWorld().getBlockAt(loc).getType() != CHECKPOINT) {
            SPAWNPOINT_LIST.remove(p.getUniqueId());
        }
        if (loc.getWorld().getBlockAt(loc).getType() == DEATH) {
            p.performCommand("kill");
        }
    }

    public static void spawnpoint(@NotNull Player player, @NotNull Location loc) {
        if (!SPAWNPOINT_LIST.contains(player.getUniqueId()) && loc.getWorld().getBlockAt(loc).getType() == CHECKPOINT) {
            player.performCommand("spawnpoint");
            player.setBedSpawnLocation(player.getLocation(), true);
            player.playSound(player, Sound.ENTITY_ARROW_HIT_PLAYER, 1f, 1f);
            SPAWNPOINT_LIST.add(player.getUniqueId());
        }
    }

    @EventHandler
    public void onHit(@NotNull EntityDamageByEntityEvent e) {
        if (e.getEntity() instanceof Player player && e.getDamager() instanceof Player damager) {
            if (Competitions.getCurrentlyCompetition() != null && Competitions.getCurrentlyCompetition() == Competitions.SUMO) {
                SumoRound sumo = ((Sumo) Competitions.getCurrentlyCompetition()).getSumoStage().getCurrentRound();
                if (sumo != null && sumo.getStatus() == SumoRound.RoundStatus.STARTED && sumo.containPlayer(player) && sumo.containPlayer(damager)) {
                    e.setDamage(0);
                } else {
                    e.setCancelled(true);
                }
                return;
            }
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onDamage(@NotNull EntityDamageEvent e) {
        if (e.getEntity() instanceof Player player) {
            if (Competitions.getCurrentlyCompetition() == null || !Competitions.containPlayer(player) || Competitions.getCurrentlyCompetition().getStage() == ICompetition.Stage.STARTED) return;
            e.setCancelled(true);
        }
    }
}
