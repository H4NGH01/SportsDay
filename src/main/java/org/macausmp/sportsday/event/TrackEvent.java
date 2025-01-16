package org.macausmp.sportsday.event;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.macausmp.sportsday.ContestantData;
import org.macausmp.sportsday.SportsDay;
import org.macausmp.sportsday.gui.event.TrackEventGUI;
import org.macausmp.sportsday.sport.Sport;
import org.macausmp.sportsday.venue.Track;
import org.macausmp.sportsday.venue.TrackPoint;

import java.util.HashMap;
import java.util.Objects;
import java.util.Optional;

public abstract class TrackEvent extends SportingEvent {
    private final int laps;
    private final boolean checkpoint;
    private final HashMap<ContestantData, Integer> lapMap = new HashMap<>();
    private final HashMap<ContestantData, Integer> checkpointMap = new HashMap<>();
    private final HashMap<ContestantData, Float> record = new HashMap<>();
    private int time = 0;
    private BukkitTask task = null;

    public TrackEvent(@NotNull Sport sport, @NotNull Track track, @Nullable PersistentDataContainer save) {
        super(sport, track, save);
        if (save == null) {
            this.laps = sport.getSetting(Sport.TrackSettings.LAPS);
            this.checkpoint = sport.getSetting(Sport.TrackSettings.ALL_CHECKPOINTS_REQUIRED);
        } else {
            this.laps = Objects.requireNonNull(save.get(new NamespacedKey(PLUGIN, "laps"), PersistentDataType.INTEGER));
            this.checkpoint = Boolean.TRUE.equals(save.get(new NamespacedKey(PLUGIN, "checkpoint"), PersistentDataType.BOOLEAN));
        }
    }

    @Override
    public void save(@NotNull PersistentDataContainer data) {
        super.save(data);
        data.set(new NamespacedKey(PLUGIN, "laps"), PersistentDataType.INTEGER, laps);
        data.set(new NamespacedKey(PLUGIN, "checkpoint"), PersistentDataType.BOOLEAN, checkpoint);
    }

    @Override
    public Track getVenue() {
        return (Track) super.getVenue();
    }

    @Override
    public TrackEventGUI<? extends TrackEvent> getEventGUI() {
        return new TrackEventGUI<>(this);
    }

    /**
     * Get the number of laps required to complete.
     *
     * @return number of laps required to complete
     */
    public final int getLaps() {
        return laps;
    }

    /**
     * Return {@code True} if all checkpoints are required.
     *
     * @return {@code True} if all checkpoints are required
     */
    public final boolean areAllCheckpointsRequired() {
        return checkpoint;
    }

    /**
     * Get the record of a specified contestant.
     *
     * @return record of a specified contestant
     */
    public float getRecord(ContestantData data) {
        return Optional.ofNullable(record.get(data)).orElse(-1f);
    }

    @Override
    public void start() {
        if (getStatus() == EventStatus.PROCESSING)
            return;
        super.start();
        onEventStart();
        PLUGIN.getServer().dispatchCommand(Bukkit.getConsoleSender(), getSports().getSetting(Sport.TrackSettings.READY_COMMAND));
        getContestants().forEach(data -> lapMap.put(data, 0));
        getContestants().forEach(data -> checkpointMap.put(data, 0));
        Bukkit.broadcast(Component.translatable("event.track.laps").arguments(Component.text(laps)).color(NamedTextColor.GREEN));
    }

    @Override
    public boolean pause(@NotNull CommandSender sender) {
        if (getStatus() == EventStatus.UPCOMING) {
            return super.pause(sender);
        } else {
            sender.sendMessage(Component.translatable("command.competition.pause.failed").color(NamedTextColor.RED));
            return false;
        }
    }

    @Override
    protected void onStart() {
        PLUGIN.getServer().dispatchCommand(Bukkit.getConsoleSender(), getSports().getSetting(Sport.TrackSettings.START_COMMAND));
        addTask(new BukkitRunnable() {
            @Override
            public void run() {
                ++time;
            }
        }.runTaskTimer(PLUGIN, 0L, 1L));
        onRacingStart();
    }

    @Override
    protected void onEnd() {
        TranslatableComponent.Builder builder = Component.translatable("event.result").toBuilder();
        for (int i = 0; i < getLeaderboard().size();) {
            ContestantData data = getLeaderboard().get(i);
            builder.appendNewline().append(Component.translatable("event.track.rank")
                    .arguments(Component.text(++i), Component.text(data.getName()), Component.text(record.get(data))));
            if (i <= 3)
                data.addScore(4 - i);
            data.addScore(1);
        }
        Bukkit.broadcast(builder.build());
    }

