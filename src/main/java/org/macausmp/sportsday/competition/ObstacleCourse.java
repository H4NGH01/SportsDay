package org.macausmp.sportsday.competition;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.macausmp.sportsday.PlayerData;
import org.macausmp.sportsday.SportsDay;

import java.util.HashMap;

public class ObstacleCourse extends AbstractCompetition {
    private final Leaderboard<PlayerData> leaderboard = new Leaderboard<>();
    private final HashMap<PlayerData, Integer> lapMap = new HashMap<>();
    private boolean ending = false;

    @Override
    public String getID() {
        return "obstacle_course";
    }

    @Override
    public void onSetup() {
        ending = false;
        getPlayerDataList().forEach(data -> {
            if (data.isPlayerOnline()) {
                lapMap.put(data, 0);
                data.getPlayer().setCollidable(false);
            }
        });
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
        if (force) return;
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (PlayerData data : getLeaderboard().getEntry()) {
            sb.append("第").append(++i).append("名 ").append(data.getName()).append("\n");
            if (i <= 3) {
                data.addScore(4 - i);
            }
            data.addScore(1);
        }
        getOnlinePlayers().forEach(p -> p.sendMessage(sb.substring(0, sb.length() - 1)));
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
                PlayerData data = Competitions.getPlayerData(player.getUniqueId());
                lapMap.put(data, lapMap.get(data) + 1);
                player.playSound(player, Sound.ENTITY_ARROW_HIT_PLAYER, 1f, 1f);
                player.teleport(getLocation());
                if (lapMap.get(data) == 1) {
                    getOnlinePlayers().forEach(p -> p.sendMessage(Component.text(player.getName() + "完成了第一圈").color(NamedTextColor.YELLOW)));
                } else if (lapMap.get(data) >= 2) {
                    getLeaderboard().add(Competitions.getPlayerData(player.getUniqueId()));
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
    }

    @Override
    public Leaderboard<PlayerData> getLeaderboard() {
        return leaderboard;
    }
}
