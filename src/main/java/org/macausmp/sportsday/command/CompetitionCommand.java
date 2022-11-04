package org.macausmp.sportsday.command;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.macausmp.sportsday.PlayerData;
import org.macausmp.sportsday.competition.Competitions;
import org.macausmp.sportsday.competition.ICompetition;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class CompetitionCommand extends PluginCommand {
    @Override
    public void onCommand(CommandSender sender, String @NotNull [] args) {
        if (args.length > 0) {
            switch (args[0]) {
                case "start":
                    if (Competitions.getCurrentlyCompetition() != null) {
                        sender.sendMessage(Component.text("已經有一場比賽正在進行中...").color(NamedTextColor.RED));
                        return;
                    }
                    if (args.length >= 2) {
                        for (ICompetition competition : Competitions.COMPETITIONS) {
                            if (competition.getID().equals(args[1])) {
                                if (!competition.isEnable()) {
                                    sender.sendMessage(Component.text("該比賽項目已被禁用").color(NamedTextColor.RED));
                                    return;
                                }
                                if (Competitions.getPlayerDataList().size() >= competition.getLeastPlayersRequired()) {
                                    sender.sendMessage(Component.text("開始新一場比賽中..."));
                                    Competitions.setCurrentlyCompetition(competition);
                                    competition.setup();
                                } else {
                                    sender.sendMessage(Component.translatable("參賽選手人數不足，無法開始比賽，最少需要%s人開始比賽").args(Component.text(competition.getLeastPlayersRequired())).color(NamedTextColor.RED));
                                }
                                return;
                            }
                        }
                        sender.sendMessage(Component.text("未知的比賽項目").color(NamedTextColor.RED));
                    } else {
                        StringBuilder sb = new StringBuilder("(");
                        Competitions.COMPETITIONS.forEach(c -> sb.append(c.getID()).append("|"));
                        sb.replace(sb.length(), sb.length(), ")");
                        sender.sendMessage("/competition start " + sb);
                    }
                    break;
                case "end":
                    if (Competitions.getCurrentlyCompetition() != null) {
                        Competitions.getCurrentlyCompetition().end(true);
                        sender.sendMessage(Component.text("已強制結束一場比賽"));
                    } else {
                        sender.sendMessage(Component.text("現在沒有比賽進行中"));
                    }
                    break;
                case "join":
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
                                        sender.sendMessage(Component.text("選手號碼必須是正整數").color(NamedTextColor.RED));
                                        return;
                                    }
                                } catch (Exception e) {
                                    sender.sendMessage(Component.translatable("parsing.int.invalid").args(Component.text(args[2])).color(NamedTextColor.RED));
                                    return;
                                }
                            } else {
                                number = Competitions.genNumber();
                            }
                            sender.sendMessage(Competitions.join(p, number) ? Component.translatable("已添加%s為參賽選手，選手號碼為%s號").args(Component.text(p.getName()), Component.text(number)) : Component.translatable("添加失敗，編號為%s的選手號碼已經被使用了，請選擇其他號碼").args(Component.text(number)));
                        } else {
                            sender.sendMessage(Component.translatable("%s已經在參賽選手名單上").args(Component.text(p.getName())).color(NamedTextColor.RED));
                        }
                    } else {
                        sender.sendMessage(Component.text("/competition join <player>"));
                    }
                    break;
                case "leave":
                    if (args.length >= 2) {
                        OfflinePlayer p = Bukkit.getOfflinePlayer(args[1]);
                        if (p.getName() == null || p.getFirstPlayed() == 0L) {
                            sender.sendMessage(Component.translatable("argument.player.unknown").color(NamedTextColor.RED));
                            return;
                        }
                        sender.sendMessage(Competitions.leave(p) ? Component.translatable("已將%s從參賽選手名單中移除").args(Component.text(p.getName())) : Component.translatable( "%s不在參賽選手名單上").args(Component.text(p.getName())).color(NamedTextColor.RED));
                    } else {
                        if (sender instanceof Player p) {
                            if (!Competitions.leave(p)) {
                                sender.sendMessage(Component.text("你不在參賽選手名單上").color(NamedTextColor.RED));
                            }
                            return;
                        }
                        sender.sendMessage(Component.text("/competition leave <player>"));
                    }
                    break;
                case "score":
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
                                    sender.sendMessage(Component.text("分數必須是正整數").color(NamedTextColor.RED));
                                    return;
                                }
                            } catch (Exception e) {
                                sender.sendMessage(Component.translatable("parsing.int.invalid").args(Component.text(args[2])).color(NamedTextColor.RED));
                                return;
                            }
                            PlayerData data = Objects.requireNonNull(Competitions.getPlayerData(p.getUniqueId()));
                            data.setScore(score);
                            sender.sendMessage(Component.translatable("已為%s設置新的分數%s").args(Component.text(data.getName()), Component.text(data.getScore())).color(NamedTextColor.GREEN));
                        } else {
                            sender.sendMessage(Component.text("該玩家不在參賽選手名單上").color(NamedTextColor.RED));
                        }
                    } else {
                        sender.sendMessage(Component.text("/competition score <player> <new_score>"));
                    }
                    break;
                case "info":
                    sender.sendMessage(Component.text("比賽資訊"));
                    sender.sendMessage(Component.translatable("比賽狀態: %s").args(Component.text(Competitions.getCurrentlyCompetition() == null ? "未開始" : Competitions.getCurrentlyCompetition().getStage().name())));
                    if (Competitions.getCurrentlyCompetition() != null) {
                        sender.sendMessage(Component.translatable("當前比賽: %s").args(Component.text(Competitions.getCurrentlyCompetition().getID().toUpperCase())));
                    }
                    sender.sendMessage(Component.translatable("參賽人數: %s").args(Component.text(Competitions.getPlayerDataList().size())));
                    List<String> l = new ArrayList<>();
                    for (ICompetition c : Competitions.COMPETITIONS) {
                        if (c.isEnable()) {
                            l.add(c.getID().toUpperCase());
                        }
                    }
                    sender.sendMessage(Component.translatable("可用比賽: %s").args(Component.text(Arrays.toString(l.toArray()))));
                    break;
                default:
                    sender.sendMessage(Component.translatable("command.unknown.argument").color(NamedTextColor.RED));
                    break;
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
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        List<String> l = new ArrayList<>();
        if (args.length > 1) {
            if (args.length == 2 && args[0].equals("start")) {
                Competitions.COMPETITIONS.forEach(c -> {
                    if (c.isEnable()) {
                        l.add(c.getID());
                    }
                });
            } else if (args[0].equals("join")) {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (!Competitions.containPlayer(p)) {
                        l.add(p.getName());
                    }
                }
            } else if (args[0].equals("leave")) {
                for (PlayerData data : Competitions.getPlayerDataList()) {
                    l.add(data.getName());
                }
            } else if (args[0].equals("score")) {
                for (PlayerData data : Competitions.getPlayerDataList()) {
                    l.add(data.getName());
                }
            }
        } else {
            l.add("start");
            l.add("end");
            l.add("join");
            l.add("leave");
            l.add("score");
            l.add("info");
        }
        return l;
    }
}
