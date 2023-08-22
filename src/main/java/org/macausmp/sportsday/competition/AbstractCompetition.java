package org.macausmp.sportsday.competition;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.TitlePart;
import org.bukkit.*;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.macausmp.sportsday.SportsDay;
import org.macausmp.sportsday.event.CompetitionEndEvent;
import org.macausmp.sportsday.gui.GUIManager;
import org.macausmp.sportsday.util.CustomizeMusickit;
import org.macausmp.sportsday.util.PlayerCustomize;
import org.macausmp.sportsday.util.PlayerData;
import org.macausmp.sportsday.util.TextUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
        this.name = TextUtil.convert(Component.translatable("competition.name." + id));
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
        COMPETITION_TASKS.clear();
        players.clear();
        getLeaderboard().clear();
        setStage(Stage.COMING);
        players.addAll(Competitions.getOnlinePlayers());
        players.forEach(data -> {
            Player p = data.getPlayer();
            p.setBedSpawnLocation(location, true);
            if (!SportsDay.REFEREE.hasPlayer(p)) p.getInventory().clear();
            p.clearActivePotionEffects();
            p.setFireTicks(0);
            p.teleport(location);
            p.setGameMode(GameMode.ADVENTURE);
            PlayerCustomize.suitUp(p);
            p.getInventory().setItem(4, CompetitionListener.SPRAY);
        });
        addRunnable(new BukkitRunnable() {
            int i = PLUGIN.getConfig().getInt("ready_time");
            @Override
            public void run() {
                if (i % 5 == 0 || (i <= 5 && i > 0)) {
                    Bukkit.getServer().sendActionBar(Component.translatable("competition.start_countdown").args(Component.text(i)).color(NamedTextColor.GREEN));
                    Bukkit.getServer().playSound(Sound.sound(Key.key("minecraft:entity.arrow.hit_player"), Sound.Source.MASTER,  1f, 0.5f));
                }
                if (i-- == 0) {
                    this.cancel();
                    start();
                }
            }
        }.runTaskTimer(PLUGIN, 0L, 20L));
        Bukkit.broadcast(Component.translatable("competition.start_message").args(name, Component.text(PLUGIN.getConfig().getInt("ready_time"))).color(NamedTextColor.GREEN));
        Bukkit.broadcast(Component.translatable("competition.rule." + id));
        onSetup();
        PLUGIN.getComponentLogger().info(Component.translatable("console.competition.coming").args(name));
    }

    @Override
    public void start() {
        setStage(Stage.STARTED);
        onStart();
        Bukkit.getServer().sendActionBar(Component.translatable("competition.start"));
        Bukkit.getServer().playSound(Sound.sound(Key.key("minecraft:entity.arrow.hit_player"), Sound.Source.MASTER, 1f, 1f));
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
                    Bukkit.getOnlinePlayers().forEach(p -> {
                        p.teleport(location);
                        p.setGameMode(GameMode.ADVENTURE);
                    });
                    Competitions.getOnlinePlayers().forEach(d -> {
                        d.getPlayer().getInventory().clear();
                        PlayerCustomize.suitUp(d.getPlayer());
                    });
                    getWorld().getEntitiesByClass(ItemFrame.class).forEach(e -> {
                        if (e.getPersistentDataContainer().has(CompetitionListener.GRAFFITI)) e.remove();
                    });
                }
            }
        }.runTaskLater(PLUGIN, 100L));
        OfflinePlayer mvp = Bukkit.getOfflinePlayer(getLeaderboard().get(0).getUUID());
        Bukkit.getServer().sendActionBar(Component.translatable("MVP: %s").args(Component.text(Objects.requireNonNull(mvp.getName()))).color(NamedTextColor.GOLD));
        CustomizeMusickit musickit = PlayerCustomize.getMusickit(mvp);
        if (musickit != null) {
            Bukkit.getServer().playSound(Sound.sound(musickit.getKey(), Sound.Source.MASTER, 1f, 1f));
        }
        Bukkit.getServer().sendTitlePart(TitlePart.TITLE, Component.translatable("competition.end"));
        PLUGIN.getComponentLogger().info(Component.translatable(force ? "console.competition.force_end" : "console.competition.end").args(name));
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

    /**
     * Set the current competition stage
     * @param stage new stage
     */
    protected void setStage(Stage stage) {
        this.stage = stage;
        GUIManager.COMPETITION_INFO_GUI.update();
    }

    /**
     * Get the list of {@link PlayerData} of current competition.
     * @return list of {@link PlayerData} of current competition
     */
    public final List<PlayerData> getPlayerDataList() {
        return players;
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
