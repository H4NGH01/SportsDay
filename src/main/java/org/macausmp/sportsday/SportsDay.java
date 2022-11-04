package org.macausmp.sportsday;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.GameRule;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.command.CommandManager;
import org.macausmp.sportsday.competition.CompetitionListener;
import org.macausmp.sportsday.competition.Competitions;
import org.macausmp.sportsday.gui.GUIListener;

public final class SportsDay extends JavaPlugin {
    private static SportsDay instance;
    public static NamespacedKey ITEM_ID;
    public static NamespacedKey COMPETITION_ID;
    private PlayerConfig playerConfig;
    private CommandManager commandManager;

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
        registryNamespaceKey();
        if (commandManager == null) {
            commandManager = new CommandManager();
        }
        commandManager.registry();
        registryListener();
        Competitions.load();
        ScoreboardManager manager = getServer().getScoreboardManager();
        Scoreboard scoreboard = manager.getMainScoreboard();
        registryTeam(scoreboard, "player", Component.text("選手"), NamedTextColor.GREEN);
        registryTeam(scoreboard, "referee", Component.text("裁判"), NamedTextColor.GOLD);
        registryTeam(scoreboard, "audience", Component.text("觀眾"), NamedTextColor.GRAY);
        getServer().getWorlds().forEach(w -> w.setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, true));
        getServer().getConsoleSender().sendMessage("Macau SMP SportsDay plugin enabled");
    }

    private void registryNamespaceKey() {
        if (ITEM_ID == null || COMPETITION_ID == null) {
            ITEM_ID = new NamespacedKey(SportsDay.getInstance(), "item_id");
            COMPETITION_ID = new NamespacedKey(SportsDay.getInstance(), "competition_id");
        }
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

    private void registryTeam(@NotNull Scoreboard scoreboard, String name, Component display, NamedTextColor color) {
        if (scoreboard.getTeam(name) == null) {
            Team team = scoreboard.registerNewTeam(name);
            team.displayName(display);
            team.color(color);
        }
    }

    @Override
    public void onDisable() {
        Competitions.save();
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
}
