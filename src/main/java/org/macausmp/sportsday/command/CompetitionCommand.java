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
                                    sender.sendMessage(Component.text("參賽選手人數不足，無法開始比賽，最少需要" + competition.getLeastPlayersRequired() + "人開始比賽").color(NamedTextColor.RED));
                                }
                                return;
                            }
                        }
                        sender.sendMessage(Component.text("未知的比賽項目").color(NamedTextColor.RED));
                    } else {
                        StringBuilder sb = new StringBuilder("(");
                        Competitions.COMPETITIONS.forEach(c -> sb.append(c.getID()).append("|"));
                        sb.replace(sb.length(), sb.length(), ")");
                        sender.sendMessage(Component.text("/competition start " + sb));
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
                            int number = 0;
                            if (args.length >= 3) {
                                try {
                                    number = Integer.parseInt(args[2]);
                                    if (number < 0) {
                                        sender.sendMessage(Component.text("選手號碼必須是正整數").color(NamedTextColor.RED));
                                        return;
                                    }
                                } catch (Exception e) {
                                    sender.sendMessage(Component.translatable("parsing.int.invalid").args(Component.text(args[2])).color(NamedTextColor.RED));
                                }
                            } else {
                                number = Competitions.genNumber();
                            }
                            sender.sendMessage(Component.text(Competitions.join(p, number) ? ("已添加" + p.getName() + "為參賽選手，選手號碼為" + number + "號") : ("添加失敗，編號為" + number + "的選手號碼已經被使用了，請選擇其他號碼")));
                        } else {
                            sender.sendMessage(Component.text(p.getName() + "已經在參賽選手名單上"));
                        }
                    } else {
                        sender.sendMessage(Component.text("/competition join <player>"));
                    }
                    break;
                case "leave":
                    if (args.length >= 2) {
                        OfflinePlayer p = Bukkit.getOfflinePlayer(args[1]);
                        if (p.getFirstPlayed() == 0L) {
                            sender.sendMessage(Component.translatable("argument.player.unknown").color(NamedTextColor.RED));
                            return;
                        }
                        sender.sendMessage(Competitions.leave(p) ? Component.text("已將" + p.getName() + "從參賽選手名單中移除") : Component.text(p.getName() + "不在參賽選手名單上"));
                    } else {
                        if (sender instanceof Player p) {
                            Competitions.leave(p);
                            return;
                        }
                        sender.sendMessage(Component.text("/competition leave <player>"));
                    }
                    break;
                case "info":
                    sender.sendMessage(Component.text("比賽資訊"));
                    sender.sendMessage(Component.text("比賽狀態: " + (Competitions.getCurrentlyCompetition() == null ? "未開始" : Competitions.getCurrentlyCompetition().getStage().name())));
                    if (Competitions.getCurrentlyCompetition() != null) {
                        sender.sendMessage(Component.text("當前比賽: " + Competitions.getCurrentlyCompetition().getID().toUpperCase()));
                    }
                    sender.sendMessage(Component.text("參賽人數: " + Competitions.getPlayerDataList().size()));
                    List<String> l = new ArrayList<>();
                    for (ICompetition c : Competitions.COMPETITIONS) {
                        if (c.isEnable()) {
                            l.add(c.getID().toUpperCase());
                        }
                    }
                    sender.sendMessage(Component.text("可用比賽: " + Arrays.toString(l.toArray())));
                    break;
                default:
                    sender.sendMessage(Component.translatable("command.unknown.argument").color(NamedTextColor.RED));
                    break;
            }
        } else {
            sender.sendMessage(Component.text("/competition (start|end|join|leave|info) <argument>"));
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
                    l.add(Bukkit.getOfflinePlayer(data.getUUID()).getName());
                }
            }
        } else {
            l.add("start");
            l.add("end");
            l.add("join");
            l.add("leave");
            l.add("info");
        }
        return l;
    }
}
