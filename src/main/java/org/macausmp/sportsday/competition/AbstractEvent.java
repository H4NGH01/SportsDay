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
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.SportsDay;
import org.macausmp.sportsday.gui.GUIManager;
import org.macausmp.sportsday.util.CustomizeMusickit;
import org.macausmp.sportsday.util.PlayerCustomize;
import org.macausmp.sportsday.util.PlayerData;
import org.macausmp.sportsday.util.TextUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public abstract class AbstractEvent implements IEvent {
    protected static final SportsDay PLUGIN = SportsDay.getInstance();
    private static final List<BukkitTask> EVENT_TASKS = new ArrayList<>();
    private final String id;
    private final Component name;
    private final int least;
    private final Location location;
    private final World world;
    private Stage stage = Stage.IDLE;
    private final List<PlayerData> players = new ArrayList<>();

    public AbstractEvent(String id) {
        this.id = id;
        this.name = TextUtil.convert(Component.translatable("event.name." + id));
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
        EVENT_TASKS.forEach(BukkitTask::cancel);
        EVENT_TASKS.clear();
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
                    Bukkit.getServer().sendActionBar(Component.translatable("event.start_countdown").args(Component.text(i)).color(NamedTextColor.GREEN));
                    Bukkit.getServer().playSound(Sound.sound(Key.key("minecraft:entity.arrow.hit_player"), Sound.Source.MASTER,  1f, 0.5f));
                }
                if (i-- == 0) {
                    this.cancel();
                    start();
                }
            }
        }.runTaskTimer(PLUGIN, 0L, 20L));
        Bukkit.broadcast(Component.translatable("event.ready_message").args(name, Component.text(PLUGIN.getConfig().getInt("ready_time"))).color(NamedTextColor.GREEN));
        Bukkit.broadcast(Component.translatable("event.rule." + id));
        onSetup();
        PLUGIN.getComponentLogger().info(Component.translatable("console.competition.coming").args(name));
    }

    @Override
    public void start() {
        setStage(Stage.STARTED);
        onStart();
        Bukkit.getServer().sendActionBar(Component.translatable("event.start_message"));
        Bukkit.getServer().playSound(Sound.sound(Key.key("minecraft:entity.arrow.hit_player"), Sound.Source.MASTER, 1f, 1f));
    }

    @Override
    public void end(boolean force) {
        setStage(Stage.ENDED);
        onEnd(force);
        EVENT_TASKS.forEach(BukkitTask::cancel);
        addRunnable(new BukkitRunnable() {
            @Override
            public void run() {
                if (stage == Stage.ENDED) {
                    Competitions.setCurrentlyEvent(null);
                    setStage(Stage.IDLE);
                    Bukkit.getOnlinePlayers().forEach(p -> {
                        p.teleport(location);
                        p.setGameMode(GameMode.ADVENTURE);
                    });
                    Competitions.getOnlinePlayers().forEach(d -> {
                        d.getPlayer().getInventory().clear();
                        PlayerCustomize.suitUp(d.getPlayer());
                        d.getPlayer().getInventory().setItem(3, CompetitionListener.MENU);
                        d.getPlayer().getInventory().setItem(4, CompetitionListener.CUSTOMIZE);
                    });
                    getWorld().getEntitiesByClass(ItemFrame.class).forEach(e -> {
                        if (e.getPersistentDataContainer().has(CompetitionListener.GRAFFITI)) e.remove();
                    });
                }
            }
        }.runTaskLater(PLUGIN, 100L));
        if (!force) {
            OfflinePlayer mvp = getLeaderboard().get(0).getOfflinePlayer();
            CustomizeMusickit musickit = PlayerCustomize.getMusickit(mvp);
            if (musickit != null) {
                Bukkit.getServer().playSound(Sound.sound(musickit.getKey(), Sound.Source.MASTER, 1f, 1f));
                Bukkit.getServer().sendActionBar(Component.translatable("broadcast.play_musickit").args(Component.text(Objects.requireNonNull(mvp.getName())).color(NamedTextColor.YELLOW), musickit.getName()));
            }
        }
        Bukkit.getServer().sendTitlePart(TitlePart.TITLE, Component.translatable("event.ended_message"));
        PLUGIN.getComponentLogger().info(Component.translatable(force ? "console.competition.force_end" : "console.competition.end").args(name));
    }

    /**
     * Called when the event sets up
     * @see IEvent#setup()
     */
    protected abstract void onSetup();

    /**
     * Called when the event starts
     * @see IEvent#start() ()
     */
    protected abstract void onStart();

    /**
     * Called when the event ends
     * @see IEvent#end(boolean)
     */
    protected abstract void onEnd(boolean force);

    @Override
    public final Stage getStage() {
        return stage;
    }

    /**
     * Set the current event stage
     * @param stage new stage
     */
    protected void setStage(Stage stage) {
        this.stage = stage;
        GUIManager.COMPETITION_INFO_GUI.update();
    }

    @Override
    public void practice(@NotNull Player player) {
        player.teleport(location);
        player.setBedSpawnLocation(location, true);
        player.sendMessage(Component.translatable("player.teleport.field").args(name));
        onPractice(player);
    }

    protected void onPractice(Player player) {
    }

    /**
     * Get the list of {@link PlayerData} of current event
     * @return list of {@link PlayerData} of current event
     */
    public final List<PlayerData> getPlayerDataList() {
        return players;
    }

    /**
     * Add a {@link BukkitTask} to this event
     * @param task {@link BukkitTask} to add
     */
    protected BukkitTask addRunnable(BukkitTask task) {
        EVENT_TASKS.add(task);
        return task;
    }
}
