package org.macausmp.sportsday.competition.sumo;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.PlayerData;
import org.macausmp.sportsday.SportsDay;
import org.macausmp.sportsday.competition.AbstractCompetition;
import org.macausmp.sportsday.competition.IRoundGame;
import org.macausmp.sportsday.competition.Leaderboard;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class Sumo extends AbstractCompetition implements IRoundGame {
    private final Leaderboard<PlayerData> leaderboard = new Leaderboard<>();
    private final List<PlayerData> queue = new ArrayList<>();
    private final List<PlayerData> alive = new ArrayList<>();
    private SumoStage sumoStage = SumoStage.ELIMINATE;
    private final Player[] theFinal = new Player[2];
    private final Player[] thirdPlace = new Player[2];
    private final Player[] semiFinal = new Player[4];

    @Override
    public String getID() {
        return "sumo";
    }

    @Override
    public void onSetup() {
        alive.clear();
        alive.addAll(getPlayerDataList());
        alive.removeIf(d -> !d.isPlayerOnline());
        getQueue().clear();
        getQueue().addAll(alive);
        getOnlinePlayers().forEach(p -> p.sendMessage(Component.text("相撲流程\n淘汰賽->四分之一決賽(八強)->半決賽(四強)->季軍賽->決賽")));
        int stageRound;
        if (getQueue().size() <= 4) {
            setSumoStage(SumoStage.SEMI_FINAL);
            stageRound = 2;
        } else if (getQueue().size() <= 8) {
            setSumoStage(SumoStage.QUARTER_FINAL);
            stageRound = getQueue().size() - 4;
        } else {
            stageRound = getQueue().size() - 8;
        }
        for (int i = 0; i < stageRound; i++) {
            getSumoStage().getRoundList().add(new SumoRound(getFromQueue(), getFromQueue()));
        }
        stageSetup();
    }

    private void stageSetup() {
        StringBuilder sb = new StringBuilder("現階段為" + getSumoStage().getName() + "\n");
        for (int i = 0; i < getSumoStage().getRoundList().size(); i++) {
            SumoRound r = getSumoStage().getRoundList().get(i);
            sb.append("第").append(i + 1).append("場由").append(r.getPlayers().get(0).getName()).append("對戰").append(r.getPlayers().get(1).getName()).append("\n");
        }
        getOnlinePlayers().forEach(p -> p.sendMessage(sb.substring(0, sb.length() - 1)));
    }

    @Override
    public void onStart() {
        onRoundStart();
    }

    @Override
    public void onEnd(boolean force) {
        if (force) return;
        getLeaderboard().getEntry().add(1, alive.get(1));
        getLeaderboard().getEntry().add(0, alive.get(0));
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (PlayerData data : getLeaderboard().getEntry()) {
            sb.append("第").append(++i).append("名 ").append(data.getName()).append("\n");
        }
        getOnlinePlayers().forEach(p -> p.sendMessage(sb.substring(0, sb.length() - 1)));
    }

    @Override
    public <T extends Event> void onEvent(T event) {
        if (event instanceof PlayerMoveEvent e) {
            Player p = e.getPlayer();
            SumoRound round = getSumoStage().getCurrentRound();
            if (!round.containPlayer(p)) return;
            if (round.getStatus() == SumoRound.RoundStatus.COMING) {
                e.setCancelled(true);
                return;
            }
            if (p.getLocation().getBlock().getType() != Material.WATER || round.getStatus() != SumoRound.RoundStatus.STARTED) return;
            round.setResult(round.getPlayers().get(0).equals(p) ? round.getPlayers().get(1) : round.getPlayers().get(0), p);
            onRoundEnd();
        }
    }

    private @NotNull Player getFromQueue() {
        PlayerData data = getQueue().get(new Random().nextInt(getQueue().size()));
        getQueue().remove(data);
        return data.getPlayer();
    }

    @Override
    public void onRoundStart() {
        getSumoStage().nextRound();
        getSumoStage().getCurrentRound().setStatus(SumoRound.RoundStatus.COMING);
        List<Player> pl = getSumoStage().getCurrentRound().getPlayers();
        pl.get(0).teleport(Objects.requireNonNull(SportsDay.getInstance().getConfig().getLocation(getID() + ".p1-location")));
        pl.get(1).teleport(Objects.requireNonNull(SportsDay.getInstance().getConfig().getLocation(getID() + ".p2-location")));
        addRunnable(new BukkitRunnable() {
            int i = 5;
            @Override
            public void run() {
                if (i != 0) {
                    getOnlinePlayers().forEach(p -> p.sendActionBar(Component.text(i + "秒後開始").color(NamedTextColor.YELLOW)));
                }
                if (i-- == 0) {
                    getSumoStage().getCurrentRound().setStatus(SumoRound.RoundStatus.STARTED);
                    getOnlinePlayers().forEach(p -> p.sendActionBar(Component.text("回合開始").color(NamedTextColor.YELLOW)));
                    this.cancel();
                }
            }
        }.runTaskTimer(SportsDay.getInstance(), 0L, 20L));
    }

    @Override
    public void onRoundEnd() {
        SumoRound round = getSumoStage().getCurrentRound();
        getWorld().strikeLightningEffect(round.getLoser().getLocation());
        getOnlinePlayers().forEach(p -> {
            p.sendActionBar(Component.text("回合結束").color(NamedTextColor.YELLOW));
            p.sendMessage(Component.translatable("%s獲得了本場勝利").args(Component.text(round.getWinner().getName())).color(NamedTextColor.YELLOW));
        });
        if (getSumoStage() != SumoStage.SEMI_FINAL) {
            for (PlayerData data : alive) {
                if (data.getUUID().equals(round.getLoser().getUniqueId())) {
                    getLeaderboard().getEntry().add(0, data);
                    alive.remove(data);
                    break;
                }
            }
        }
        // After sumo stage
        if (getSumoStage() == SumoStage.SEMI_FINAL) {
            theFinal[getSumoStage().getRoundIndex() - 1] = round.getWinner();
            thirdPlace[getSumoStage().getRoundIndex() - 1] = round.getLoser();
        } else if (getSumoStage() == SumoStage.QUARTER_FINAL) {
            semiFinal[getSumoStage().getRoundIndex() - 1] = round.getWinner();
        }
        if (getSumoStage().getRoundRemaining() != 0) {
            nextRound();
        } else {
            if (getSumoStage() != SumoStage.FINAL) {
                nextSumoStage();
            } else {
                end(false);
            }
        }
    }

    @Override
    public void nextRound() {
        addRunnable(new BukkitRunnable() {
            int i = 5;
            @Override
            public void run() {
                getOnlinePlayers().forEach(p -> p.sendActionBar(Component.text(i + "秒後進行下一場比賽").color(NamedTextColor.YELLOW)));
                if (i-- == 0) {
                    getSumoStage().getCurrentRound().getPlayers().forEach(p -> p.teleport(getLocation()));
                    onRoundStart();
                    this.cancel();
                }
            }
        }.runTaskTimer(SportsDay.getInstance(), 0L, 20L));
    }

    private void nextSumoStage() {
        if (alive.size() <= 8 && getSumoStage().hasNextStage()) {
            setSumoStage(getSumoStage().getNextStage());
        }
        // Before sumo stage
        if (getSumoStage() == SumoStage.FINAL) {
            getSumoStage().getRoundList().add(new SumoRound(theFinal[0], theFinal[1]));
        } else if (getSumoStage() == SumoStage.THIRD_PLACE) {
            getSumoStage().getRoundList().add(new SumoRound(thirdPlace[0], thirdPlace[1]));
        } else if (getSumoStage() == SumoStage.SEMI_FINAL) {
            for (int i = 0; i < alive.size(); i++) {
                getSumoStage().getRoundList().add(new SumoRound(semiFinal[i * 2], semiFinal[i * 2 + 1]));
            }
        } else if (getSumoStage() == SumoStage.QUARTER_FINAL) {
            for (int i = 0; i < alive.size(); i++) {
                getSumoStage().getRoundList().add(new SumoRound(getFromQueue(), getFromQueue()));
            }
        } else {
            for (int i = 0; i < alive.size() - 8; i++) {
                getSumoStage().getRoundList().add(new SumoRound(getFromQueue(), getFromQueue()));
            }
        }
        stageSetup();
        addRunnable(new BukkitRunnable() {
            int i = 15;
            @Override
            public void run() {
                if (i == 10 || (i <= 5 && i > 0)) {
                    getOnlinePlayers().forEach(p -> p.sendActionBar(Component.text(i + "秒後進入下一場階段").color(NamedTextColor.YELLOW)));
                }
                if (i-- == 0) {
                    this.cancel();
                }
            }
        }.runTaskTimer(SportsDay.getInstance(), 0L, 20L));
    }

    public SumoStage getSumoStage() {
        return sumoStage;
    }

    private void setSumoStage(SumoStage sumoStage) {
        this.sumoStage = sumoStage;
    }

    @Override
    public Leaderboard<PlayerData> getLeaderboard() {
        return leaderboard;
    }

    @Override
    public List<PlayerData> getQueue() {
        return this.queue;
    }
}
