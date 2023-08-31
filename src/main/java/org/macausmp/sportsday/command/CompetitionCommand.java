package org.macausmp.sportsday.command;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.competition.Competitions;
import org.macausmp.sportsday.competition.IEvent;
import org.macausmp.sportsday.util.PlayerData;
import org.macausmp.sportsday.util.TextUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CompetitionCommand implements IPluginCommand {
    @Override
    public void onCommand(CommandSender sender, String @NotNull [] args) {
        if (args.length > 0) {
            switch (args[0]) {
                case "start" -> {
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
                                        sender.sendMessage(Component.translatable("argument.register.negative").color(NamedTextColor.RED));
                                        return;
                                    }
                                } catch (Exception e) {
                                    sender.sendMessage(Component.translatable("parsing.int.invalid").args(Component.text(args[2])).color(NamedTextColor.RED));
                                    return;
                                }
                            } else {
                                number = Competitions.genNumber();
                            }
                            sender.sendMessage(Competitions.join(p, number) ? Component.translatable("player.register_success").args(p.displayName(), Component.text(number)).color(NamedTextColor.GREEN) : Component.translatable("player.register_number_occupied").args(Component.text(number)).color(NamedTextColor.RED));
                        } else {
                            sender.sendMessage(Component.translatable("player.registered").args(p.displayName()).color(NamedTextColor.RED));
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
                        sender.sendMessage(Competitions.leave(p) ? Component.translatable("player.leave").args(Component.text(p.getName())).color(NamedTextColor.GREEN) : Component.translatable("player.unregistered").args(Component.text(p.getName())).color(NamedTextColor.RED));
                    } else {
                        if (sender instanceof Player p) {
                            if (!Competitions.leave(p)) {
                                sender.sendMessage(Component.translatable("player.unregistered_message").color(NamedTextColor.RED));
                            }
                            return;
                        }
                        sender.sendMessage(Component.text("/competition leave <player>"));
                    }
                }
                case "score" -> {
                    if (args.length >= 2) {
                        OfflinePlayer p = Bukkit.getOfflinePlayer(args[1]);
                        if (p.getName() == null || p.getFirstPlayed() == 0L) {
                            sender.sendMessage(Component.translatable("argument.player.unknown").color(NamedTextColor.RED));
                            return;
                        }
                        if (Competitions.containPlayer(p)) {
                            if (args.length == 2) {
                                sender.sendMessage(Component.translatable("player.score.query").args(Component.text(p.getName()), Component.text(Competitions.getPlayerData(p.getUniqueId()).getScore())).color(NamedTextColor.GREEN));
                                return;
                            }
                            int score;
                            try {
                                score = Integer.parseInt(args[2]);
                                if (score < 0) {
                                    sender.sendMessage(Component.translatable("argument.score.negative"));
                                    return;
                                }
                            } catch (Exception e) {
                                sender.sendMessage(Component.translatable("parsing.int.invalid").args(Component.text(args[2])).color(NamedTextColor.RED));
                                return;
                            }
                            PlayerData data = Competitions.getPlayerData(p.getUniqueId());
                            data.setScore(score);
                            sender.sendMessage(Component.translatable("player.score.success_set").args(Component.text(p.getName()), Component.text(data.getScore())).color(NamedTextColor.GREEN));
                        } else {
                            sender.sendMessage(Component.translatable("player.unregistered").args(Component.text(p.getName())));
                        }
                    } else {
                        sender.sendMessage(Component.text("/competition score <player> <new_score>"));
                    }
                }
                case "info" -> {
                    sender.sendMessage(Component.translatable("gui.info.title"));
                    boolean b = Competitions.getCurrentlyEvent() != null;
                    sender.sendMessage(Component.translatable("competition.current").color(NamedTextColor.GREEN).args(b ? Competitions.getCurrentlyEvent().getName() : TextUtil.convert(Component.translatable("gui.none"))));
                    if (b) sender.sendMessage(Component.translatable("competition.stage").color(NamedTextColor.GREEN).args(Competitions.getCurrentlyEvent().getStage().getName()));
                    sender.sendMessage(Component.translatable("competition.players").color(NamedTextColor.GREEN).args(Component.text(Competitions.getPlayerData().size()).color(NamedTextColor.YELLOW)));
                    List<String> pl = new ArrayList<>();
                    Competitions.getPlayerData().forEach(d -> pl.add(d.getName()));
                    sender.sendMessage(Component.translatable("competition.players_name").color(NamedTextColor.GREEN).args(Component.text(Arrays.toString(pl.toArray())).color(NamedTextColor.YELLOW)));
                    List<String> cl = new ArrayList<>();
                    for (IEvent c : Competitions.COMPETITIONS) {
                        if (c.isEnable()) cl.add(c.getID().toUpperCase());
                    }
                    sender.sendMessage(Component.translatable("competition.enabled").color(NamedTextColor.GREEN).args(Component.text(Arrays.toString(cl.toArray())).color(NamedTextColor.YELLOW)));
                }
                default -> sender.sendMessage(Component.translatable("command.unknown.argument").color(NamedTextColor.RED));
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
                    if (c.isEnable()) l.add(c.getID());
                });
                case "join" -> {
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        if (!Competitions.containPlayer(p)) l.add(p.getName());
                    }
                }
                case "leave", "score" -> Competitions.getPlayerData().forEach(d -> l.add(d.getName()));
            }
        }
        return l;
    }
}
