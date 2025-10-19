package org.macausmp.sportsday.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.macausmp.sportsday.ContestantData;
import org.macausmp.sportsday.SportsDay;

import java.util.function.Function;

public class ContestantCommand extends PluginCommand {
    @Override
    public String name() {
        return "contestant";
    }

    @Override
    public String description() {
        return "Contestant management command";
    }

    @Override
    protected Function<LiteralArgumentBuilder<CommandSourceStack>, LiteralArgumentBuilder<CommandSourceStack>> logic() {
        return literal -> literal
                .requires(sender -> sender.getSender().isOp())
                .then(Commands.literal("join")
                        .then(Commands.argument("player", ArgumentTypes.player())
                                .suggests((ctx, builder) -> {
                                    Bukkit.getOnlinePlayers().stream()
                                            .filter(player -> !SportsDay.isContestant(player))
                                            .map(Player::getName)
                                            .filter(name -> name.toLowerCase().startsWith(builder.getRemainingLowerCase()))
                                            .forEach(builder::suggest);
                                    return builder.buildFuture();
                                })
                                .then(Commands.argument("number", IntegerArgumentType.integer(0))
                                        .executes(ctx -> {
                                            Player player = ctx.getArgument("player", PlayerSelectorArgumentResolver.class).resolve(ctx.getSource()).getFirst();
                                            if (SportsDay.isContestant(player)) {
                                                ctx.getSource().getSender().sendMessage(Component.translatable("command.contestant.register.failed")
                                                        .arguments(player.displayName()).color(NamedTextColor.RED));
                                                return Command.SINGLE_SUCCESS;
                                            }
                                            int number = ctx.getArgument("number", int.class);
                                            ctx.getSource().getSender().sendMessage(SportsDay.join(player, number)
                                                    ? Component.translatable("command.contestant.register.success")
                                                    .arguments(player.displayName(), Component.text(number)).color(NamedTextColor.GREEN)
                                                    : Component.translatable("contestant.register.failed.number_occupied")
                                                    .arguments(Component.text(number)).color(NamedTextColor.RED));
                                            return Command.SINGLE_SUCCESS;
                                        })
                                )
                                .executes(ctx -> {
                                    Player player = ctx.getArgument("player", PlayerSelectorArgumentResolver.class).resolve(ctx.getSource()).getFirst();
                                    if (SportsDay.isContestant(player)) {
                                        ctx.getSource().getSender().sendMessage(Component.translatable("command.contestant.register.failed")
                                                .arguments(player.displayName()).color(NamedTextColor.RED));
                                        return Command.SINGLE_SUCCESS;
                                    }
                                    int number = SportsDay.genNumber();
                                    ctx.getSource().getSender().sendMessage(SportsDay.join(player, number)
                                            ? Component.translatable("command.contestant.register.success")
                                            .arguments(player.displayName(), Component.text(number)).color(NamedTextColor.GREEN)
                                            : Component.translatable("contestant.register.failed.number_occupied")
                                            .arguments(Component.text(number)).color(NamedTextColor.RED));
                                    return Command.SINGLE_SUCCESS;
                                })
                        )
                )
                .then(Commands.literal("leave")
                        .then(Commands.argument("player", new ContestantArgument())
                                .executes(ctx -> {
                                    Player player = ctx.getArgument("player", Player.class);
                                    ctx.getSource().getSender().sendMessage(SportsDay.leave(player)
                                            ? Component.translatable("command.contestant.unregister.success")
                                            .arguments(Component.text(player.getName())).color(NamedTextColor.GREEN)
                                            : Component.translatable("command.contestant.unregister.failed")
                                            .arguments(Component.text(player.getName())).color(NamedTextColor.RED));
                                    return Command.SINGLE_SUCCESS;
                                })
                        )
                        .executes(ctx -> {
                            if (ctx.getSource().getSender() instanceof Player player) {
                                if (!SportsDay.leave(player))
                                    player.sendMessage(Component.translatable("contestant.unregister.failed")
                                            .color(NamedTextColor.RED));
                                return Command.SINGLE_SUCCESS;
                            }
                            ctx.getSource().getSender().sendMessage(Component.text("/contestant leave <player>"));
                            return Command.SINGLE_SUCCESS;
                        })
                )
                .then(Commands.literal("score")
                        .then(Commands.argument("player", new ContestantArgument())
                                .then(Commands.argument("score", IntegerArgumentType.integer(0))
                                        .executes(ctx -> {
                                            Player player = ctx.getArgument("player", Player.class);
                                            if (!SportsDay.isContestant(player)) {
                                                ctx.getSource().getSender().sendMessage(Component.translatable("command.contestant.unregister.failed")
                                                        .arguments(Component.text(player.getName())));
                                                return Command.SINGLE_SUCCESS;
                                            }
                                            int score = ctx.getArgument("score", int.class);
                                            ContestantData data = SportsDay.getContestant(player.getUniqueId());
                                            data.setScore(score);
                                            ctx.getSource().getSender().sendMessage(Component.translatable("command.contestant.score.set.success")
                                                    .arguments(Component.text(player.getName()), Component.text(data.getScore()))
                                                    .color(NamedTextColor.GREEN));
                                            return Command.SINGLE_SUCCESS;
                                        })
                                )
                                .executes(ctx -> {
                                    Player player = ctx.getArgument("player", Player.class);
                                    if (!SportsDay.isContestant(player)) {
                                        ctx.getSource().getSender().sendMessage(Component.translatable("command.contestant.unregister.failed")
                                                .arguments(Component.text(player.getName())));
                                        return Command.SINGLE_SUCCESS;
                                    }
                                    ContestantData data = SportsDay.getContestant(player.getUniqueId());
                                    ctx.getSource().getSender().sendMessage(Component.translatable("command.contestant.score.query")
                                            .arguments(Component.text(player.getName()), Component.text(data.getScore()))
                                            .color(NamedTextColor.GREEN));
                                    return Command.SINGLE_SUCCESS;
                                })
                        )
                )
                .then(Commands.literal("list")
                        .executes(ctx -> {
                            TextComponent.Builder builder = Component.text();
                            SportsDay.getContestants().forEach(data -> builder.appendNewline()
                                    .append(Component.text(data.getName() + " #" + data.getNumber())
                                            .color(data.isOnline() ? NamedTextColor.YELLOW : NamedTextColor.GRAY)));
                            ctx.getSource().getSender().sendMessage(Component.translatable("command.contestant.list")
                                    .color(NamedTextColor.YELLOW)
                                    .arguments(builder));
                            return Command.SINGLE_SUCCESS;
                        })
                );
    }
}
