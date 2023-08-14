package org.macausmp.sportsday.competition;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.macausmp.sportsday.PlayerData;
import org.macausmp.sportsday.SportsDay;
import org.macausmp.sportsday.event.CompetitionEndEvent;
import org.macausmp.sportsday.gui.CompetitionGUI;
import org.macausmp.sportsday.util.ColorTextUtil;
import org.macausmp.sportsday.util.Translation;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public abstract class AbstractCompetition implements ICompetition {
    protected static final SportsDay PLUGIN = SportsDay.getInstance();
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
        this.name = ColorTextUtil.convert(Translation.translatable("competition.name." + id));
        this.least = PLUGIN.getConfig().getInt(id + ".least_players_required");
        this.location = Objects.requireNonNull(PLUGIN.getConfig().getLocation(id + ".location"));
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
        return PLUGIN.getConfig().getBoolean(id + ".enable");
    }

    @Override
    public void setup() {
        COMPETITION_TASKS.forEach(BukkitTask::cancel);
        players.clear();
        getLeaderboard().clear();
        setStage(Stage.COMING);
        players.addAll(Competitions.getOnlinePlayers());
        int i = PLUGIN.getConfig().getInt("ready_time");
        getOnlinePlayers(p -> p.sendMessage(Translation.translatable("competition.start_message").args(name, Component.text(i)).color(NamedTextColor.GREEN)));
        players.forEach(data -> {
            data.getPlayer().setBedSpawnLocation(location, true);
            if (!SportsDay.REFEREE.hasPlayer(data.getPlayer())) {
                data.getPlayer().getInventory().clear();
            }
            data.getPlayer().teleport(location);
            data.getPlayer().setGameMode(GameMode.ADVENTURE);
        });
        addRunnable(new BukkitRunnable() {
            int i = PLUGIN.getConfig().getInt("ready_time");
            @Override
            public void run() {
                if (i % 5 == 0 || (i <= 5 && i > 0)) {
                    getOnlinePlayers(p -> {
                        p.sendActionBar(Translation.translatable("competition.start_countdown").args(Component.text(i)).color(NamedTextColor.GREEN));
                        p.playSound(p, Sound.ENTITY_ARROW_HIT_PLAYER, 1f, 0.5f);
                    });
                }
                if (i-- == 0) {
                    this.cancel();
                    start();
                    getOnlinePlayers(p -> {
                        p.sendActionBar(Translation.translatable("competition.start"));
                        p.playSound(p, Sound.ENTITY_ARROW_HIT_PLAYER, 1f, 1f);
                    });
                }
            }
        }.runTaskTimer(PLUGIN, 0L, 20L));
        onSetup();
        PLUGIN.getComponentLogger().info(Translation.translatable("console.competition.coming").args(name));
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
        getOnlinePlayers(p -> p.showTitle(Title.title(Translation.translatable("competition.end"), Component.text(""))));
        addRunnable(new BukkitRunnable() {
            @Override
            public void run() {
                if (stage == Stage.ENDED) {
                    Competitions.setCurrentlyCompetition(null);
                    setStage(Stage.IDLE);
                    getOnlinePlayers(p -> {
                        p.teleport(location);
                        p.setGameMode(GameMode.ADVENTURE);
                    });
                    Competitions.getOnlinePlayers().forEach(d -> d.getPlayer().getInventory().clear());
                }
            }
        }.runTaskLater(PLUGIN, 100L));
        PLUGIN.getComponentLogger().info(Translation.translatable("console.competition." + (force ? "force_end" : "end")).args(name));
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
     * Get the online players of server and apply action
     */
    protected void getOnlinePlayers(Consumer<? super Player> action) {
        Bukkit.getOnlinePlayers().forEach(action);
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
