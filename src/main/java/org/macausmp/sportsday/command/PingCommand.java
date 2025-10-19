package org.macausmp.sportsday.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.function.Function;

public class PingCommand extends PluginCommand {
    @Override
    public String name() {
        return "ping";
    }

    @Override
    public String description() {
        return "Query player's ping";
    }

    @Override
    protected Function<LiteralArgumentBuilder<CommandSourceStack>, LiteralArgumentBuilder<CommandSourceStack>> logic() {
        return literal -> literal
                .then(Commands.argument("player", ArgumentTypes.player())
                        .suggests((ctx, builder) -> {
                            Bukkit.getOnlinePlayers().stream()
                                    .map(Player::getName)
                                    .filter(name -> name.toLowerCase().startsWith(builder.getRemainingLowerCase()))
                                    .forEach(builder::suggest);
                            return builder.buildFuture();
                        })
                        .executes(ctx -> {
                            Player player = ctx.getArgument("player", PlayerSelectorArgumentResolver.class).resolve(ctx.getSource()).getFirst();
                            ctx.getSource().getSender().sendMessage(Component.translatable("%s's ping: %sms").arguments(player.displayName(), Component.text(player.getPing())));
                            return Command.SINGLE_SUCCESS;
                        })
                )
                .requires(sender -> sender.getSender() instanceof Player)
                .executes(ctx -> {
                    Player player = (Player) ctx.getSource().getSender();
                    player.sendMessage("Your ping: %sms".formatted(player.getPing()));
                    return Command.SINGLE_SUCCESS;
                });
    }
}
