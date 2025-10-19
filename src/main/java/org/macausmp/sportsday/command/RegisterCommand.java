package org.macausmp.sportsday.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.macausmp.sportsday.SportsDay;

import java.util.function.Function;

public class RegisterCommand extends PluginCommand {
    @Override
    public String name() {
        return "register";
    }

    @Override
    public String description() {
        return "Register as a contestant";
    }

    @Override
    protected Function<LiteralArgumentBuilder<CommandSourceStack>, LiteralArgumentBuilder<CommandSourceStack>> logic() {
        return literal -> literal
                .requires(sender -> sender.getSender() instanceof Player)
                .then(Commands.argument("number", IntegerArgumentType.integer(0))
                        .executes(ctx -> {
                            Player player = (Player) ctx.getSource().getSender();
                            if (SportsDay.isContestant(player)) {
                                player.sendMessage(Component.translatable("contestant.register.failed")
                                        .arguments(player.displayName()).color(NamedTextColor.RED));
                                return Command.SINGLE_SUCCESS;
                            }
                            int number = ctx.getArgument("number", int.class);
                            if (!SportsDay.join(player, number))
                                player.sendMessage(Component.translatable("contestant.register.failed.number_occupied")
                                        .arguments(Component.text(number)).color(NamedTextColor.RED));
                            return Command.SINGLE_SUCCESS;
                        })
                )
                .executes(ctx -> {
                    Player player = (Player) ctx.getSource().getSender();
                    if (SportsDay.isContestant(player)) {
                        player.sendMessage(Component.translatable("contestant.register.failed")
                                .arguments(player.displayName()).color(NamedTextColor.RED));
                        return Command.SINGLE_SUCCESS;
                    }
                    int number = SportsDay.genNumber();
                    if (!SportsDay.join(player, number))
                        player.sendMessage(Component.translatable("contestant.register.failed.number_occupied")
                                .arguments(Component.text(number)).color(NamedTextColor.RED));
                    return Command.SINGLE_SUCCESS;
                });
    }
}
