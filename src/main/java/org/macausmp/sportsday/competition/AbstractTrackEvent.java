package org.macausmp.sportsday.competition;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.util.PlayerData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public abstract class AbstractTrackEvent extends AbstractEvent implements ITrackEvent {
    public static final Material FINISH_LINE = Material.getMaterial(Objects.requireNonNull(PLUGIN.getConfig().getString("finish_line_block")));
    private final List<PlayerData> leaderboard = new ArrayList<>();
    private final HashMap<PlayerData, Integer> lapMap = new HashMap<>();
    private final HashMap<PlayerData, Float> record = new HashMap<>();
    private final int laps;
    private float time = 0f;
    private boolean endCountdown = false;
    private BukkitTask task;

    public AbstractTrackEvent(String id) {
        super(id);
        this.laps = PLUGIN.getConfig().getInt(getID() + ".laps");
    }

    @Override
    public void setup() {
        lapMap.clear();
        record.clear();
        endCountdown = false;
        PLUGIN.getServer().dispatchCommand(Bukkit.getConsoleSender(), Objects.requireNonNull(PLUGIN.getConfig().getString(getID() + ".ready_command")));
        super.setup();
        getPlayerDataList().forEach(data -> lapMap.put(data, 0));
        Bukkit.broadcast(Component.translatable("event.track.laps").args(Component.text(laps)).color(NamedTextColor.GREEN));
    }

    @Override
    public void start() {
        super.start();
        PLUGIN.getServer().dispatchCommand(Bukkit.getConsoleSender(), Objects.requireNonNull(PLUGIN.getConfig().getString(getID() + ".start_command")));
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
        }.runTaskTimer(PLUGIN, 1L, 1L));
    }

    @Override
    public void end(boolean force) {
        super.end(force);
        if (force) return;
        Component c = Component.text().build();
        for (int i = 0; i < leaderboard.size();) {
            PlayerData data = leaderboard.get(i++);
            c = c.append(Component.translatable("event.rank").args(Component.text(i), Component.text(data.getName()), Component.text(record.get(data))));
            if (i < leaderboard.size()) c = c.appendNewline();
            if (i <= 3) data.addScore(4 - i);
            data.addScore(1);
        }
        Bukkit.broadcast(c);
    }

    @Override
    public <T extends Event> void onEvent(T event) {
        if (event instanceof PlayerMoveEvent e) {
            Player p = e.getPlayer();
            PlayerData data = Competitions.getPlayerData(p.getUniqueId());
            if (leaderboard.contains(data) || !lapMap.containsKey(data)) return;
            Location loc = p.getLocation().clone();
            loc.setY(loc.getY() - 0.5f);
            CompetitionListener.spawnpoint(p, loc);
            if (loc.getBlock().getType() == FINISH_LINE) {
                lapMap.put(data, lapMap.get(data) + 1);
                p.playSound(p, Sound.ENTITY_ARROW_HIT_PLAYER, 1f, 1f);
                if (lapMap.get(data) < laps) {
                    p.setBedSpawnLocation(getLocation(), true);
                    p.teleport(getLocation());
                    onCompletedLap(p);
                    Bukkit.broadcast(Component.translatable("event.player_finished_lap").args(p.displayName()).color(NamedTextColor.YELLOW));
                } else {
                    record.put(data, time / 20f);
                    leaderboard.add(Competitions.getPlayerData(p.getUniqueId()));
                    p.setGameMode(GameMode.SPECTATOR);
                    onRaceFinish(p);
                    Bukkit.broadcast(Component.translatable("event.player_finished").args(p.displayName(), Component.text(record.get(data))).color(NamedTextColor.YELLOW));
                    if (leaderboard.size() == getPlayerDataList().size()) {
                        if (task != null && !task.isCancelled()) task.cancel();
                        PLUGIN.getServer().sendActionBar(Component.translatable("event.all_player_finished"));
                        end(false);
                        return;
                    }
                    if (leaderboard.size() >= 3 && !endCountdown) {
                        endCountdown = true;
                        Bukkit.broadcast(Component.translatable("event.third_player_finished").args(Component.text(PLUGIN.getConfig().getInt("event_end_countdown"))));
                        task = addRunnable(new BukkitRunnable() {
                            int i = PLUGIN.getConfig().getInt("event_end_countdown");
                            @Override
                            public void run() {
                                if (i > 0) PLUGIN.getServer().sendActionBar(Component.translatable("event.end_countdown").args(Component.text(i)).color(NamedTextColor.GREEN));
                                if (i-- == 0) {
                                    PLUGIN.getServer().sendActionBar(Component.translatable("event.ended_message"));
                                    end(false);
                                    cancel();
                                }
                            }
                        }.runTaskTimer(PLUGIN, 0L, 20L));
                    }
                }
            }
        }
    }

    protected void onCompletedLap(@NotNull Player player) {
    }

    protected void onRaceFinish(@NotNull Player player) {
    }

    @Override
    public int getMaxLaps() {
        return laps;
    }

    @Override
    public final List<PlayerData> getLeaderboard() {
        return leaderboard;
    }
}
