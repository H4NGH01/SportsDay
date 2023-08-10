package org.macausmp.sportsday;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.GameRule;
import org.bukkit.NamespacedKey;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.*;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.command.CommandManager;
import org.macausmp.sportsday.competition.CompetitionListener;
import org.macausmp.sportsday.competition.Competitions;
import org.macausmp.sportsday.gui.CompetitionGUI;
import org.macausmp.sportsday.gui.GUIListener;
import org.macausmp.sportsday.gui.PlayerListGUI;
import org.macausmp.sportsday.util.Translation;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public final class SportsDay extends JavaPlugin implements Listener {
    private static SportsDay instance;
    public static NamespacedKey ITEM_ID;
    public static NamespacedKey COMPETITION_ID;
    public static Team PLAYER;
    public static Team REFEREE;
    public static Team AUDIENCE;
    private static BossBar BOSSBAR;
    private ConfigManager configManager;
    private CommandManager commandManager;
    private ScoreboardObjects so;

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
        ITEM_ID = NamespacedKey.fromString("item_id", this);
        COMPETITION_ID = NamespacedKey.fromString("competition_id", this);
        Competitions.load();
        ScoreboardManager manager = getServer().getScoreboardManager();
        Scoreboard scoreboard = manager.getMainScoreboard();
        PLAYER = registerTeam(scoreboard, "player", Translation.translatable("role.player"), NamedTextColor.GREEN);
        REFEREE = registerTeam(scoreboard, "referee", Translation.translatable("role.referee"), NamedTextColor.GOLD);
        AUDIENCE = registerTeam(scoreboard, "audience", Translation.translatable("role.audience"), NamedTextColor.GRAY);
        BOSSBAR = getServer().createBossBar(getLanguageConfig().getString("bar.title"), BarColor.YELLOW, BarStyle.SOLID);
        getServer().getWorlds().forEach(w -> {
            w.setGameRule(GameRule.DISABLE_ELYTRA_MOVEMENT_CHECK, false);
            w.setGameRule(GameRule.DO_ENTITY_DROPS, false);
            w.setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, true);
            w.setGameRule(GameRule.DO_INSOMNIA, false);
            w.setGameRule(GameRule.DO_MOB_SPAWNING, true);
            w.setGameRule(GameRule.DO_PATROL_SPAWNING, false);
            w.setGameRule(GameRule.DO_TRADER_SPAWNING, false);
        });
        registerCommand();
        registerListener();
        sendPackets();
        if (so == null) {
            so = new ScoreboardObjects();
        }
        getServer().getOnlinePlayers().forEach(this::sendPackets);
    }

    private void registerCommand() {
        if (commandManager == null) {
            commandManager = new CommandManager();
        }
        commandManager.register();
    }

    private void registerListener() {
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

    private Team registerTeam(@NotNull Scoreboard scoreboard, String name, Component display, NamedTextColor color) {
        if (scoreboard.getTeam(name) == null) {
            Team team = scoreboard.registerNewTeam(name);
            team.displayName(display);
            team.color(color);
            team.prefix(Component.translatable("[%s]").args(display));
        }
        return scoreboard.getTeam(name);
    }

    @Override
    public void onDisable() {
        Competitions.save();
    }

    public FileConfiguration getPlayerConfig() {
        return configManager.getPlayerConfig();
    }

    public FileConfiguration getLanguageConfig() {
        return configManager.getLanguageConfig();
    }

    public void savePlayerConfig() {
        configManager.saveConfig();
    }

    private void sendPackets() {
        long d = Math.round(20f - LocalDateTime.now().getNano() / 50000000f);
        new BukkitRunnable() {
            @Override
            public void run() {
                new BukkitRunnable() {
                    Component header;
                    Component competition;
                    Component time;
                    Component footer;
                    @Override
                    public void run() {
                        header = Translation.translatable("bar.title");
                        if (Competitions.getCurrentlyCompetition() != null) {
                            Component cn = Competitions.getCurrentlyCompetition().getName();
                            Component sn = Competitions.getCurrentlyCompetition().getStage().getName();
                            competition = Translation.translatable("bar.current").args(cn, sn);
                        } else {
                            competition = Translation.translatable("bar.idle");
                        }
                        header = header.append(competition);
                        time = Component.text(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
                        for (Player p : getServer().getOnlinePlayers()) {
                            Component ping = Translation.translatable("bar.ping").color(NamedTextColor.GREEN).args(Component.text(p.getPing()).color(p.getPing() < 50 ? NamedTextColor.GREEN : NamedTextColor.YELLOW));
                            footer = Translation.translatable("bar.local_time").args(time).color(NamedTextColor.GREEN).append(ping);
                            p.sendPlayerListHeaderAndFooter(header, footer);
                            p.playerListName(p.teamDisplayName());
                        }
                    }
                }.runTaskTimer(SportsDay.getInstance(), 0L, 20L);
            }
        }.runTaskLater(this, d);
    }

    private class ScoreboardObjects {
        private final String title = Objects.requireNonNull(getLanguageConfig().getString("scoreboard.title"));
        private final String comp = Objects.requireNonNull(getLanguageConfig().getString("scoreboard.competition"));
        private final String stage = Objects.requireNonNull(getLanguageConfig().getString("scoreboard.stage"));
        private final String count = Objects.requireNonNull(getLanguageConfig().getString("scoreboard.player_count"));
        private final String number = Objects.requireNonNull(getLanguageConfig().getString("scoreboard.number"));
        private final String score = Objects.requireNonNull(getLanguageConfig().getString("scoreboard.score"));
        private final String time = Objects.requireNonNull(getLanguageConfig().getString("scoreboard.time"));
        private final String ping = Objects.requireNonNull(getLanguageConfig().getString("scoreboard.ping"));
    }

    private void sendPackets(Player p) {
        // Tick time correction (attempt to bring the timer closer to reality)
        long d = Math.round(20f - LocalDateTime.now().getNano() / 50000000f);
        new BukkitRunnable() {
            @Override
            public void run() {
                ScoreboardManager manager = SportsDay.getInstance().getServer().getScoreboardManager();
                Scoreboard scoreboard = manager.getNewScoreboard();
                Objective o = scoreboard.registerNewObjective("sportsday", Criteria.DUMMY, Component.text(so.title).color(NamedTextColor.GOLD));
                o.setDisplaySlot(DisplaySlot.SIDEBAR);

                Team tComp = scoreboard.registerNewTeam("competition");
                tComp.addEntry(so.title);

                Team tStage = scoreboard.registerNewTeam("stage");
                tStage.addEntry(so.stage);

                Team tList = scoreboard.registerNewTeam("players");
                tList.addEntry(so.count);
                o.getScore(so.count).setScore(8);

                Team tNumber = scoreboard.registerNewTeam("number");
                tNumber.addEntry(so.number);

                Team tScore = scoreboard.registerNewTeam("score");
                tScore.addEntry(so.score);

                o.getScore("  ").setScore(5);

                Team tTime = scoreboard.registerNewTeam("time");
                tTime.addEntry(so.time);
                o.getScore(so.time).setScore(4);

                Team tPing = scoreboard.registerNewTeam("ping");
                tPing.addEntry(so.ping);
                o.getScore(so.ping).setScore(3);

                o.getScore("   ").setScore(2);
                o.getScore(Objects.requireNonNull(getConfig().getString("server_ip"))).setScore(1);
                new BukkitRunnable() {
                    Component time;
                    @Override
                    public void run() {
                        if (Competitions.getCurrentlyCompetition() != null) {
                            tComp.suffix(Competitions.getCurrentlyCompetition().getName());
                            tStage.suffix(Competitions.getCurrentlyCompetition().getStage().getName());
                            o.getScore(so.comp).setScore(11);
                            o.getScore(so.stage).setScore(10);
                            o.getScore(" ").setScore(9);
                        } else {
                            o.getScore(so.comp).resetScore();
                            o.getScore(so.stage).resetScore();
                            o.getScore(" ").resetScore();
                        }
                        tList.suffix(Component.translatable("%s/%s").args(Component.text(Competitions.getPlayerDataList().size()), Component.text(getServer().getOfflinePlayers().length)));
                        time = Component.text(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
                        tTime.suffix(time);
                        tPing.suffix(Component.text(p.getPing()));
                        if (Competitions.containPlayer(p)) {
                            tNumber.suffix(Component.text(Competitions.getPlayerData(p.getUniqueId()).getNumber()));
                            tScore.suffix(Component.text(Competitions.getPlayerData(p.getUniqueId()).getScore()));
                            o.getScore(so.number).setScore(7);
                            o.getScore(so.score).setScore(6);
                        } else {
                            o.getScore(so.number).resetScore();
                            o.getScore(so.score).resetScore();
                        }
                        p.setScoreboard(scoreboard);
                    }
                }.runTaskTimer(SportsDay.getInstance(), 0L, 20L);
            }
        }.runTaskLater(this, d);
    }

    @EventHandler
    public void onJoin(@NotNull PlayerJoinEvent e) {
        Player p = e.getPlayer();
        sendPackets(p);
        BOSSBAR.addPlayer(p);
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
