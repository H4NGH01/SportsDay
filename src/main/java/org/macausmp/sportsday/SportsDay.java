package org.macausmp.sportsday;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.GameRule;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.*;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.command.CommandManager;
import org.macausmp.sportsday.competition.CompetitionListener;
import org.macausmp.sportsday.competition.Competitions;
import org.macausmp.sportsday.gui.CompetitionGUI;
import org.macausmp.sportsday.gui.GUIListener;
import org.macausmp.sportsday.gui.PlayerListGUI;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class SportsDay extends JavaPlugin implements Listener {
    private static SportsDay instance;
    public static NamespacedKey ITEM_ID;
    public static NamespacedKey COMPETITION_ID;
    private ConfigManager configManager;
    private CommandManager commandManager;
    public static Team PLAYER;
    public static Team REFEREE;
    public static Team AUDIENCE;
    private final List<BukkitTask> runnableTasks = new ArrayList<>();

    public static SportsDay getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;
        getConfig().options().copyDefaults(true);
        saveConfig();
        if (configManager == null) {
            configManager = new ConfigManager();
        }
        configManager.setup();
        configManager.saveConfig();
        ITEM_ID = registryNamespaceKey("item_id");
        COMPETITION_ID = registryNamespaceKey("competition_id");
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
        registryCommand();
        registryListener();
        sendPackets();
        getServer().getOnlinePlayers().forEach(this::sendPackets);
        flickTitle();
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
        getServer().getPluginManager().registerEvents(this, this);
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
        runnableTasks.forEach(BukkitTask::cancel);
        getServer().getConsoleSender().sendMessage("Macau SMP SportsDay plugin disabled");
    }

    public FileConfiguration getPlayerConfig() {
        return configManager.getPlayerConfig();
    }

    public FileConfiguration getLangConfig() {
        return configManager.getLangConfig();
    }

    public void savePlayerConfig() {
        configManager.saveConfig();
    }

    private void sendPackets() {
        long d = Math.round(20f - LocalDateTime.now().getNano() / 50000000f);
        new BukkitRunnable() {
            @Override
            public void run() {
                runnableTasks.add(new BukkitRunnable() {
                    Component header;
                    Component competition;
                    Component time;
                    Component footer;
                    @Override
                    public void run() {
                        header = Component.text("歡迎來到Macau SMP運動會").color(NamedTextColor.GOLD);
                        if (Competitions.getCurrentlyCompetition() != null) {
                            Component cn = Competitions.getCurrentlyCompetition().getName();
                            Component sn = Competitions.getCurrentlyCompetition().getStage().getName();
                            competition = Component.translatable("\n當前比賽: %s   比賽階段: %s").color(NamedTextColor.GREEN).args(cn, sn);
                        } else {
                            competition = Component.text("\n比賽還未開始，請耐心等待").color(NamedTextColor.AQUA);
                        }
                        header = header.append(competition);
                        time = Component.text(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
                        for (Player p : getServer().getOnlinePlayers()) {
                            Component ping = Component.translatable("\n你的延遲: %s").color(NamedTextColor.GREEN).args(Component.text(p.getPing()).color(p.getPing() < 50 ? NamedTextColor.GREEN : NamedTextColor.YELLOW));
                            footer = Component.translatable("當前時間: %s").args(time).color(NamedTextColor.GREEN).append(ping);
                            p.sendPlayerListHeaderAndFooter(header, footer);
                            p.playerListName(p.teamDisplayName());
                        }
                    }
                }.runTaskTimer(SportsDay.getInstance(), 0L, 20L));
            }
        }.runTaskLater(this, d);
    }

    private final String title = "Macau SMP運動會";
    private final String sComp = "§a當前比賽: ";
    private final String sStage = "§a比賽階段: ";
    private final String sList = "§a參賽選手人數: ";
    private final String sNumber = "§a你的參賽號碼: ";
    private final String sScore = "§a你的得分: ";
    private final String sTime = "§a當前時間: ";
    private final String sPing = "§a你的延遲: ";

    private void sendPackets(Player p) {
        // Tick time correction (attempt to bring the timer closer to reality)
        long d = Math.round(20f - LocalDateTime.now().getNano() / 50000000f);
        new BukkitRunnable() {
            @Override
            public void run() {
                ScoreboardManager manager = SportsDay.getInstance().getServer().getScoreboardManager();
                Scoreboard scoreboard = manager.getNewScoreboard();
                Objective o = scoreboard.registerNewObjective("sportsday", Criteria.DUMMY, Component.text(title).color(NamedTextColor.GOLD));
                o.setDisplaySlot(DisplaySlot.SIDEBAR);

                Team tComp = scoreboard.registerNewTeam("competition");
                tComp.addEntry(sComp);

                Team tStage = scoreboard.registerNewTeam("stage");
                tStage.addEntry(sStage);

                Team tList = scoreboard.registerNewTeam("players");
                tList.addEntry(sList);
                o.getScore(sList).setScore(8);

                Team tNumber = scoreboard.registerNewTeam("number");
                tNumber.addEntry(sNumber);

                Team tScore = scoreboard.registerNewTeam("score");
                tScore.addEntry(sScore);

                o.getScore("  ").setScore(5);

                Team tTime = scoreboard.registerNewTeam("time");
                tTime.addEntry(sTime);
                o.getScore(sTime).setScore(4);

                Team tPing = scoreboard.registerNewTeam("ping");
                tPing.addEntry(sPing);
                o.getScore(sPing).setScore(3);

                o.getScore("   ").setScore(2);
                o.getScore("§e" + Objects.requireNonNull(getConfig().getString("server_ip"))).setScore(1);
                runnableTasks.add(new BukkitRunnable() {
                    Component time;
                    @Override
                    public void run() {
                        if (Competitions.getCurrentlyCompetition() != null) {
                            tComp.suffix(Competitions.getCurrentlyCompetition().getName());
                            tStage.suffix(Competitions.getCurrentlyCompetition().getStage().getName());
                            o.getScore(sComp).setScore(11);
                            o.getScore(sStage).setScore(10);
                            o.getScore(" ").setScore(9);
                        } else {
                            o.getScore(sComp).resetScore();
                            o.getScore(sStage).resetScore();
                            o.getScore(" ").resetScore();
                        }
                        tList.suffix(Component.translatable("%s/%s").args(Component.text(Competitions.getPlayerDataList().size()), Component.text(getServer().getOfflinePlayers().length)));
                        time = Component.text(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
                        tTime.suffix(time);
                        tPing.suffix(Component.text(p.getPing()));
                        if (Competitions.containPlayer(p)) {
                            tNumber.suffix(Component.text(Competitions.getPlayerData(p.getUniqueId()).getNumber()));
                            tScore.suffix(Component.text(Competitions.getPlayerData(p.getUniqueId()).getScore()));
                            o.getScore(sNumber).setScore(7);
                            o.getScore(sScore).setScore(6);
                        } else {
                            o.getScore(sNumber).resetScore();
                            o.getScore(sScore).resetScore();
                        }
                        p.setScoreboard(scoreboard);
                    }
                }.runTaskTimer(SportsDay.getInstance(), 0L, 20L));
            }
        }.runTaskLater(this, d);
    }

    private void flickTitle() {
        runnableTasks.add(new BukkitRunnable() {
            int i = 0;
            @Override
            public void run() {
                StringBuilder builder = new StringBuilder(title);
                if (i < builder.length()) builder.insert(i, "§e");
                if (i - 1 > 0 && i - 1 < builder.length()) builder.insert(i - 1, "§6");
                if (i - 2 > 0 && i - 2 < builder.length()) builder.insert(i - 2, "§f");
                if (i > builder.length() + 2 && i < builder.length() + 7 || i > builder.length() + 11) {
                    builder.insert(0, "§e");
                }
                for (Player p : getServer().getOnlinePlayers()) {
                    Objective o = p.getScoreboard().getObjective("sportsday");
                    if (o != null) {
                        o.displayName(Component.text(builder.toString()));
                    }
                }
                i++;
                if (i == 50) {
                    i %= 50;
                }
            }
        }.runTaskTimer(SportsDay.getInstance(), 0L, 2L));
    }

    @EventHandler
    public void onJoin(@NotNull PlayerJoinEvent e) {
        Player p = e.getPlayer();
        sendPackets(p);
        if (!p.hasPlayedBefore()) {
            SportsDay.AUDIENCE.addPlayer(p);
            return;
        }
        if (Competitions.containPlayer(e.getPlayer())) {
            CompetitionGUI.COMPETITION_INFO_GUI.update();
            PlayerListGUI.updateGUI();
        }
    }
}
