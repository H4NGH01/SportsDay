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
import org.macausmp.sportsday.gui.competition.CompetitionInfoGUI;
import org.macausmp.sportsday.gui.competition.ContestantsListGUI;
import org.macausmp.sportsday.util.ContestantData;
import org.macausmp.sportsday.util.PlayerHolder;

import java.util.*;
import java.util.stream.Collectors;

public final class Competitions {
    private static final SportsDay PLUGIN = SportsDay.getInstance();
    private static final FileConfiguration CONTESTANTS_CONFIG = PLUGIN.getConfigManager().getContestantsConfig();
    public static final Map<String, IEvent> EVENTS = new LinkedHashMap<>();
    public static final IEvent ELYTRA_RACING = register(new ElytraRacing());
    public static final IEvent ICE_BOAT_RACING = register(new IceBoatRacing());
    public static final IEvent JAVELIN_THROW = register(new JavelinThrow());
    public static final IEvent OBSTACLE_COURSE = register(new ObstacleCourse());
    public static final IEvent PARKOUR = register(new Parkour());
    public static final IEvent SUMO = register(new Sumo());
    private static IEvent CURRENT_EVENT;
    private static final Set<ContestantData> CONTESTANTS = new HashSet<>();
    private static final Set<Integer> REGISTERED_NUMBER_LIST = new HashSet<>();
    private static int NUMBER = 1;

    /**
     * Register competition event
     * @param competition Competition event to register
     * @return Competition event after registered
     */
    private static <T extends IEvent> @NotNull T register(T competition) {
        EVENTS.put(competition.getID(), competition);
        return competition;
    }

    /**
     * Load contestants data
     */
    public static void load() {
        CONTESTANTS.clear();
        Set<String> keys = CONTESTANTS_CONFIG.getKeys(false);
        for (String key : keys) {
            UUID uuid = UUID.fromString(key);
            int number = CONTESTANTS_CONFIG.getInt(key + ".number");
            int score = CONTESTANTS_CONFIG.getInt((key + ".score"));
            CONTESTANTS.add(new ContestantData(uuid, number, score));
        }
    }

    /**
     * Save contestants data
     */
    public static void save() {
        for (ContestantData data : CONTESTANTS) {
            CONTESTANTS_CONFIG.set(data.getUUID() + ".name", data.getName());
            CONTESTANTS_CONFIG.set(data.getUUID() + ".number", data.getNumber());
            CONTESTANTS_CONFIG.set(data.getUUID() + ".score", data.getScore());
        }
        PLUGIN.getConfigManager().saveConfig();
    }

    /**
     * Start a competition
     * @param sender Who host the competition
     * @param id Competition id
     * @return {@code True} if competition successfully started
     */
    public static boolean start(CommandSender sender, String id) {
        if (getCurrentEvent() != null && getCurrentEvent().getStatus() != Status.ENDED) {
            sender.sendMessage(Component.translatable("command.competition.start.failed").color(NamedTextColor.RED));
            return false;
        }
        IEvent event = EVENTS.get(id);
        if (event == null) {
            sender.sendMessage(Component.translatable("event.name.unknown").color(NamedTextColor.RED));
            return false;
        }
        if (!event.isEnable()) {
            sender.sendMessage(Component.translatable("command.competition.disabled").color(NamedTextColor.RED));
            return false;
        }
        if (getOnlineContestants().size() < event.getLeastPlayersRequired()) {
            sender.sendMessage(Component.translatable("command.competition.not_enough_player_required").args(Component.text(event.getLeastPlayersRequired())).color(NamedTextColor.RED));
            return false;
        }
        sender.sendMessage(Component.translatable("command.competition.start.success").color(NamedTextColor.GREEN));
        setCurrentEvent(event);
        event.setup();
        return true;
    }

    /**
     * Force end current competition
     * @param sender who end the competition
     */
    public static void forceEnd(CommandSender sender) {
        if (getCurrentEvent() != null && getCurrentEvent().getStatus() != Status.ENDED) {
            getCurrentEvent().end(true);
            sender.sendMessage(Component.translatable("command.competition.end.success").color(NamedTextColor.GREEN));
        } else {
            sender.sendMessage(Component.translatable("command.competition.end.failed").color(NamedTextColor.RED));
        }
    }

