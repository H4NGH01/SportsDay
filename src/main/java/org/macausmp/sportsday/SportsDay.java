package org.macausmp.sportsday;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.GameRule;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.command.CommandManager;
import org.macausmp.sportsday.competition.CompetitionListener;
import org.macausmp.sportsday.competition.Competitions;
import org.macausmp.sportsday.gui.GUIListener;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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
        getServer().getWorlds().forEach(w -> w.setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, true));
        sendPlayerListPacket();
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

    private void sendPlayerListPacket() {
        // Tick time correction (attempt to bring the timer closer to reality)
        long d = Math.round(20f - LocalDateTime.now().getNano() / 50000000f);
        new BukkitRunnable() {
            @Override
            public void run() {
                playerlistTask = new BukkitRunnable() {
                    @Override
                    public void run() {
                        getServer().getOnlinePlayers().forEach(p -> {
                            Component header = Component.text("歡迎來到Macau SMP運動會").color(NamedTextColor.GOLD);
                            Component competition;
                            if (Competitions.getCurrentlyCompetition() != null) {
                                Component cn = Competitions.getCurrentlyCompetition().getName();
                                Component sn = Component.text(Competitions.getCurrentlyCompetition().getStage().getName());
                                competition = Component.translatable("\n當前比賽: %s   比賽階段: %s").color(NamedTextColor.YELLOW).args(cn, sn);
                            } else {
                                competition = Component.text("\n比賽還未開始，請耐心等待").color(NamedTextColor.AQUA);
                            }
                            header = header.append(competition);
                            Component s1 = Component.text(Competitions.getPlayerDataList().size());
                            Component s2 = Component.text(getServer().getOfflinePlayers().length);
                            Component footer = Component.translatable("參賽選手人數: %s/%s").args(s1, s2).color(NamedTextColor.YELLOW);
                            Component time = Component.translatable("\n當前時間: %s").args(Component.text(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))));
                            Component ping = Component.translatable("\n你的延遲: %s").args(Component.text(p.getPing()));
                            footer = footer.append(time).append(ping);
                            p.sendPlayerListHeader(header);
                            p.sendPlayerListFooter(footer);
                        });
                    }
                }.runTaskTimer(SportsDay.getInstance(), 0L, 20L);
            }
        }.runTaskLater(this, d);
    }
}
