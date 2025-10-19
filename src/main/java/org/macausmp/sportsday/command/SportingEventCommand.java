package org.macausmp.sportsday.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.macausmp.sportsday.ContestantData;
import org.macausmp.sportsday.SportsDay;
import org.macausmp.sportsday.SportsRegistry;
import org.macausmp.sportsday.event.SportingEvent;
import org.macausmp.sportsday.sport.Sport;
import org.macausmp.sportsday.util.TextUtil;
import org.macausmp.sportsday.venue.Venue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

public class SportingEventCommand extends PluginCommand {
    @Override
    public String name() {
        return "event";
    }

    @Override
    public String description() {
        return "Event management command";
    }

    @Override
    protected Function<LiteralArgumentBuilder<CommandSourceStack>, LiteralArgumentBuilder<CommandSourceStack>> logic() {
        return literal -> literal
                .requires(sender -> sender.getSender().isOp())
                .then(Commands.literal("start")
                        .then(Commands.argument("sport", new SportArgument())
                                .then(Commands.argument("venue", StringArgumentType.string())
                                        .suggests((ctx, builder) -> {
                                            Sport sport = ctx.getArgument("sport", Sport.class);
                                            sport.getVenues().forEach(v -> builder.suggest(v.getName()));
                                            return builder.buildFuture();
                                        })
                                        .executes(ctx -> {
                                            Sport sport = ctx.getArgument("sport", Sport.class);
                                            String parsed = ctx.getArgument("venue", String.class);
                                            Venue venue = sport.getVenues().stream().filter(v -> v.getName().equals(parsed)).findFirst().orElse(null);
                                            if (venue == null) {
                                                ctx.getSource().getSender().sendMessage(Component.translatable("argument.venue.invalid")
                                                        .arguments(Component.text(sport.getKey().getKey()), Component.text(parsed))
                                                        .color(NamedTextColor.RED));
                                                return Command.SINGLE_SUCCESS;
                                            }
                                            SportsDay.startEvent(ctx.getSource().getSender(), sport, venue);
                                            return Command.SINGLE_SUCCESS;
                                        })
                                )
                        )
                )
                .then(Commands.literal("load")
                        .executes(ctx -> {
                            SportsDay.saveEvent(ctx.getSource().getSender());
                            return Command.SINGLE_SUCCESS;
                        })
                )
                .then(Commands.literal("save")
                        .executes(ctx -> {
                            SportsDay.saveEvent(ctx.getSource().getSender());
                            return Command.SINGLE_SUCCESS;
                        })
                )
                .then(Commands.literal("pause")
                        .executes(ctx -> {
                            SportsDay.pause(ctx.getSource().getSender());
                            return Command.SINGLE_SUCCESS;
                        })
                )
                .then(Commands.literal("unpause")
                        .executes(ctx -> {
                            SportsDay.unpause(ctx.getSource().getSender());
                            return Command.SINGLE_SUCCESS;
                        })
                )
                .then(Commands.literal("terminate")
                        .executes(ctx -> {
                            SportsDay.terminate(ctx.getSource().getSender());
                            return Command.SINGLE_SUCCESS;
                        })
                )
                .then(Commands.literal("info")
                        .executes(ctx -> {
                            CommandSender sender = ctx.getSource().getSender();
                            sender.sendMessage(Component.translatable("event.info"));
                            SportingEvent event = SportsDay.getCurrentEvent();
                            boolean hasEvent = event != null;
                            sender.sendMessage(Component.translatable("event.current").color(NamedTextColor.GREEN)
                                    .arguments(hasEvent ? event : TextUtil.convert(Component.translatable("gui.text.none"))));
                            if (hasEvent)
                                sender.sendMessage(Component.translatable("event.status").color(NamedTextColor.GREEN)
                                        .arguments(event.getStatus()));
                            sender.sendMessage(Component.translatable("event.contestants.total").color(NamedTextColor.GREEN)
                                    .arguments(Component.text(SportsDay.getContestants().size()).color(NamedTextColor.YELLOW)));
                            sender.sendMessage(Component.translatable("event.contestants.online").color(NamedTextColor.GREEN)
                                    .arguments(Component.text(SportsDay.getOnlineContestants().size()).color(NamedTextColor.YELLOW)));
                            List<String> pl = new ArrayList<>();
                            SportsDay.getContestants().stream().sorted(Comparator.comparingInt(ContestantData::getNumber))
                                    .forEach(d -> pl.add(d.getName()));
                            sender.sendMessage(Component.translatable("event.contestants.list").color(NamedTextColor.GREEN)
                                    .arguments(Component.text(Arrays.toString(pl.toArray())).color(NamedTextColor.YELLOW)));
                            List<String> el = new ArrayList<>();
                            SportsRegistry.SPORT.stream().filter(s -> s.getSetting(Sport.Settings.ENABLE))
                                    .forEach(e -> el.add(e.getKey().getKey().toUpperCase()));
                            sender.sendMessage(Component.translatable("event.enabled").color(NamedTextColor.GREEN)
                                    .arguments(Component.text(Arrays.toString(el.toArray())).color(NamedTextColor.YELLOW)));
                            return Command.SINGLE_SUCCESS;
                        })
                )
                .executes(ctx -> {
                    ctx.getSource().getSender().sendMessage(Component.translatable("command.unknown.argument").color(NamedTextColor.RED));
                    return Command.SINGLE_SUCCESS;
                });
    }
}
