package org.macausmp.sportsday.competition;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.macausmp.sportsday.PlayerData;
import org.macausmp.sportsday.SportsDay;
import org.macausmp.sportsday.event.CompetitionEndEvent;
import org.macausmp.sportsday.event.CompetitionStartEvent;
import org.macausmp.sportsday.gui.CompetitionGUI;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public abstract class AbstractCompetition implements ICompetition, Listener {
    private Stage stage = Stage.IDLE;
    private final int leastPlayersRequired = SportsDay.getInstance().getConfig().getInt(this.getID() + ".least_players_required");
    private final Location location = SportsDay.getInstance().getConfig().getLocation(this.getID() + ".location");
    private final World world = Objects.requireNonNull(this.location).getWorld();
    protected final List<BukkitTask> runnableList = new ArrayList<>();

    @Override
    public Component getName() {
        return Component.text(Objects.requireNonNull(SportsDay.getInstance().getConfig().getString(this.getID() + ".name")));
    }

    @Override
    public int getLeastPlayersRequired() {
        return this.leastPlayersRequired;
    }

    @Override
    public Location getLocation() {
        return this.location;
    }

    @Override
    public World getWorld() {
        return this.world;
    }

    @Override
    public boolean isEnable() {
        return SportsDay.getInstance().getConfig().getBoolean(this.getID() + ".enable");
    }

    @Override
    public void setup() {
        Bukkit.getConsoleSender().sendMessage(Component.text("§a一場").append(getName()).append(Component.text("§a即將開始")));
        setStage(Stage.COMING);
        getOnlinePlayers().forEach(p -> p.sendMessage(getName().append(Component.text("§a將於15秒後開始"))));
        getPlayerDataList().forEach(data -> {
            if (data.isPlayerOnline()) {
                data.getPlayer().setBedSpawnLocation(getLocation(), true);
                data.getPlayer().getInventory().clear();
                data.getPlayer().teleport(getLocation());
                data.getPlayer().setGameMode(GameMode.ADVENTURE);
            }
        });
        runnableList.add(new BukkitRunnable() {
            int i = 15;
            @Override
            public void run() {
                if (i == 15 || i == 10 || (i <= 5 && i > 0)) {
                    getOnlinePlayers().forEach(p -> p.sendActionBar(Component.text("§e比賽還有§a" + i + "秒§e開始")));
                    if ((i <= 5 && i > 0)) {
                        getOnlinePlayers().forEach(p -> p.playSound(p, Sound.ENTITY_ARROW_HIT_PLAYER, 1f, 0.5f));
                    }
                }
                if (i-- == 0) {
                    this.cancel();
                    start();
                    getOnlinePlayers().forEach(p -> {
                        p.sendActionBar(Component.text("比賽開始").color(NamedTextColor.YELLOW));
                        p.playSound(p, Sound.ENTITY_ARROW_HIT_PLAYER, 1f, 1f);
                    });
                }
            }
        }.runTaskTimer(SportsDay.getInstance(), 0L, 20L));
        onSetup();
    }

    @Override
    public void start() {
        setStage(Stage.STARTED);
        Bukkit.getPluginManager().callEvent(new CompetitionStartEvent(this));
        onStart();
    }

    @Override
    public void end(boolean force) {
        Bukkit.getConsoleSender().sendMessage(getName().append(Component.text(force ? "§c已強制結束" : "§c已結束")));
        setStage(Stage.ENDED);
        Bukkit.getPluginManager().callEvent(new CompetitionEndEvent(this));
        onEnd(force);
        getLeaderboard().clear();
        Competitions.setCurrentlyCompetition(null);
        if (isEnable()) {
            setStage(Stage.IDLE);
            runnableList.forEach(BukkitTask::cancel);
        }
    }

    /**
     * extra event on {@link AbstractCompetition#setup()}
     */
    protected abstract void onSetup();

    /**
     * extra event on {@link AbstractCompetition#start()}
     */
    protected abstract void onStart();

    /**
     * extra event on {@link AbstractCompetition#end(boolean)}}
     */
    protected abstract void onEnd(boolean force);

    @Override
    public Stage getStage() {
        return this.stage;
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
        return Competitions.getPlayerDataList();
    }

    /**
     * Get the online players of server
     * @return online players of server
     */
    protected Collection<? extends Player> getOnlinePlayers() {
        return Bukkit.getOnlinePlayers();
    }
}
