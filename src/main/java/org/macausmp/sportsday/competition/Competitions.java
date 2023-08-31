package org.macausmp.sportsday.competition;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.SportsDay;
import org.macausmp.sportsday.competition.sumo.Sumo;
import org.macausmp.sportsday.gui.GUIManager;
import org.macausmp.sportsday.gui.competition.PlayerListGUI;
import org.macausmp.sportsday.util.PlayerData;

import java.util.*;

public final class Competitions {
    private static final SportsDay PLUGIN = SportsDay.getInstance();
    private static final FileConfiguration PLAYER_CONFIG = PLUGIN.getConfigManager().getPlayerConfig();
    public static final List<IEvent> COMPETITIONS = new ArrayList<>();
    public static final IEvent ELYTRA_RACING = register(new ElytraRacing());
    public static final IEvent ICE_BOAT_RACING = register(new IceBoatRacing());
    public static final IEvent JAVELIN_THROW = register(new JavelinThrow());
    public static final IEvent OBSTACLE_COURSE = register(new ObstacleCourse());
    public static final IEvent PARKOUR = register(new Parkour());
    public static final IEvent SUMO = register(new Sumo());
    private static final List<PlayerData> PLAYERS = new ArrayList<>();
    private static IEvent CURRENTLY_EVENT;
    private static int NUMBER = 1;
    private static final List<Integer> REGISTERED_NUMBER_LIST = new ArrayList<>();

    /**
     * Register competition
     * @param competition competition to register
     * @return competition after registered
     */
    private static <T extends IEvent> T register(T competition) {
        COMPETITIONS.add(competition);
        return competition;
    }

    public static void load() {
        PLAYERS.clear();
        Set<String> keys = PLAYER_CONFIG.getKeys(false);
        for (String s : keys) {
            UUID uuid = UUID.fromString(s);
            int number = PLAYER_CONFIG.getInt(s + ".number");
            int score = PLAYER_CONFIG.getInt((s + ".score"));
            PLAYERS.add(new PlayerData(uuid, number, score));
        }
    }

    public static void save() {
        for (PlayerData data : PLAYERS) {
            PLAYER_CONFIG.set(data.getUUID() + ".name", data.getName());
            PLAYER_CONFIG.set(data.getUUID() + ".number", data.getNumber());
            PLAYER_CONFIG.set(data.getUUID() + ".score", data.getScore());
        }
        PLUGIN.getConfigManager().saveConfig();
    }

    /**
     * Start a competition
     * @param sender who host the competition
     * @param id competition id
     * @return True if competition successfully started
     */
    public static boolean start(CommandSender sender, String id) {
        if (getCurrentlyEvent() != null && getCurrentlyEvent().getStage() != Stage.ENDED) {
            sender.sendMessage(Component.translatable("competition.already_in_progress").color(NamedTextColor.RED));
            return false;
        }
        for (IEvent event : COMPETITIONS) {
            if (event.getID().equals(id)) {
                if (!event.isEnable()) {
                    sender.sendMessage(Component.translatable("competition.event_disabled").color(NamedTextColor.RED));
                    return false;
                }
                int i = getOnlinePlayers().size();
                if (i >= event.getLeastPlayersRequired()) {
                    sender.sendMessage(Component.translatable("competition.setting_up").color(NamedTextColor.GREEN));
                    setCurrentlyEvent(event);
                    event.setup();
                    return true;
                } else {
                    sender.sendMessage(Component.translatable("competition.not_enough_player_required").args(Component.text(event.getLeastPlayersRequired())).color(NamedTextColor.RED));
                    return false;
                }
            }
        }
        sender.sendMessage(Component.translatable("event.name.unknown").color(NamedTextColor.RED));
        return false;
    }

    /**
     * Force end current competition
     * @param sender who end the competition
     */
    public static void forceEnd(CommandSender sender) {
        if (getCurrentlyEvent() != null && getCurrentlyEvent().getStage() != Stage.ENDED) {
            getCurrentlyEvent().end(true);
            sender.sendMessage(Component.translatable("competition.force_ended").color(NamedTextColor.GREEN));
        } else {
            sender.sendMessage(Component.translatable("competition.not_in_progress").color(NamedTextColor.RED));
        }
    }

    /**
     * Get current event
     * @return current event
     */
    public static IEvent getCurrentlyEvent() {
        return CURRENTLY_EVENT;
    }

    /**
     * Set current event
     * @param event new event
     */
    public static void setCurrentlyEvent(IEvent event) {
        CURRENTLY_EVENT = event;
    }

    /**
     * Add player to competition player list
     * @param player player to add to competition player list
     * @param number player's competition number
     * @return True if player successfully added to competition player list
     */
    public static boolean join(@NotNull Player player, int number) {
        for (PlayerData data : PLAYERS) {
            if (data.getNumber() == number) return false;
        }
        PLAYERS.add(new PlayerData(player.getUniqueId(), number));
        GUIManager.COMPETITION_INFO_GUI.update();
        PlayerListGUI.updateGUI();
        player.sendMessage(Component.translatable("player.register_success_message").args(Component.text(number)).color(NamedTextColor.GREEN));
        SportsDay.PLAYER.addPlayer(player);
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
                    PLAYER_CONFIG.set(data.getUUID().toString(), null);
                    REGISTERED_NUMBER_LIST.remove((Integer) data.getNumber());
                    if (getCurrentlyEvent() != null) ((AbstractEvent) getCurrentlyEvent()).getPlayerDataList().remove(data);
                    PLAYERS.remove(data);
                    GUIManager.COMPETITION_INFO_GUI.update();
                    PlayerListGUI.updateGUI();
                    if (player.isOnline()) Objects.requireNonNull(player.getPlayer()).sendMessage(Component.translatable("player.leave_message"));
                    SportsDay.AUDIENCE.addPlayer(player);
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
            if (d.getOfflinePlayer().isOnline()) list.add(d);
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
