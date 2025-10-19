package org.macausmp.sportsday.command;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.MessageComponentSerializer;
import io.papermc.paper.command.brigadier.argument.CustomArgumentType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.NamespacedKey;
import org.jspecify.annotations.NullMarked;
import org.macausmp.sportsday.SportsDay;
import org.macausmp.sportsday.SportsRegistry;
import org.macausmp.sportsday.sport.Sport;

import java.util.Locale;
import java.util.concurrent.CompletableFuture;

@NullMarked
public final class SportArgument implements CustomArgumentType<Sport, String> {
    private static final SportsDay PLUGIN = SportsDay.getInstance();

    private static final DynamicCommandExceptionType ERROR_INVALID_SPORT = new DynamicCommandExceptionType(
            sport -> MessageComponentSerializer.message()
                    .serialize(Component.translatable("argument.sport.invalid")
                            .arguments(Component.text(sport.toString()))
                            .color(NamedTextColor.RED))
    );

    @Override
    public Sport parse(StringReader reader) throws CommandSyntaxException {
        String parsed = getNativeType().parse(reader);
        final Sport sport = SportsRegistry.SPORT.get(new NamespacedKey(PLUGIN, parsed));
        if (sport == null) {
            throw ERROR_INVALID_SPORT.create(parsed);
        }
        return sport;
    }

    @Override
    public ArgumentType<String> getNativeType() {
        return StringArgumentType.word();
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        SportsRegistry.SPORT.stream()
                .map(sport -> sport.getKey().getKey())
                .filter(name -> name.toLowerCase(Locale.ROOT).startsWith(builder.getRemainingLowerCase()))
                .forEach(builder::suggest);
        return builder.buildFuture();
    }
}
