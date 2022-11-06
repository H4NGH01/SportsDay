package org.macausmp.sportsday;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.GameRule;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.*;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.command.CommandManager;
import org.macausmp.sportsday.competition.CompetitionListener;
import org.macausmp.sportsday.competition.Competitions;
import org.macausmp.sportsday.gui.GUIListener;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public final class SportsDay extends JavaPlugin {
    private static SportsDay instance;
    public static NamespacedKey ITEM_ID;
    public static NamespacedKey COMPETITION_ID;
    private PlayerConfig playerConfig;
    private CommandManager commandManager;
    public static Team PLAYER;
    public static Team REFEREE;
    public static Team AUDIENCE;
    private BukkitTask playerlistTask;

    @Override
    public void onEnable() {
        instance = this;
        getConfig().options().copyDefaults(true);
        saveConfig();
        if (playerConfig == null) {
            playerConfig = new PlayerConfig();
        }
        playerConfig.setup();
        playerConfig.saveConfig();
        ITEM_ID = registryNamespaceKey("item_id");
        COMPETITION_ID = registryNamespaceKey("competition_id");
        registryCommand();
        registryListener();
        Competitions.load();
        ScoreboardManager manager = getServer().getScoreboardManager();
        Scoreboard scoreboard = manager.getMainScoreboard();
        PLAYER = registryTeam(scoreboard, "player", Component.text("選手"), Component.text("[選手] "), NamedTextColor.GREEN);
        REFEREE = registryTeam(scoreboard, "referee", Component.text("裁判"), Component.text("[裁判] "), NamedTextColor.GOLD);
        AUDIENCE = registryTeam(scoreboard, "audience", Component.text("觀眾"), Component.text("[觀眾] "), NamedTextColor.GRAY);
        getServer().getWorlds().forEach(w -> {
            w.setGameRule(GameRule.DISABLE_ELYTRA_MOVEMENT_CHECK, true);
            w.setGameRule(GameRule.DO_ENTITY_DROPS, false);
            w.setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, true);
            w.setGameRule(GameRule.DO_INSOMNIA, false);
            w.setGameRule(GameRule.DO_MOB_SPAWNING, true);
            w.setGameRule(GameRule.DO_PATROL_SPAWNING, false);
            w.setGameRule(GameRule.DO_TRADER_SPAWNING, false);
        });
        sendPackets();
        getServer().getConsoleSender().sendMessage("Macau SMP SportsDay plugin enabled");
    }

    private NamespacedKey registryNamespaceKey(String key) {
        return NamespacedKey.fromString(key) != null ? NamespacedKey.fromString(key) : new NamespacedKey(SportsDay.getInstance(), key);
    }

    private void registryCommand() {
        if (commandManager == null) {
            commandManager = new CommandManager();
        }
        commandManager.registry();
    }

    private void registryListener() {
        getServer().getPluginManager().registerEvents(new CompetitionListener(), this);
        getServer().getPluginManager().registerEvents(Competitions.ELYTRA_RACING, this);
        getServer().getPluginManager().registerEvents(Competitions.ICE_BOAT_RACING, this);
        getServer().getPluginManager().registerEvents(Competitions.JAVELIN_THROW, this);
        getServer().getPluginManager().registerEvents(Competitions.OBSTACLE_COURSE, this);
        getServer().getPluginManager().registerEvents(Competitions.PARKOUR, this);
        getServer().getPluginManager().registerEvents(Competitions.SUMO, this);
        getServer().getPluginManager().registerEvents(new GUIListener(), this);
    }

    private Team registryTeam(@NotNull Scoreboard scoreboard, String name, Component display, Component prefix, NamedTextColor color) {
        if (scoreboard.getTeam(name) == null) {
            Team team = scoreboard.registerNewTeam(name);
            team.displayName(display);
            team.color(color);
            team.prefix(prefix);
        }
        return scoreboard.getTeam(name);
    }

    @Override
    public void onDisable() {
        Competitions.save();
        if (playerlistTask != null) playerlistTask.cancel();
        getServer().getConsoleSender().sendMessage("Macau SMP SportsDay plugin disabled");
    }

    public static SportsDay getInstance() {
        return instance;
    }

    public FileConfiguration getPlayerConfig() {
        return this.playerConfig.getPlayerConfig();
    }

    public void savePlayerConfig() {
        this.playerConfig.saveConfig();
    }

    private void sendPackets() {
        //Scoreboard
        ScoreboardManager manager = SportsDay.getInstance().getServer().getScoreboardManager();
        Scoreboard scoreboard = manager.getNewScoreboard();
        Objective o = scoreboard.getObjective("sportsday") != null ? Objects.requireNonNull(scoreboard.getObjective("sportsday")) : scoreboard.registerNewObjective("sportsday", Criteria.DUMMY, Component.text("Macau SMP運動會").color(NamedTextColor.GOLD));
        o.setDisplaySlot(DisplaySlot.SIDEBAR);

        Team tc = getTeam(scoreboard, "competition");
        tc.addEntry("§a當前比賽: ");

        Team ts = getTeam(scoreboard, "stage");
        ts.addEntry("§a比賽階段: ");

        Team tl = getTeam(scoreboard, "players");
        tl.addEntry("§a參賽選手人數: ");
        o.getScore("§a參賽選手人數: ").setScore(5);

        Team tpn = getTeam(scoreboard, "number");
        tpn.addEntry("§a你的參賽號碼: ");
        o.getScore("§a你的參賽號碼: ").setScore(4);

        Team tps = getTeam(scoreboard, "score");
        tps.addEntry("§a你的得分: ");
        o.getScore("§a你的得分: ").setScore(3);

        Team tt = getTeam(scoreboard, "time");
        tt.addEntry("§a當前時間: ");
        o.getScore("§a當前時間: ").setScore(1);

        Team tp = getTeam(scoreboard, "ping");
        tp.addEntry("§a你的延遲: ");
        o.getScore("§a你的延遲: ").setScore(0);

        // Tick time correction (attempt to bring the timer closer to reality)
        long d = Math.round(20f - LocalDateTime.now().getNano() / 50000000f);
        new BukkitRunnable() {
            @Override
            public void run() {
                playerlistTask = new BukkitRunnable() {
                    @Override
                    public void run() {
                        Component header = Component.text("歡迎來到Macau SMP運動會").color(NamedTextColor.GOLD);
                        Component competition;
                        if (Competitions.getCurrentlyCompetition() != null) {
                            Component cn = Competitions.getCurrentlyCompetition().getName();
                            Component sn = Competitions.getCurrentlyCompetition().getStage().getName();
                            competition = Component.translatable("\n當前比賽: %s   比賽階段: %s").color(NamedTextColor.GREEN).args(cn, sn);
                        } else {
                            competition = Component.text("\n比賽還未開始，請耐心等待").color(NamedTextColor.AQUA);
                        }
                        header = header.append(competition);
                        Component s1 = Component.text(Competitions.getPlayerDataList().size());
                        Component s2 = Component.text(getServer().getOfflinePlayers().length);
                        Component footer = Component.translatable("參賽選手人數: %s/%s").args(s1, s2).color(NamedTextColor.GREEN);
                        Component t = Component.text(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
                        Component time = Component.translatable("\n當前時間: %s").args(t).color(NamedTextColor.GREEN);

                        if (Competitions.getCurrentlyCompetition() != null) {
                            tc.suffix(Competitions.getCurrentlyCompetition().getName());
                            ts.suffix(Competitions.getCurrentlyCompetition().getStage().getName());
                            o.getScore("§a當前比賽: ").setScore(8);
                            o.getScore("§a比賽階段: ").setScore(7);
                            o.getScore(" ").setScore(6);
                        } else {
                            o.getScore("§a當前比賽: ").resetScore();
                            o.getScore("§a比賽階段: ").resetScore();
                            o.getScore(" ").resetScore();
                        }
                        tt.suffix(t);
                        tl.suffix(Component.translatable("%s/%s").args(s1, s2));

                        for (Player p : getServer().getOnlinePlayers()) {
                            Component ping = Component.translatable("\n你的延遲: %s").color(NamedTextColor.GREEN).args(Component.text(p.getPing()).color(p.getPing() < 50 ? NamedTextColor.GREEN : NamedTextColor.YELLOW));
                            footer = footer.append(time).append(ping);
                            p.sendPlayerListHeaderAndFooter(header, footer);
                            p.playerListName(p.teamDisplayName());

                            if (Competitions.containPlayer(p)) {
                                tpn.suffix(Component.text(Competitions.getPlayerData(p.getUniqueId()).getNumber()));
                                tps.suffix(Component.text(Competitions.getPlayerData(p.getUniqueId()).getScore()));
                                o.getScore("§a你的參賽號碼: ").setScore(4);
                                o.getScore("§a你的得分: ").setScore(3);
                            } else {
                                o.getScore("§a你的參賽號碼: ").resetScore();
                                o.getScore("§a你的得分: ").resetScore();
                            }
                            o.getScore("  ").setScore(2);
                            tp.suffix(Component.text(p.getPing()));
                            p.setScoreboard(scoreboard);
                        }
                    }
                }.runTaskTimer(SportsDay.getInstance(), 0L, 20L);
            }
        }.runTaskLater(this, d);
    }

    private Team getTeam(@NotNull Scoreboard scoreboard, String teamName) {
        return scoreboard.getTeam(teamName) != null ? scoreboard.getTeam(teamName) : scoreboard.registerNewTeam(teamName);
    }
}
