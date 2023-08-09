package org.macausmp.sportsday.competition;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.SportsDay;
import org.macausmp.sportsday.command.CompetitionGUICommand;
import org.macausmp.sportsday.competition.sumo.Sumo;
import org.macausmp.sportsday.competition.sumo.SumoRound;
import org.macausmp.sportsday.gui.CompetitionGUI;
import org.macausmp.sportsday.util.Translation;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class CompetitionListener implements Listener {
    private static final List<UUID> SPAWNPOINT_LIST = new ArrayList<>();
    public static final Material CHECKPOINT = Material.getMaterial(Objects.requireNonNull(SportsDay.getInstance().getConfig().getString("checkpoint_block")));
    public static final Material DEATH = Material.getMaterial(Objects.requireNonNull(SportsDay.getInstance().getConfig().getString("death_block")));

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        if (Competitions.getCurrentlyCompetition() == null || Competitions.getCurrentlyCompetition().getStage() != Stage.STARTED) return;
        Player p = e.getPlayer();
        if (p.getGameMode() != GameMode.ADVENTURE || !Competitions.containPlayer(p)) return;
        Competitions.getCurrentlyCompetition().onEvent(e);
        Location loc = e.getTo().clone();
        loc.setY(loc.getY() - 0.5f);
        if (SPAWNPOINT_LIST.contains(p.getUniqueId()) && loc.getWorld().getBlockAt(loc).getType() != CHECKPOINT) {
            SPAWNPOINT_LIST.remove(p.getUniqueId());
        }
        if (loc.getWorld().getBlockAt(loc).getType() == DEATH) {
            p.setHealth(0);
        }
    }

    public static void spawnpoint(@NotNull Player player, @NotNull Location loc) {
        if (!SPAWNPOINT_LIST.contains(player.getUniqueId()) && loc.getWorld().getBlockAt(loc).getType() == CHECKPOINT) {
            SPAWNPOINT_LIST.add(player.getUniqueId());
            player.setBedSpawnLocation(player.getLocation(), true);
            player.playSound(player, Sound.ENTITY_ARROW_HIT_PLAYER, 1f, 1f);
            player.sendActionBar(Component.text("Checkpoint").color(NamedTextColor.GREEN).decoration(TextDecoration.BOLD, true));
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
            if (Competitions.getCurrentlyCompetition() == null || !Competitions.containPlayer(player) || Competitions.getCurrentlyCompetition().getStage() == Stage.STARTED) return;
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlace(@NotNull BlockPlaceEvent e) {
        if (e.getPlayer().getGameMode() == GameMode.CREATIVE && !SportsDay.AUDIENCE.hasPlayer(e.getPlayer())) return;
        e.setCancelled(true);
    }

    @EventHandler
    public void onBreak(@NotNull BlockBreakEvent e) {
        if (e.getPlayer().getGameMode() == GameMode.CREATIVE && !SportsDay.AUDIENCE.hasPlayer(e.getPlayer())) return;
        e.setCancelled(true);
    }

    private static final List<UUID> EASTER_TRIGGER = new ArrayList<>();

    @EventHandler
    public void onOpenBook(@NotNull PlayerInteractEvent e) {
        if (e.getItem() != null && e.getItem().equals(CompetitionGUICommand.book())) {
            e.setCancelled(true);
            Player p = e.getPlayer();
            if (p.isOp()) {
                CompetitionGUI.MENU_GUI.openTo(e.getPlayer());
                return;
            }
            // Easter egg, happen if player use the book without op permission
            if (EASTER_TRIGGER.contains(p.getUniqueId())) return;
            EASTER_TRIGGER.add(p.getUniqueId());
            new BukkitRunnable() {
                int i = 0;
                @Override
                public void run() {
                    if (i >= 3 && i < 10) {
                        p.spawnParticle(Particle.BLOCK_CRACK, p.getLocation(), 20, 0.2, 0.5, 0.2, Material.REDSTONE_BLOCK.createBlockData());
                    }
                    if (i == 0) {
                        p.damage(5);
                        p.sendMessage(Translation.translatable("item.op_book_easter_egg1").args(e.getItem().displayName()));
                    } else if (i == 6) {
                        p.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 40, 2, false, false));
                    } else if (i == 8) {
                        p.sendMessage(Translation.translatable("item.op_book_easter_egg2"));
                    } if (i == 10) {
                        p.getWorld().setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, false);
                        p.setHealth(0);
                        p.getWorld().setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, true);
                        EASTER_TRIGGER.remove(p.getUniqueId());
                        cancel();
                    }
                    i++;
                }
            }.runTaskTimer(SportsDay.getInstance(), 0L, 10L);
        }
    }
}
