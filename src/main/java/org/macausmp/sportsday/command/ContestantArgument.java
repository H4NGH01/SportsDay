package org.macausmp.sportsday.command;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.MessageComponentSerializer;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.CustomArgumentType;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;
import org.macausmp.sportsday.ContestantData;
import org.macausmp.sportsday.SportsDay;

import java.util.Locale;
import java.util.concurrent.CompletableFuture;

@NullMarked
public final class ContestantArgument implements CustomArgumentType<Player, PlayerSelectorArgumentResolver> {
    private static final SimpleCommandExceptionType ERROR_BAD_SOURCE = new SimpleCommandExceptionType(
            MessageComponentSerializer.message().serialize(Component.text("The source needs to be a CommandSourceStack!"))
    );

    private static final DynamicCommandExceptionType ERROR_NOT_CONTESTANT = new DynamicCommandExceptionType(
            name -> MessageComponentSerializer.message().serialize(
                    Component.translatable("argument.contestant.unregistered")
                            .arguments(Component.text(name.toString()))
                            .color(NamedTextColor.RED))
    );

    @Override
    public Player parse(StringReader reader) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <S> Player parse(StringReader reader, S source) throws CommandSyntaxException {
        if (!(source instanceof CommandSourceStack stack)) {
            throw ERROR_BAD_SOURCE.create();
        }
        final Player player = getNativeType().parse(reader).resolve(stack).getFirst();
        if (!SportsDay.isContestant(player)) {
            throw ERROR_NOT_CONTESTANT.create(player.getName());
        }
        return player;
    }

    @Override
    public ArgumentType<PlayerSelectorArgumentResolver> getNativeType() {
        return ArgumentTypes.player();
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        SportsDay.getContestants().stream()
                .map(ContestantData::getName)
                .filter(name -> name.toLowerCase(Locale.ROOT).startsWith(builder.getRemainingLowerCase()))
                .forEach(builder::suggest);
        return builder.buildFuture();
    }
}
