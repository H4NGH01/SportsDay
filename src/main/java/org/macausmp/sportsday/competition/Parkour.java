package org.macausmp.sportsday.competition;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.macausmp.sportsday.PlayerData;
import org.macausmp.sportsday.SportsDay;

import java.util.ArrayList;
import java.util.List;

public class Parkour extends AbstractCompetition {
    private final Leaderboard<PlayerData> leaderboard = new Leaderboard<>();
    private boolean ending = false;

    @Override
    public String getID() {
        return "parkour";
    }

    @Override
    public void onSetup() {
        ending = false;
        getPlayerDataList().forEach(data -> {
            if (data.isPlayerOnline()) {
                data.getPlayer().setCollidable(false);
            }
        });
    }

    @Override
    public void onStart() {
        getPlayerDataList().forEach(data -> {
            if (data.isPlayerOnline()) {
                data.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 0, false, false, false));
            }
        });
    }

    @Override
    public void onEnd(boolean force) {
        getPlayerDataList().forEach(data -> {
            if (data.isPlayerOnline()) {
                data.getPlayer().setCollidable(true);
                data.getPlayer().removePotionEffect(PotionEffectType.INVISIBILITY);
            }
        });
        if (force) return;
        List<Component> cl = new ArrayList<>();
        for (int i = 0; i < getLeaderboard().size();) {
            PlayerData data = getLeaderboard().getEntry().get(i++);
            cl.add(Component.translatable("第%s名 %s").args(Component.text(i), Component.text(data.getName())));
            if (i <= 3) {
                data.addScore(4 - i);
            }
            data.addScore(1);
        }
        getOnlinePlayers().forEach(p -> cl.forEach(p::sendMessage));
    }

    private BukkitTask task;

    @Override
    public <T extends Event> void onEvent(T event) {
        if (event instanceof PlayerMoveEvent e) {
            Player player = e.getPlayer();
            if (getLeaderboard().contains(Competitions.getPlayerData(player.getUniqueId()))) return;
            Location loc = player.getLocation().clone();
            loc.setY(loc.getY() - 0.5f);
            CompetitionListener.spawnpoint(player, loc);
            if (loc.getBlock().getType() == FINISH_LINE) {
                getLeaderboard().add(Competitions.getPlayerData(player.getUniqueId()));
                player.removePotionEffect(PotionEffectType.INVISIBILITY);
                player.playSound(player, Sound.ENTITY_ARROW_HIT_PLAYER, 1f, 1f);
                player.setGameMode(GameMode.SPECTATOR);
                getOnlinePlayers().forEach(p -> p.sendMessage(Component.text(player.getName() + "完成了比賽").color(NamedTextColor.YELLOW)));
                if (getLeaderboard().size() == getPlayerDataList().size()) {
                    if (task != null) task.cancel();
                    getOnlinePlayers().forEach(p -> p.sendActionBar(Component.text("所有選手已完成比賽，比賽結束").color(NamedTextColor.YELLOW)));
                    end(false);
                    return;
                }
                if (getLeaderboard().size() >= 3 && !ending) {
                    ending = true;
                    getOnlinePlayers().forEach(p -> p.sendMessage(Component.text("前三名已完成了比賽，比賽將於30秒後結束").color(NamedTextColor.YELLOW)));
                    task = addRunnable(new BukkitRunnable() {
                        int i = 30;
                        @Override
                        public void run() {
                            if (i > 0) {
                                getOnlinePlayers().forEach(p -> p.sendActionBar(Component.text("比賽將於" + i + "秒後結束").color(NamedTextColor.YELLOW)));
                            }
                            if (i-- == 0) {
                                getOnlinePlayers().forEach(p -> p.sendActionBar(Component.text("比賽結束").color(NamedTextColor.YELLOW)));
                                end(false);
                                cancel();
                            }
                        }
                    }.runTaskTimer(SportsDay.getInstance(), 0L, 20L));
                }
            }
        }
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
        }.runTaskLater(SportsDay.getInstance(), 5L));
    }

    @Override
    public Leaderboard<PlayerData> getLeaderboard() {
        return leaderboard;
    }
}
