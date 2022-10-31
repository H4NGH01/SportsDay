package org.macausmp.sportsday.competition;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.macausmp.sportsday.PlayerData;
import org.macausmp.sportsday.SportsDay;
import org.macausmp.sportsday.competition.sumo.Sumo;
import org.macausmp.sportsday.gui.CompetitionGUI;

import java.util.*;

public class Competitions {
    public static final List<AbstractCompetition> COMPETITIONS = new ArrayList<>();
    public static final AbstractCompetition ELYTRA_RACING = registry(new ElytraRacing());
    public static final AbstractCompetition ICE_BOAT_RACING = registry(new IceBoatRacing());
    public static final AbstractCompetition JAVELIN_THROW = registry(new JavelinThrow());
    public static final AbstractCompetition OBSTACLE_COURSE = registry(new ObstacleCourse());
    public static final AbstractCompetition PARKOUR = registry(new Parkour());
    public static final AbstractCompetition SUMO = registry(new Sumo());
    private static final List<PlayerData> PLAYERS = new ArrayList<>();
    private static ICompetition CURRENTLY_COMPETITION;
    private static int NUMBER = 1;
    private static final List<Integer> REGISTERED_NUMBER_LIST = new ArrayList<>();

    private static <T extends AbstractCompetition> T registry(T competition) {
        COMPETITIONS.add(competition);
        return competition;
    }

    public static void load() {
        getPlayerDataList().clear();
        Set<String> keys = SportsDay.getInstance().getPlayerConfig().getKeys(false);
        for (String s : keys) {
            UUID uuid = UUID.fromString(s);
            int number = SportsDay.getInstance().getPlayerConfig().getInt(s + ".number");
            int score = SportsDay.getInstance().getPlayerConfig().getInt((s + ".score"));
            getPlayerDataList().add(new PlayerData(uuid, number, score));
        }
    }

    public static void save() {
        for (PlayerData data : getPlayerDataList()) {
            SportsDay.getInstance().getPlayerConfig().set(data.getUUID() + ".name", data.getName());
            SportsDay.getInstance().getPlayerConfig().set(data.getUUID() + ".number", data.getNumber());
            SportsDay.getInstance().getPlayerConfig().set(data.getUUID() + ".score", data.getScore());
        }
        SportsDay.getInstance().savePlayerConfig();
    }

    public static List<PlayerData> getPlayerDataList() {
        return PLAYERS;
    }

    public static boolean containPlayer(OfflinePlayer player) {
        for (PlayerData data : PLAYERS) {
            if (data.getUUID().equals(player.getUniqueId())) return true;
        }
        return false;
    }

    public static @Nullable PlayerData getPlayerData(UUID uuid) {
        for (PlayerData data : PLAYERS) {
            if (data.getUUID().equals(uuid)) return data;
        }
        return null;
    }

    public static ICompetition getCurrentlyCompetition() {
        return CURRENTLY_COMPETITION;
    }

    public static void setCurrentlyCompetition(ICompetition newCompetition) {
        CURRENTLY_COMPETITION = newCompetition;
    }

    public static boolean join(@NotNull Player player, int number) {
        for (PlayerData data : Competitions.getPlayerDataList()) {
            if (data.getNumber() == number) {
                return false;
            }
        }
        Competitions.getPlayerDataList().add(new PlayerData(player.getUniqueId(), number));
        CompetitionGUI.PLAYER_LIST_GUI.update();
        player.sendMessage(Component.text("你已成功註冊為參賽選手，選手號碼為" + number).color(NamedTextColor.GREEN));
        return true;
    }

    public static boolean leave(OfflinePlayer player) {
        if (containPlayer(player)) {
            for (PlayerData data : getPlayerDataList()) {
                if (data.getUUID().equals(player.getUniqueId())) {
                    SportsDay.getInstance().getPlayerConfig().set(data.getUUID().toString(), null);
                    SportsDay.getInstance().savePlayerConfig();
                    REGISTERED_NUMBER_LIST.remove((Integer) data.getNumber());
                    getPlayerDataList().remove(data);
                    CompetitionGUI.PLAYER_LIST_GUI.update();
                    if (player.isOnline()) {
                        Objects.requireNonNull(player.getPlayer()).sendMessage(Component.text("你已被從參賽選手名單上除名").color(NamedTextColor.YELLOW));
                    }
                    return true;
                }
            }
        }
        return false;
    }

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
}
