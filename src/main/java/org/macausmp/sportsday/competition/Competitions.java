package org.macausmp.sportsday.competition;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.PlayerData;
import org.macausmp.sportsday.SportsDay;
import org.macausmp.sportsday.competition.sumo.Sumo;
import org.macausmp.sportsday.event.CompetitionJoinPlayerEvent;
import org.macausmp.sportsday.event.CompetitionLeavePlayerEvent;
import org.macausmp.sportsday.event.CompetitionSetupEvent;
import org.macausmp.sportsday.gui.CompetitionGUI;
import org.macausmp.sportsday.gui.PlayerListGUI;
import org.macausmp.sportsday.util.Translation;

import java.util.*;

public class Competitions {
    private static final SportsDay PLUGIN = SportsDay.getInstance();
    public static final List<ICompetition> COMPETITIONS = new ArrayList<>();
    public static final ICompetition ELYTRA_RACING = register(new ElytraRacing());
    public static final ICompetition ICE_BOAT_RACING = register(new IceBoatRacing());
    public static final ICompetition JAVELIN_THROW = register(new JavelinThrow());
    public static final ICompetition OBSTACLE_COURSE = register(new ObstacleCourse());
    public static final ICompetition PARKOUR = register(new Parkour());
    public static final ICompetition SUMO = register(new Sumo());
    private static final List<PlayerData> PLAYERS = new ArrayList<>();
    private static ICompetition CURRENTLY_COMPETITION;
    private static int NUMBER = 1;
    private static final List<Integer> REGISTERED_NUMBER_LIST = new ArrayList<>();

    /**
     * Register competition
     * @param competition competition to register
     * @return competition after registered
     */
    private static <T extends ICompetition> T register(T competition) {
        COMPETITIONS.add(competition);
        return competition;
    }

    public static void load() {
        PLAYERS.clear();
        Set<String> keys = PLUGIN.getPlayerConfig().getKeys(false);
        for (String s : keys) {
            UUID uuid = UUID.fromString(s);
            int number = PLUGIN.getPlayerConfig().getInt(s + ".number");
            int score = PLUGIN.getPlayerConfig().getInt((s + ".score"));
            PLAYERS.add(new PlayerData(uuid, number, score));
        }
    }

    public static void save() {
        for (PlayerData data : PLAYERS) {
            PLUGIN.getPlayerConfig().set(data.getUUID() + ".name", data.getName());
            PLUGIN.getPlayerConfig().set(data.getUUID() + ".number", data.getNumber());
            PLUGIN.getPlayerConfig().set(data.getUUID() + ".score", data.getScore());
        }
        PLUGIN.savePlayerConfig();
    }

    /**
     * Start a competition
     * @param sender who host the competition
     * @param id competition id
     * @return True if competition successfully started
     */
    public static boolean start(CommandSender sender, String id) {
        if (getCurrentlyCompetition() != null && getCurrentlyCompetition().getStage() != Stage.ENDED) {
            sender.sendMessage(Translation.translatable("competition.already_in_progress").color(NamedTextColor.RED));
            return false;
        }
        for (ICompetition competition : COMPETITIONS) {
            if (competition.getID().equals(id)) {
                if (!competition.isEnable()) {
                    sender.sendMessage(Translation.translatable("competition.disabled").color(NamedTextColor.RED));
                    return false;
                }
                int i = getOnlinePlayers().size();
                if (i >= competition.getLeastPlayersRequired()) {
                    sender.sendMessage(Translation.translatable("competition.setting_up").color(NamedTextColor.GREEN));
                    setCurrentlyCompetition(competition);
                    competition.setup();
                    Bukkit.getPluginManager().callEvent(new CompetitionSetupEvent(competition));
                    return true;
                } else {
                    sender.sendMessage(Translation.translatable("competition.not_enough_player_required").args(Component.text(competition.getLeastPlayersRequired())).color(NamedTextColor.RED));
                    return false;
                }
            }
        }
        sender.sendMessage(Translation.translatable("competition.unknown").color(NamedTextColor.RED));
        return false;
    }

