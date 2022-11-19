package org.macausmp.sportsday.competition;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.macausmp.sportsday.PlayerData;
import org.macausmp.sportsday.SportsDay;
import org.macausmp.sportsday.event.PlayerFinishCompetitionEvent;
import org.macausmp.sportsday.event.PlayerFinishLapEvent;
import org.macausmp.sportsday.util.Translation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public abstract class AbstractTrackCompetition extends AbstractCompetition implements ITrackCompetition {
    private static final Material FINISH_LINE = Material.getMaterial(Objects.requireNonNull(SportsDay.getInstance().getConfig().getString("finish_line_block")));
    private final Leaderboard<PlayerData> leaderboard = new Leaderboard<>();
    private final HashMap<PlayerData, Integer> lapMap = new HashMap<>();
    private final HashMap<PlayerData, Float> record = new HashMap<>();
    private float time = 0f;
    private boolean endCountdown = false;
    private BukkitTask task;

    @Override
    public void setup() {
        lapMap.clear();
        record.clear();
        endCountdown = false;
        super.setup();
        getPlayerDataList().forEach(data -> lapMap.put(data, 0));
    }

    @Override
    public void start() {
        super.start();
        time = 0f;
        addRunnable(new BukkitRunnable() {
            @Override
            public void run() {
                if (getStage() != Stage.STARTED) {
                    cancel();
                    return;
                }
                time++;
            }
        }.runTaskTimer(SportsDay.getInstance(), 1L, 1L));
    }

    @Override
    public void end(boolean force) {
        super.end(force);
        if (force) return;
        List<Component> cl = new ArrayList<>();
        for (int i = 0; i < getLeaderboard().size();) {
            PlayerData data = getLeaderboard().get(i++);
            cl.add(Translation.translatable("competition.rank").args(Component.text(i), Component.text(data.getName()), Component.text(record.get(data))));
            if (i <= 3) {
                data.addScore(4 - i);
            }
            data.addScore(1);
        }
        getOnlinePlayers().forEach(p -> cl.forEach(p::sendMessage));
    }

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
                if (lapMap.get(data) < getMaxLaps()) {
                    Bukkit.getPluginManager().callEvent(new PlayerFinishLapEvent(player, this));
                    getOnlinePlayers().forEach(p -> p.sendMessage(Translation.translatable("competition.player_finished_lap").args(player.displayName()).color(NamedTextColor.YELLOW)));
                } else {
                    record.put(data, time / 20f);
                    getLeaderboard().add(Competitions.getPlayerData(player.getUniqueId()));
                    player.setGameMode(GameMode.SPECTATOR);
                    Bukkit.getPluginManager().callEvent(new PlayerFinishCompetitionEvent(player, this));
                    getOnlinePlayers().forEach(p -> p.sendMessage(Translation.translatable("competition.player_finished").args(player.displayName(), Component.text(record.get(data))).color(NamedTextColor.YELLOW)));
                    if (getLeaderboard().size() == getPlayerDataList().size()) {
                        if (task != null && !task.isCancelled()) task.cancel();
                        getOnlinePlayers().forEach(p -> p.sendActionBar(Translation.translatable("competition.all_player_finished")));
                        end(false);
                        return;
                    }
                    if (getLeaderboard().size() >= 3 && !endCountdown) {
                        endCountdown = true;
                        getOnlinePlayers().forEach(p -> p.sendMessage(Translation.translatable("competition.third_player_finished")));
                        task = addRunnable(new BukkitRunnable() {
                            int i = 30;
                            @Override
                            public void run() {
                                if (i > 0) {
                                    getOnlinePlayers().forEach(p -> p.sendActionBar(Translation.translatable("competition.end_countdown").args(Component.text(i)).color(NamedTextColor.GREEN)));
                                }
                                if (i-- == 0) {
                                    getOnlinePlayers().forEach(p -> p.sendActionBar(Translation.translatable("competition.end")));
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
