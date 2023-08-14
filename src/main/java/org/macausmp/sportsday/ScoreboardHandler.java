package org.macausmp.sportsday;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.*;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.competition.Competitions;
import org.macausmp.sportsday.util.Translation;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class ScoreboardHandler {
    private final SportsDay plugin = SportsDay.getInstance();
    private final String title = Translation.translate("scoreboard.title");

    public void setScoreboard(Player p) {
        // Tick time correction (attempt to bring the timer closer to reality)
        long d = Math.round(20f - LocalDateTime.now().getNano() / 50000000f);
        new BukkitRunnable() {
            @Override
            public void run() {
                ScoreboardManager manager = plugin.getServer().getScoreboardManager();
                Scoreboard scoreboard = manager.getNewScoreboard();
                Objective o = scoreboard.registerNewObjective("sportsday", Criteria.DUMMY, Component.text(title).color(NamedTextColor.GOLD));
                o.setDisplaySlot(DisplaySlot.SIDEBAR);
                Entry comp = new Entry(o, "competition");
                Entry stage = new Entry(o, "stage");
                Entry count = new Entry(o, "player_count").setScore(8);
                Entry number = new Entry(o, "number");
                Entry score = new Entry(o, "score");
                Entry time = new Entry(o, "time").setScore(4);
                Entry ping = new Entry(o, "ping").setScore(3);
                newline(o).setScore(5);
                newline(o).setScore(2);
                o.getScore(Objects.requireNonNull(plugin.getConfig().getString("server_ip"))).setScore(1);
                new BukkitRunnable() {
                    final Score line = newline(o);
                    @Override
                    public void run() {
                        if (Competitions.getCurrentlyCompetition() != null) {
                            comp.suffix(Competitions.getCurrentlyCompetition().getName());
                            stage.suffix(Competitions.getCurrentlyCompetition().getStage().getName());
                            comp.setScore(11);
                            stage.setScore(10);
                            line.setScore(9);
                        } else {
                            comp.resetScore();
                            stage.resetScore();
                            line.resetScore();
                        }
                        count.suffix(Component.translatable("%s/%s").args(Component.text(Competitions.getOnlinePlayers().size()), Component.text(plugin.getServer().getOnlinePlayers().size())));
                        if (Competitions.containPlayer(p)) {
                            number.suffix(Component.text(Competitions.getPlayerData(p.getUniqueId()).getNumber()));
                            score.suffix(Component.text(Competitions.getPlayerData(p.getUniqueId()).getScore()));
                            number.setScore(7);
                            score.setScore(6);
                        } else {
                            number.resetScore();
                            score.resetScore();
                        }
                        time.suffix(Component.text(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))));
                        ping.suffix(Component.text(p.getPing()));
                        p.setScoreboard(scoreboard);
                    }
                }.runTaskTimer(plugin, 0L, 20L);
            }
        }.runTaskLater(plugin, d);
    }

    private @NotNull Score newline(@NotNull Objective o) {
        StringBuilder sb = new StringBuilder(" ");
        while (o.getScore(sb.toString()).isScoreSet()) {
            sb.append(" ");
        }
        return o.getScore(sb.toString());
    }

    protected static class Entry {
        private final Objective objective;
        protected final Team team;
        protected final String entry;

        protected Entry(@NotNull Objective objective, String entry) {
            this.objective = objective;
            this.team = Objects.requireNonNull(objective.getScoreboard()).registerNewTeam(entry);
            this.entry = Translation.translate("scoreboard." + entry);
            this.team.addEntry(this.entry);
        }

        public void suffix(Component suffix) {
            team.suffix(suffix);
        }

        public Entry setScore(int score) {
            objective.getScore(entry).setScore(score);
            return this;
        }

        public void resetScore() {
            objective.getScore(entry).resetScore();
        }
    }
}
