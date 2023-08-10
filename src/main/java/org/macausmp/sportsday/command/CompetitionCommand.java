package org.macausmp.sportsday.command;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.PlayerData;
import org.macausmp.sportsday.competition.Competitions;
import org.macausmp.sportsday.competition.ICompetition;
import org.macausmp.sportsday.competition.Stage;
import org.macausmp.sportsday.util.Translation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CompetitionCommand implements IPluginCommand {
    @Override
    public void onCommand(CommandSender sender, String @NotNull [] args) {
        if (args.length > 0) {
            switch (args[0]) {
                case "start" -> {
                    if (Competitions.getCurrentlyCompetition() != null && Competitions.getCurrentlyCompetition().getStage() != Stage.ENDED) {
                        sender.sendMessage(Translation.translatable("competition.already_in_progress"));
                        return;
                    }
                    if (args.length >= 2) {
                        Competitions.start(sender, args[1]);
                    } else {
                        StringBuilder sb = new StringBuilder("(");
                        Competitions.COMPETITIONS.forEach(c -> sb.append(c.getID()).append("|"));
                        sender.sendMessage("/competition start " + sb.replace(sb.length(), sb.length(), ")"));
                    }
                }
                case "end" -> Competitions.forceEnd(sender);
                case "join" -> {
                    if (args.length >= 2) {
                        Player p = Bukkit.getPlayer(args[1]);
                        if (p == null) {
                            sender.sendMessage(Component.translatable("argument.player.unknown").color(NamedTextColor.RED));
                            return;
                        }
                        if (!Competitions.containPlayer(p)) {
                            int number;
                            if (args.length >= 3) {
                                try {
                                    number = Integer.parseInt(args[2]);
                                    if (number < 0) {
                                        sender.sendMessage(Translation.translatable("argument.register.negative"));
                                        return;
                                    }
                                } catch (Exception e) {
                                    sender.sendMessage(Component.translatable("parsing.int.invalid").args(Component.text(args[2])).color(NamedTextColor.RED));
                                    return;
                                }
                            } else {
                                number = Competitions.genNumber();
                            }
                            sender.sendMessage(Competitions.join(p, number) ? Translation.translatable("player.register_success").args(p.displayName(), Component.text(number)).color(NamedTextColor.GREEN) : Translation.translatable("player.register_number_occupied").args(Component.text(number)).color(NamedTextColor.RED));
                        } else {
                            sender.sendMessage(Translation.translatable("player.registered").args(p.displayName()).color(NamedTextColor.RED));
                        }
                    } else {
                        sender.sendMessage(Component.text("/competition join <player>"));
                    }
                }
                case "leave" -> {
                    if (args.length >= 2) {
                        OfflinePlayer p = Bukkit.getOfflinePlayer(args[1]);
                        if (p.getName() == null || p.getFirstPlayed() == 0L) {
                            sender.sendMessage(Component.translatable("argument.player.unknown").color(NamedTextColor.RED));
                            return;
                        }
                        sender.sendMessage(Competitions.leave(p) ? Translation.translatable("player.leave").args(Component.text(p.getName())).color(NamedTextColor.GREEN) : Translation.translatable("player.unregistered").args(Component.text(p.getName())).color(NamedTextColor.RED));
                    } else {
                        if (sender instanceof Player p) {
                            if (!Competitions.leave(p)) {
                                sender.sendMessage(Translation.translatable("player.unregistered_message"));
                            }
                            return;
                        }
                        sender.sendMessage(Component.text("/competition leave <player>"));
                    }
                }
                case "score" -> {
                    if (args.length >= 3) {
                        OfflinePlayer p = Bukkit.getOfflinePlayer(args[1]);
                        if (p.getName() == null || p.getFirstPlayed() == 0L) {
                            sender.sendMessage(Component.translatable("argument.player.unknown").color(NamedTextColor.RED));
                            return;
                        }
                        if (Competitions.containPlayer(p)) {
                            int score;
                            try {
                                score = Integer.parseInt(args[2]);
                                if (score < 0) {
                                    sender.sendMessage(Translation.translatable("argument.score.negative"));
                                    return;
                                }
                            } catch (Exception e) {
                                sender.sendMessage(Component.translatable("parsing.int.invalid").args(Component.text(args[2])).color(NamedTextColor.RED));
                                return;
                            }
                            PlayerData data = Competitions.getPlayerData(p.getUniqueId());
                            data.setScore(score);
                            sender.sendMessage(Translation.translatable("score.success_set").args(Component.text(p.getName()), Component.text(data.getScore())).color(NamedTextColor.GREEN));
                        } else {
                            sender.sendMessage(Translation.translatable("player.unregistered").args(Component.text(p.getName())));
                        }
                    } else {
                        sender.sendMessage(Component.text("/competition score <player> <new_score>"));
                    }
                }
                case "info" -> {
                    sender.sendMessage(Translation.translatable("gui.title.info"));
                    boolean b = Competitions.getCurrentlyCompetition() != null;
                    sender.sendMessage(Translation.translatable("competition.current").args(b ? Competitions.getCurrentlyCompetition().getName() : Translation.translatable("gui.none")));
                    if (b) {
                        sender.sendMessage(Translation.translatable("competition.stage").args(Competitions.getCurrentlyCompetition().getStage().getName()));
                    }
                    sender.sendMessage(Translation.translatable("competition.players").args(Component.text(Competitions.getPlayerDataList().size()).color(NamedTextColor.YELLOW)));
                    List<String> pl = new ArrayList<>();
                    Competitions.getPlayerDataList().forEach(d -> pl.add(d.getName()));
                    sender.sendMessage(Translation.translatable("competition.players_name").args(Component.text(Arrays.toString(pl.toArray())).color(NamedTextColor.YELLOW)));
                    List<String> cl = new ArrayList<>();
                    for (ICompetition c : Competitions.COMPETITIONS) {
                        if (c.isEnable()) {
                            cl.add(c.getID().toUpperCase());
                        }
                    }
                    sender.sendMessage(Translation.translatable("competition.enabled").args(Component.text(Arrays.toString(cl.toArray())).color(NamedTextColor.YELLOW)));
                }
                default ->
                        sender.sendMessage(Component.translatable("command.unknown.argument").color(NamedTextColor.RED));
            }
        } else {
            sender.sendMessage(Component.text("/competition (start|end|join|leave|score|info) <argument>"));
        }
    }

    @Override
    public String name() {
        return "competition";
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        List<String> l = new ArrayList<>();
        if (args.length == 1) {
            l.add("start");
            l.add("end");
            l.add("join");
            l.add("leave");
            l.add("score");
            l.add("info");
        } else if (args.length == 2) {
            switch (args[0]) {
                case "start" -> Competitions.COMPETITIONS.forEach(c -> {
                    if (c.isEnable()) {
                        l.add(c.getID());
                    }
                });
                case "join" -> {
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        if (!Competitions.containPlayer(p)) {
                            l.add(p.getName());
                        }
                    }
                }
                case "leave", "score" -> Competitions.getPlayerDataList().forEach(d -> l.add(d.getName()));
            }
        }
        return l;
    }
}
