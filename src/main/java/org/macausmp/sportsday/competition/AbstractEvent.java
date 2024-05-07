package org.macausmp.sportsday.competition;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.TitlePart;
import org.bukkit.*;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.CompetitionListener;
import org.macausmp.sportsday.SportsDay;
import org.macausmp.sportsday.customize.CustomizeMusickit;
import org.macausmp.sportsday.customize.PlayerCustomize;
import org.macausmp.sportsday.gui.competition.CompetitionInfoGUI;
import org.macausmp.sportsday.util.ContestantData;
import org.macausmp.sportsday.util.ItemUtil;
import org.macausmp.sportsday.util.TextUtil;

import java.util.*;

public abstract class AbstractEvent implements IEvent {
    protected static final SportsDay PLUGIN = SportsDay.getInstance();
    private static final Set<BukkitTask> EVENT_TASKS = new HashSet<>();
    private static final Map<Player, IEvent> PRACTICE = new HashMap<>();
    private final String id;
    private final Component name;
    private final int least;
    private final Location location;
    private final World world;
    private Status status = Status.IDLE;
    private final Collection<ContestantData> contestants = new HashSet<>();
    private final Map<UUID, CustomizeMusickit> contestantToMusickit = new HashMap<>();
    public static final NamespacedKey IN_GAME = new NamespacedKey(PLUGIN, "in_game");
    private long time;

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
    public final long getLastTime() {
        return time;
    }

    @Override
    public void setup() {
        time = System.currentTimeMillis();
        Bukkit.getOnlinePlayers().forEach(p -> {
            leavePractice(p);
            p.getPersistentDataContainer().set(IN_GAME, PersistentDataType.LONG, time);
        });
        PRACTICE.clear();
        EVENT_TASKS.forEach(BukkitTask::cancel);
        EVENT_TASKS.clear();
        contestants.clear();
        getLeaderboard().clear();
        setStatus(Status.COMING);
        contestants.addAll(Competitions.getOnlineContestants());
        contestants.forEach(data -> {
            Player p = data.getPlayer();
            if (!SportsDay.REFEREES.hasPlayer(p)) p.getInventory().clear();
            p.clearActivePotionEffects();
            p.setFireTicks(0);
            p.setGameMode(GameMode.ADVENTURE);
            PlayerCustomize.suitUp(p);
            p.getInventory().setItem(4, ItemUtil.SPRAY);
            p.setBedSpawnLocation(location, true);
            p.teleport(location);
            contestantToMusickit.put(data.getUUID(), PlayerCustomize.getMusickit(p));
        });
        addRunnable(new BukkitRunnable() {
            int i = PLUGIN.getConfig().getInt("ready_time");
            @Override
            public void run() {
                if (i % 5 == 0 || (i <= 5 && i > 0)) {
                    Bukkit.getServer().sendActionBar(Component.translatable("event.start.countdown").args(Component.text(i)).color(NamedTextColor.GREEN));
                    Bukkit.getServer().playSound(Sound.sound(Key.key("minecraft:entity.arrow.hit_player"), Sound.Source.MASTER, 1f, 0.5f));
                }
                if (i-- == 0) {
                    start();
                    cancel();
                }
            }
        }.runTaskTimer(PLUGIN, 0L, 20L));
        Bukkit.broadcast(Component.translatable("event.ready.broadcast").args(name, Component.text(PLUGIN.getConfig().getInt("ready_time"))).color(NamedTextColor.GREEN));
        Bukkit.broadcast(Component.translatable("event.rule." + id));
        onSetup();
        PLUGIN.getComponentLogger().info(Component.translatable("console.competition.coming").args(name));
    }

    @Override
    public void start() {
        setStatus(Status.STARTED);
        onStart();
        Bukkit.getServer().sendActionBar(Component.translatable("event.start.broadcast"));
        Bukkit.getServer().playSound(Sound.sound(Key.key("minecraft:entity.arrow.hit_player"), Sound.Source.MASTER, 1f, 1f));
    }

    @Override
    public void end(boolean force) {
        setStatus(Status.ENDED);
        onEnd(force);
        EVENT_TASKS.forEach(BukkitTask::cancel);
        if (!force) {
            if (!getLeaderboard().isEmpty()) {
                OfflinePlayer mvp = getLeaderboard().getFirst().getOfflinePlayer();
                CustomizeMusickit musickit = contestantToMusickit.get(mvp.getUniqueId());
                if (musickit != null) {
                    Bukkit.getServer().playSound(Sound.sound(musickit.getKey(), Sound.Source.MASTER, 1f, 1f));
                    Bukkit.getServer().sendActionBar(Component.translatable("broadcast.play_mvp_anthem").args(Component.text(Objects.requireNonNull(mvp.getName())).color(NamedTextColor.YELLOW), musickit.getName()));
                }
            }
            addRunnable(new BukkitRunnable() {
                @Override
                public void run() {
                    end();
                }
            }.runTaskLater(PLUGIN, 100L));
        } else {
            end();
        }
        Bukkit.getServer().sendTitlePart(TitlePart.TITLE, Component.translatable("event.end.broadcast"));
        PLUGIN.getComponentLogger().info(Component.translatable(force ? "console.competition.force_end" : "console.competition.end").args(name));
    }