    /**
     * Force end current competition
     * @param sender who end the competition
     */
    public static void forceEnd(CommandSender sender) {
        if (getCurrentlyCompetition() != null && getCurrentlyCompetition().getStage() != Stage.ENDED) {
            getCurrentlyCompetition().end(true);
            sender.sendMessage(Translation.translatable("competition.force_end").color(NamedTextColor.GREEN));
        } else {
            sender.sendMessage(Translation.translatable("competition.not_in_progress").color(NamedTextColor.RED));
        }
    }

    /**
     * Get current competition
     * @return current competition
     */
    public static ICompetition getCurrentlyCompetition() {
        return CURRENTLY_COMPETITION;
    }

    /**
     * Set current competition
     * @param competition new competition
     */
    public static void setCurrentlyCompetition(ICompetition competition) {
        CURRENTLY_COMPETITION = competition;
    }

    /**
     * Add player to competition player list
     * @param player player to add to competition player list
     * @param number player's competition number
     * @return True if player successfully added to competition player list
     */
    public static boolean join(@NotNull Player player, int number) {
        for (PlayerData data : PLAYERS) {
            if (data.getNumber() == number) {
                return false;
            }
        }
        PLAYERS.add(new PlayerData(player.getUniqueId(), number));
        CompetitionGUI.COMPETITION_INFO_GUI.update();
        PlayerListGUI.updateGUI();
        player.sendMessage(Translation.translatable("player.register_success_message").args(Component.text(number)).color(NamedTextColor.GREEN));
        SportsDay.PLAYER.addPlayer(player);
        Bukkit.getPluginManager().callEvent(new CompetitionJoinPlayerEvent(player));
        return true;
    }

    /**
     * Remove player from competition player list
     * @param player player to remove from competition player list
     * @return True if player successfully removed to competition player list
     */
    public static boolean leave(OfflinePlayer player) {
        if (containPlayer(player)) {
            for (PlayerData data : PLAYERS) {
                if (data.getUUID().equals(player.getUniqueId())) {
                    PLUGIN.getPlayerConfig().set(data.getUUID().toString(), null);
                    PLUGIN.savePlayerConfig();
                    REGISTERED_NUMBER_LIST.remove((Integer) data.getNumber());
                    PLAYERS.remove(data);
                    CompetitionGUI.COMPETITION_INFO_GUI.update();
                    PlayerListGUI.updateGUI();
                    if (player.isOnline()) {
                        Objects.requireNonNull(player.getPlayer()).sendMessage(Translation.translatable("player.leave_message"));
                    }
                    SportsDay.AUDIENCE.addPlayer(player);
                    Bukkit.getPluginManager().callEvent(new CompetitionLeavePlayerEvent(player));
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Generate player numbers for competitions
     * @return Unoccupied player number
     */
    public static int genNumber() {
        PLAYERS.forEach(data -> {
            if (!REGISTERED_NUMBER_LIST.contains(data.getNumber())) REGISTERED_NUMBER_LIST.add(data.getNumber());
        });
        while (REGISTERED_NUMBER_LIST.contains(NUMBER)) {
            NUMBER++;
        }
        REGISTERED_NUMBER_LIST.add(NUMBER);
        return NUMBER;
    }

    /**
     * Get list of registered player data
     * @return list of registered player data
     */
    public static List<PlayerData> getPlayerData() {
        return PLAYERS;
    }

    /**
     * Get list of online registered player data
     * @return list of online registered player data
     */
    public static @NotNull List<PlayerData> getOnlinePlayers() {
        List<PlayerData> list = new ArrayList<>();
        for (PlayerData d : PLAYERS) {
            if (d.isPlayerOnline()) {
                list.add(d);
            }
        }
        return list;
    }

    /**
     * Return true if the player is in competition player list
     * @param player player who presence in competition player list is to be tested
     * @return True if the player is in competition player list
     */
    public static boolean containPlayer(OfflinePlayer player) {
        for (PlayerData data : PLAYERS) {
            if (data.getUUID().equals(player.getUniqueId())) return true;
        }
        return false;
    }

    /**
     * Get player data by uuid
     * @param uuid player uuid
     * @return player data
     */
    public static @NotNull PlayerData getPlayerData(UUID uuid) {
        for (PlayerData data : PLAYERS) {
            if (data.getUUID().equals(uuid)) return data;
        }
        throw new IllegalArgumentException("Player data list does not contain this data");
    }
}
