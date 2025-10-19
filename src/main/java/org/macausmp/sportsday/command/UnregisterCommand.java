package org.macausmp.sportsday.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.macausmp.sportsday.SportsDay;

import java.util.function.Function;

public class UnregisterCommand extends PluginCommand {
    @Override
    public String name() {
        return "unregister";
    }

    @Override
    public String description() {
        return "Unregister as a contestant";
    }

    @Override
    protected Function<LiteralArgumentBuilder<CommandSourceStack>, LiteralArgumentBuilder<CommandSourceStack>> logic() {
        return literal -> literal
                .requires(sender -> sender.getSender() instanceof Player)
                .then(Commands.argument("confirm", StringArgumentType.word())
                        .executes(ctx -> {
                            if (ctx.getArgument("confirm", String.class).equals("confirm")) {
                                Player player = (Player) ctx.getSource().getSender();
                                if (!SportsDay.leave(player))
                                    player.sendMessage(Component.translatable("contestant.unregister.failed")
                                            .color(NamedTextColor.RED));
                            }
                            return Command.SINGLE_SUCCESS;
                        })
                )
                .executes(ctx -> {
                    ctx.getSource().getSender().sendMessage(Component.translatable("command.unregister.confirmation")
                            .color(NamedTextColor.RED));
                    return Command.SINGLE_SUCCESS;
                });
    }
}
