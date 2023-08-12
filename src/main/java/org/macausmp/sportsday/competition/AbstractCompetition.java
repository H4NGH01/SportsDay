package org.macausmp.sportsday.competition;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.PlayerData;
import org.macausmp.sportsday.SportsDay;
import org.macausmp.sportsday.event.CompetitionEndEvent;
import org.macausmp.sportsday.gui.CompetitionGUI;
import org.macausmp.sportsday.util.ColorTextUtil;
import org.macausmp.sportsday.util.Translation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public abstract class AbstractCompetition implements ICompetition {
    private static final List<BukkitTask> COMPETITION_TASKS = new ArrayList<>();
    private final String id;
    private final Component name;
    private final int least;
    private final Location location;
    private final World world;
    private Stage stage = Stage.IDLE;
    private final List<PlayerData> players = new ArrayList<>();

    public AbstractCompetition(String id) {
        this.id = id;
<<<<<<< HEAD
        this.name = ColorTextUtil.convert(Translation.translatable("competition.name." + id));
        this.least = SportsDay.getInstance().getConfig().getInt(id + ".least_players_required");
        this.location = Objects.requireNonNull(SportsDay.getInstance().getConfig().getLocation(id + ".location"));
=======
        this.name = ColorTextUtil.convert(Objects.requireNonNull(SportsDay.getInstance().getLanguageConfig().getString("competition.name." + getID())));
        this.least = SportsDay.getInstance().getConfig().getInt(getID() + ".least_players_required");
        this.location = Objects.requireNonNull(SportsDay.getInstance().getConfig().getLocation(getID() + ".location"));
>>>>>>> dcdbd7911177a6fc22c18a4a0094c1c4aa88d1ad
        this.world = location.getWorld();
    }

    @Override
    public final String getID() {
        return id;
    }

    @Override
    public final Component getName() {
        return name;
    }

    @Override
    public final int getLeastPlayersRequired() {
        return least;
    }

    @Override
    public final Location getLocation() {
        return location;
    }

    @Override
    public final World getWorld() {
        return world;
    }

    @Override
    public final boolean isEnable() {
        return SportsDay.getInstance().getConfig().getBoolean(id + ".enable");
    }

    @Override
    public void setup() {
        COMPETITION_TASKS.forEach(BukkitTask::cancel);
        players.clear();
        getLeaderboard().clear();
        setStage(Stage.COMING);
        players.addAll(Competitions.getPlayerDataList());
        players.removeIf(d -> !d.isPlayerOnline());
        getOnlinePlayers().forEach(p -> p.sendMessage(Translation.translatable("competition.start_in_15sec").args(name)));
        players.forEach(data -> {
            data.getPlayer().setBedSpawnLocation(location, true);
            if (!SportsDay.REFEREE.hasPlayer(data.getPlayer())) {
                data.getPlayer().getInventory().clear();
            }
            data.getPlayer().teleport(location);
            data.getPlayer().setGameMode(GameMode.ADVENTURE);
        });
        addRunnable(new BukkitRunnable() {
            int i = 15;
            @Override
            public void run() {
                if (i == 15 || i == 10 || (i <= 5 && i > 0)) {
                    getOnlinePlayers().forEach(p -> {
                        p.sendActionBar(Translation.translatable("competition.start_countdown").args(Component.text(i)).color(NamedTextColor.GREEN));
                        p.playSound(p, Sound.ENTITY_ARROW_HIT_PLAYER, 1f, 0.5f);
                    });
                }
                if (i-- == 0) {
                    this.cancel();
                    start();
                    getOnlinePlayers().forEach(p -> {
                        p.sendActionBar(Translation.translatable("competition.start"));
                        p.playSound(p, Sound.ENTITY_ARROW_HIT_PLAYER, 1f, 1f);
                    });
                }
            }
        }.runTaskTimer(SportsDay.getInstance(), 0L, 20L));
        onSetup();
<<<<<<< HEAD
        SportsDay.getInstance().getComponentLogger().info(Translation.translatable("console.competition.coming").args(name));
=======
        SportsDay.getInstance().getComponentLogger().info(Translation.translatable("console.competition.coming").args(getName()));
>>>>>>> dcdbd7911177a6fc22c18a4a0094c1c4aa88d1ad
    }

    @Override
    public void start() {
        setStage(Stage.STARTED);
        onStart();
    }

    @Override
    public void end(boolean force) {
        setStage(Stage.ENDED);
        Bukkit.getPluginManager().callEvent(new CompetitionEndEvent(this, force));
        onEnd(force);
        COMPETITION_TASKS.forEach(BukkitTask::cancel);
        addRunnable(new BukkitRunnable() {
            @Override
            public void run() {
                if (stage == Stage.ENDED) {
                    Competitions.setCurrentlyCompetition(null);
                    setStage(Stage.IDLE);
                    getOnlinePlayers().forEach(p -> {
                        p.teleport(location);
                        p.setGameMode(GameMode.ADVENTURE);
                    });
                    getPlayerDataList().forEach(d -> {
                        if (d.isPlayerOnline()) d.getPlayer().getInventory().clear();
                    });
                }
            }
        }.runTaskLater(SportsDay.getInstance(), 100L));
<<<<<<< HEAD
        SportsDay.getInstance().getComponentLogger().info(Translation.translatable("console.competition." + (force ? "force_end" : "end")).args(name));
=======
        SportsDay.getInstance().getComponentLogger().info(Translation.translatable("console.competition." + (force ? "force_end" : "end")).args(getName()));
>>>>>>> dcdbd7911177a6fc22c18a4a0094c1c4aa88d1ad
    }

    /**
     * extra event on {@link ICompetition#setup()}
     */
    protected abstract void onSetup();

    /**
     * extra event on {@link ICompetition#start()}
     */
    protected abstract void onStart();

    /**
     * extra event on {@link ICompetition#end(boolean)}}
     */
    protected abstract void onEnd(boolean force);

    @Override
    public final Stage getStage() {
        return stage;
    }

    @Override
    public void setStage(Stage stage) {
        this.stage = stage;
        CompetitionGUI.COMPETITION_INFO_GUI.update();
    }

    /**
     * Get the {@link PlayerData} list of competition
     * @return {@link PlayerData} list of competition
     */
    public final List<PlayerData> getPlayerDataList() {
        return players;
    }

    /**
     * Get the online players of server
     * @return online players of server
     */
    protected final @NotNull Collection<? extends Player> getOnlinePlayers() {
        return Bukkit.getOnlinePlayers();
    }

    /**
     * Add a {@link BukkitTask} to this competition
     * @param task {@link BukkitTask} to add
     */
    protected BukkitTask addRunnable(BukkitTask task) {
        COMPETITION_TASKS.add(task);
        return task;
    }
}