    /**
     * Get the current event
     * @return Current event
     */
    public static IEvent getCurrentEvent() {
        return CURRENT_EVENT;
    }

    /**
     * Set the current event
     * @param event New event
     */
    public static void setCurrentEvent(IEvent event) {
        CURRENT_EVENT = event;
    }

    /**
     * Add a player to contestants list
     * @param player Player to add to the contestants list
     * @param number Player's entry number
     * @return {@code True} if player successfully added to the contestants list
     */
    public static boolean join(@NotNull Player player, int number) {
        for (ContestantData data : CONTESTANTS) {
            if (data.getNumber() == number) return false;
        }
        CONTESTANTS.add(new ContestantData(player.getUniqueId(), number));
        CompetitionInfoGUI.updateGUI();
        ContestantsListGUI.updateGUI();
        player.sendMessage(Component.translatable("command.competition.register.success.self").args(Component.text(number)).color(NamedTextColor.GREEN));
        SportsDay.CONTESTANTS.addPlayer(player);
        return true;
    }

    /**
     * Remove a specific player from contestants list
     * @param player Player to remove from the contestants list
     * @return {@code True} if player successfully removed from the contestants list
     */
    public static boolean leave(OfflinePlayer player) {
        if (isContestant(player)) {
            for (ContestantData data : CONTESTANTS) {
                if (data.getUUID().equals(player.getUniqueId())) {
                    data.remove();
                    if (player.isOnline() && getCurrentEvent() != null) getCurrentEvent().onDisqualification(data);
                    CONTESTANTS_CONFIG.set(data.getUUID().toString(), null);
                    REGISTERED_NUMBER_LIST.remove(data.getNumber());
                    CONTESTANTS.remove(data);
                    CompetitionInfoGUI.updateGUI();
                    ContestantsListGUI.updateGUI();
                    if (player.isOnline()) Objects.requireNonNull(player.getPlayer()).sendMessage(Component.translatable("command.competition.unregister.success.self"));
                    SportsDay.AUDIENCES.addPlayer(player);
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Generate unoccupied contestant numbers
     * @return Unoccupied contestant number
     */
    public static int genNumber() {
        CONTESTANTS.forEach(data -> REGISTERED_NUMBER_LIST.add(data.getNumber()));
        while (REGISTERED_NUMBER_LIST.contains(NUMBER)) {
            NUMBER++;
        }
        REGISTERED_NUMBER_LIST.add(NUMBER);
        return NUMBER;
    }

    /**
     * Gets a view of all registered contestants
     * @return a view of registered contestants
     */
    public static @NotNull Collection<ContestantData> getContestants() {
        return new HashSet<>(CONTESTANTS);
    }

    /**
     * Gets a view of all currently logged in registered contestants
     * @return a view of currently online registered contestants
     */
    public static @NotNull Collection<ContestantData> getOnlineContestants() {
        return CONTESTANTS.stream().filter(PlayerHolder::isOnline).collect(Collectors.toSet());
    }

    /**
     * Checks if this player is in contestants list
     * @param player Specified player
     * @return {@code True} if the player is in contestants list
     */
    public static boolean isContestant(OfflinePlayer player) {
        return CONTESTANTS.stream().anyMatch(data -> data.getUUID().equals(player.getUniqueId()));
    }

    /**
     * Get {@link ContestantData} by uuid
     *
     * <p>Plugins should check that {@link #isContestant(OfflinePlayer)} returns {@code True} before calling this method.</p>
     *
     * @param uuid Player uuid
     * @return {@link ContestantData} of uuid
     */
    public static @NotNull ContestantData getContestant(UUID uuid) {
        for (ContestantData data : CONTESTANTS) {
            if (data.getUUID().equals(uuid)) return data;
        }
        throw new IllegalArgumentException("Contestants data list does not contain this data");
    }
}
