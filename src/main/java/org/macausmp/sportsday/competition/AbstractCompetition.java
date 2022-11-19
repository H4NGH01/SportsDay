package org.macausmp.sportsday.competition;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.macausmp.sportsday.PlayerData;
import org.macausmp.sportsday.SportsDay;
import org.macausmp.sportsday.event.CompetitionEndEvent;
import org.macausmp.sportsday.gui.CompetitionGUI;
import org.macausmp.sportsday.util.Translation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public abstract class AbstractCompetition implements ICompetition {
    private Stage stage = Stage.IDLE;
    private final int leastPlayersRequired = SportsDay.getInstance().getConfig().getInt(getID() + ".least_players_required");
    private final Location location = SportsDay.getInstance().getConfig().getLocation(getID() + ".location");
    private final World world = Objects.requireNonNull(location).getWorld();
    private static final List<BukkitTask> COMPETITION_TASKS = new ArrayList<>();
    private final List<PlayerData> players = new ArrayList<>();

    @Override
    public Component getName() {
        return Component.text(Objects.requireNonNull(SportsDay.getInstance().getConfig().getString(getID() + ".name")));
    }

    @Override
    public int getLeastPlayersRequired() {
        return leastPlayersRequired;
    }

    @Override
    public Location getLocation() {
        return location;
    }

    @Override
    public World getWorld() {
        return world;
    }

    @Override
    public boolean isEnable() {
        return SportsDay.getInstance().getConfig().getBoolean(getID() + ".enable");
    }

    @Override
    public void setup() {
        COMPETITION_TASKS.forEach(BukkitTask::cancel);
        getPlayerDataList().clear();
        getLeaderboard().clear();
        setStage(Stage.COMING);
        getPlayerDataList().addAll(Competitions.getPlayerDataList());
        getPlayerDataList().removeIf(d -> !d.isPlayerOnline());
        getOnlinePlayers().forEach(p -> p.sendMessage(Translation.translatable("competition.start_in_15sec").args(getName())));
        getPlayerDataList().forEach(data -> {
            data.getPlayer().setBedSpawnLocation(getLocation(), true);
            if (!SportsDay.REFEREE.hasPlayer(data.getPlayer())) {
                data.getPlayer().getInventory().clear();
            }
            data.getPlayer().teleport(getLocation());
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
        Bukkit.getConsoleSender().sendMessage("§a一場" + Objects.requireNonNull(SportsDay.getInstance().getConfig().getString(getID() + ".name")) + "§a即將開始");
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
                if (getStage() == Stage.ENDED) {
                    Competitions.setCurrentlyCompetition(null);
                    if (isEnable()) {
                        setStage(Stage.IDLE);
                    }
                }
            }
        }.runTaskLater(SportsDay.getInstance(), 100L));
        Bukkit.getConsoleSender().sendMessage(Objects.requireNonNull(SportsDay.getInstance().getConfig().getString(getID() + ".name")) + (force ? "§c已強制結束" : "§c已結束"));
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
    public Stage getStage() {
        return stage;
    }

    @Override
    public void setStage(Stage stage) {
        this.stage = stage;
        CompetitionGUI.COMPETITION_INFO_GUI.update();
    }

    /**
     * Get the player data list of competition
     * @return player data list of competition
     */
    public List<PlayerData> getPlayerDataList() {
        return players;
    }

    /**
     * Get the online players of server
     * @return online players of server
     */
    protected Collection<? extends Player> getOnlinePlayers() {
        return Bukkit.getOnlinePlayers();
    }

    /**
     * Add a runnable task to this competition
     * @param task runnable task to add
     */
    protected BukkitTask addRunnable(BukkitTask task) {
        COMPETITION_TASKS.add(task);
        return task;
    }
}