    private void end() {
        if (status == Status.ENDED) {
            Competitions.setCurrentEvent(null);
            setStatus(Status.IDLE);
            Bukkit.getOnlinePlayers().forEach(p -> {
                p.getPersistentDataContainer().remove(IN_GAME);
                p.teleport(location);
                p.setGameMode(GameMode.ADVENTURE);
            });
            Competitions.getOnlineContestants().forEach(d -> {
                Player p = d.getPlayer();
                p.getInventory().clear();
                PlayerCustomize.suitUp(p);
                p.getInventory().setItem(0, ItemUtil.MENU);
                p.getInventory().setItem(4, ItemUtil.CUSTOMIZE);
            });
            getWorld().getEntitiesByClass(ItemFrame.class).forEach(e -> {
                if (e.getPersistentDataContainer().has(CompetitionListener.GRAFFITI)) e.remove();
            });
        }
    }

    /**
     * Called when the event sets up
     * @see #setup()
     */
    protected abstract void onSetup();

    /**
     * Called when the event starts
     * @see #start()
     */
    protected abstract void onStart();

    /**
     * Called when the event ends
     * @see #end(boolean)
     */
    protected abstract void onEnd(boolean force);

    @Override
    public final Status getStatus() {
        return status;
    }

    /**
     * Set the current event status
     * @param status new status
     */
    protected final void setStatus(Status status) {
        this.status = status;
        CompetitionInfoGUI.updateGUI();
    }

    public final Collection<ContestantData> getContestants() {
        return contestants;
    }

    @Override
    public void onDisqualification(@NotNull ContestantData contestant) {
        contestants.remove(contestant);
        getLeaderboard().remove(contestant);
        Player p = contestant.getPlayer();
        if (p.isInsideVehicle()) Objects.requireNonNull(p.getVehicle()).remove();
        p.clearActivePotionEffects();
        p.setFireTicks(0);
        p.getInventory().clear();
        PlayerCustomize.suitUp(p);
        p.getInventory().setItem(0, ItemUtil.MENU);
        p.getInventory().setItem(4, ItemUtil.CUSTOMIZE);
        p.setBedSpawnLocation(p.getWorld().getSpawnLocation(), true);
    }

    @Override
    public void joinPractice(@NotNull Player p) {
        PRACTICE.put(p, this);
        p.clearActivePotionEffects();
        p.setFireTicks(0);
        p.getInventory().clear();
        PlayerCustomize.suitUp(p);
        p.getInventory().setItem(8, ItemUtil.LEAVE_PRACTICE);
        p.setBedSpawnLocation(location, true);
        p.teleport(location);
        p.sendMessage(Component.translatable("contestant.practice.teleport.venue").args(name));
        onPractice(p);
        p.playSound(Sound.sound(Key.key("minecraft:entity.arrow.hit_player"), Sound.Source.MASTER, 1f, 1f));
    }

    /**
     * Called when a player participates in practice
     * @param p Who going to practice this event
     */
    protected abstract void onPractice(@NotNull Player p);

    /**
     * Let players leave this practice
     * @param p Who leave practicing this event
     */
    public static void leavePractice(@NotNull Player p) {
        if (!PRACTICE.containsKey(p)) return;
        PRACTICE.remove(p);
        if (p.isInsideVehicle()) Objects.requireNonNull(p.getVehicle()).remove();
        p.clearActivePotionEffects();
        p.setFireTicks(0);
        p.getInventory().clear();
        PlayerCustomize.suitUp(p);
        p.getInventory().setItem(0, ItemUtil.MENU);
        p.getInventory().setItem(4, ItemUtil.CUSTOMIZE);
        p.setBedSpawnLocation(p.getWorld().getSpawnLocation(), true);
        p.teleport(p.getWorld().getSpawnLocation());
    }

    /**
     * Check if player is practicing
     * @param p Who going to be checked
     * @return True if player is practicing
     */
    public static boolean inPractice(Player p) {
        return PRACTICE.containsKey(p);
    }

    /**
     * Check if player is practicing at specified event
     * @param p Who going to be checked
     * @param event The specified event
     * @return True if player is practicing at specified event
     * @param <T> The event type
     */
    public static <T extends IEvent> boolean inPractice(Player p, T event) {
        return PRACTICE.containsKey(p) && PRACTICE.get(p) == event;
    }

    /**
     * Get the event the player is practice on
     * @param p Who is practicing
     * @return Event the player is practice on
     * @param <T> The event type
     */
    public static <T extends IEvent> T getPracticeEvent(Player p) {
        //noinspection unchecked
        return (T) PRACTICE.get(p);
    }

    /**
     * Add a {@link BukkitTask} to this event
     * @param task {@link BukkitTask} to add
     */
    protected final BukkitTask addRunnable(BukkitTask task) {
        EVENT_TASKS.add(task);
        return task;
    }
}
