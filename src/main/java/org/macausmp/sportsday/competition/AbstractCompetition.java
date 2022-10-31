package org.macausmp.sportsday.competition;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
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
    private final int leastPlayerRequired = SportsDay.getInstance().getConfig().getInt(this.getID() + ".least_players_required");
    private final Location location = SportsDay.getInstance().getConfig().getLocation(this.getID() + ".location");
    protected final List<BukkitTask> runnableList = new ArrayList<>();

    @Override
    public Component getName() {
        return Component.text(Objects.requireNonNull(SportsDay.getInstance().getConfig().getString(this.getID() + ".name"))).decoration(TextDecoration.ITALIC, false);
    }

    @Override
    public int getLeastPlayerRequired() {
        return this.leastPlayerRequired;
    }

    @Override
    public Location getLocation() {
        return this.location;
    }

    @Override
    public boolean isEnable() {
        return SportsDay.getInstance().getConfig().getBoolean(this.getID() + ".enable");
    }

    @Override
    public void setup() {
        SportsDay.getInstance().getServer().getConsoleSender().sendMessage("§a一場" + SportsDay.getInstance().getConfig().getString(getID() + ".name") + "§a即將開始");
        setStage(Stage.COMING);
        getPlayerDataList().forEach(data -> {
            if (data.isPlayerOnline()) {
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
        SportsDay.getInstance().getServer().getPluginManager().callEvent(new CompetitionStartEvent(this));
        onStart();
    }

    @Override
    public void end(boolean force) {
        setStage(Stage.ENDED);
        SportsDay.getInstance().getServer().getPluginManager().callEvent(new CompetitionEndEvent(this));
        onEnd(force);
        getLeaderboard().getEntry().clear();
        Competitions.setCurrentlyCompetition(null);
    }

    protected abstract void onSetup();

    protected abstract void onStart();

    protected abstract void onEnd(boolean force);

    public void reset() {
        if (isEnable()) {
            setStage(Stage.IDLE);
            runnableList.forEach(BukkitTask::cancel);
        }
    }

    @Override
    public Stage getStage() {
        return this.stage;
    }

    @Override
    public void setStage(Stage stage) {
        this.stage = stage;
        CompetitionGUI.COMPETITION_INFO_GUI.update();
    }

    public List<PlayerData> getPlayerDataList() {
        return Competitions.getPlayerDataList();
    }

    protected Collection<? extends Player> getOnlinePlayers() {
        return SportsDay.getInstance().getServer().getOnlinePlayers();
    }
}