    @Override
    public void onLeave(ContestantData contestant) {
        if (getLeaderboard().size() == getContestants().size()) {
            if (task != null && !task.isCancelled())
                task.cancel();
            end();
            return;
        }
        super.onLeave(contestant);
    }

    protected void onEventStart() {}

    protected void onRacingStart() {}

    /**
     * Called when a player completed a lap.
     */
    protected void onCompletedLap(@NotNull Player player) {}

    /**
     * Called when a player finished whole race.
     */
    protected void onRaceFinish(@NotNull Player player) {}

    @EventHandler
    public void onMove(@NotNull PlayerMoveEvent e) {
        Player p = e.getPlayer();
        if (!predicate.test(p) || getStatus() != EventStatus.PROCESSING)
            return;
        ContestantData data = SportsDay.getContestant(p.getUniqueId());
        int next = checkpointMap.get(data);
        if (next < getVenue().getCheckPoints().size()) {
            if (checkpoint)
                for (int i = next + 1; i < getVenue().getCheckPoints().size(); i++) {
                    if (getVenue().getCheckPoints().get(i).overlaps(p)) {
                        p.teleportAsync((next > 1 ? getVenue().getCheckPoints().get(next - 1) : getVenue().getStartPoint())
                                .getLocation());
                        p.playSound(Sound.sound(Key.key("minecraft:entity.enderman.teleport"),
                                Sound.Source.MASTER, 1f, 1f));
                        p.sendActionBar(Component.translatable("event.track.checkpoint_missed")
                                .color(NamedTextColor.RED));
                        break;
                    }
                }
            TrackPoint point = getVenue().getCheckPoints().get(next);
            if (point.overlaps(p)) {
                p.setRespawnLocation(p.getLocation(), true);
                checkpointMap.put(data, ++next);
                p.playSound(Sound.sound(Key.key("minecraft:entity.arrow.hit_player"),
                        Sound.Source.MASTER, 1f, 1f));
                p.sendActionBar(Component.translatable("event.track.checkpoint")
                        .arguments(Component.text(next)).color(NamedTextColor.GREEN));
            }
        }
        if (getVenue().getEndPoint().overlaps(p) && next == getVenue().getCheckPoints().size()) {
            int lap = lapMap.get(data) + 1;
            lapMap.put(data, lap);
            checkpointMap.put(data, 0);
            if (lap < getLaps()) {
                onCompletedLap(p);
                Bukkit.broadcast(Component.translatable("event.track.contestant.completed_lap")
                        .arguments(p.displayName(), Component.text(lap)).color(NamedTextColor.YELLOW));
                TrackEventGUI.updateGUI();
            } else {
                onRaceFinish(p);
                record.put(data, time / 20f);
                getLeaderboard().add(data);
                p.setGameMode(GameMode.SPECTATOR);
                Bukkit.broadcast(Component.translatable("event.track.contestant.completed_all")
                        .arguments(p.displayName(), Component.text(record.get(data))).color(NamedTextColor.YELLOW));
                TrackEventGUI.updateGUI();

                if (getLeaderboard().size() == getContestants().size()) {
                    if (task != null && !task.isCancelled())
                        task.cancel();
                    PLUGIN.getServer().sendActionBar(Component.translatable("event.track.end.all_completed"));
                    end();
                    return;
                }
                if (getLeaderboard().size() >= 3 && task == null) {
                    final int i1 = PLUGIN.getConfig().getInt("event_end_countdown");
                    Bukkit.broadcast(Component.translatable("event.track.end.countdown.notice")
                            .arguments(Component.text(i1)));
                    task = addTask(new BukkitRunnable() {
                        int i = i1;

                        @Override
                        public void run() {
                            if (i > 0)
                                PLUGIN.getServer().sendActionBar(Component.translatable("event.track.end.countdown")
                                        .arguments(Component.text(i)).color(NamedTextColor.GREEN));
                            if (i-- == 0) {
                                PLUGIN.getServer().sendActionBar(Component.translatable("event.track.end.countdown_end"));
                                end();
                                cancel();
                            }
                        }
                    }.runTaskTimer(PLUGIN, 0L, 20L));
                }
            }
        }
    }
}
