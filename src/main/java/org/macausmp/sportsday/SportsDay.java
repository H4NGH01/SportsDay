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
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
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
    private ScoreboardHandler scoreboardHandler;

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
        Scoreboard scoreboard = getServer().getScoreboardManager().getMainScoreboard();
        PLAYER = registerTeam(scoreboard, "player", Translation.translatable("role.player"), NamedTextColor.GREEN);
        REFEREE = registerTeam(scoreboard, "referee", Translation.translatable("role.referee"), NamedTextColor.GOLD);
        AUDIENCE = registerTeam(scoreboard, "audience", Translation.translatable("role.audience"), NamedTextColor.GRAY);
        BOSSBAR = getServer().createBossBar(Translation.translate("bar.title"), BarColor.YELLOW, BarStyle.SOLID);
        setGameRules();
        registerCommand();
        registerListener();
        sendPackets();
        if (scoreboardHandler == null) {
            scoreboardHandler = new ScoreboardHandler();
        }
        getServer().getOnlinePlayers().forEach(scoreboardHandler::setScoreboard);
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

    private void setGameRules() {
        getServer().getWorlds().forEach(w -> {
            w.setGameRule(GameRule.DISABLE_ELYTRA_MOVEMENT_CHECK, false);
            w.setGameRule(GameRule.DO_ENTITY_DROPS, false);
            w.setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, true);
            w.setGameRule(GameRule.DO_INSOMNIA, false);
            w.setGameRule(GameRule.DO_MOB_SPAWNING, false);
            w.setGameRule(GameRule.DO_PATROL_SPAWNING, false);
            w.setGameRule(GameRule.DO_TRADER_SPAWNING, false);
        });
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
                        header = Translation.translatable("bar.title").appendNewline();
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
                            footer = Translation.translatable("bar.local_time").args(time).color(NamedTextColor.GREEN).appendNewline().append(ping);
                            p.sendPlayerListHeaderAndFooter(header, footer);
                            p.playerListName(p.teamDisplayName());
                        }
                    }
                }.runTaskTimer(SportsDay.getInstance(), 0L, 20L);
            }
        }.runTaskLater(this, d);
    }

    @EventHandler
    public void onJoin(@NotNull PlayerJoinEvent e) {
        Player p = e.getPlayer();
        scoreboardHandler.setScoreboard(p);
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
