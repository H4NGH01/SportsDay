package org.macausmp.sportsday.competition;

import net.kyori.adventure.text.Component;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.PlayerData;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CastleKrusader extends AbstractCompetition implements IRoundGame {
    private final Leaderboard<PlayerTeam> leaderboard = new Leaderboard<>();
    private final List<PlayerTeam> queue = new ArrayList<>();

    public CastleKrusader() {
        super("castle_krusader");
    }

    @Override
    protected void onSetup() {
        getQueue().clear();
        PlayerTeam.reset();
        List<PlayerData> temp = new ArrayList<>(getPlayerDataList());
        List<Component> cl = new ArrayList<>();
        for (int i = 0; i < getPlayerDataList().size() / 3 + (getPlayerDataList().size() % 3 == 0 ? 0 : 1); i++) {
            PlayerTeam team = new PlayerTeam(getFromList(temp), getFromList(temp), getFromList(temp));
            getQueue().add(team);
            StringBuilder b = new StringBuilder();
            team.getList().forEach(d -> b.append(d.getName()));
            cl.add(Component.translatable("Team ID: %s Members: %s").args(Component.text(team.getID()), Component.text(b.toString())));
        }
        getOnlinePlayers().forEach(p -> cl.forEach(p::sendMessage));
    }

    private @NotNull PlayerData getFromList(@NotNull List<PlayerData> list) {
        PlayerData data = list.get(new Random().nextInt(list.size()));
        list.remove(data);
        return data;
    }

    @Override
    protected void onStart() {

    }

    @Override
    protected void onEnd(boolean force) {

    }

    @Override
    public <T extends Event> void onEvent(T event) {

    }

    @Override
    public Leaderboard<PlayerTeam> getLeaderboard() {
        return leaderboard;
    }

    @Override
    public void onRoundStart() {

    }

    @Override
    public void onRoundEnd() {

    }

    @Override
    public void nextRound() {

    }

    @Override
    public List<PlayerTeam> getQueue() {
        return queue;
    }

    public static class PlayerTeam {
        private static int STATIC_ID = 0;
        private final int id;
        private final List<PlayerData> list;

        public PlayerTeam(PlayerData... data) {
            id = STATIC_ID++;
            list = new ArrayList<>(List.of(data));
        }

        public int getID() {
            return id;
        }

        public List<PlayerData> getList() {
            return list;
        }

        public static void reset() {
            STATIC_ID = 0;
        }
    }
}
