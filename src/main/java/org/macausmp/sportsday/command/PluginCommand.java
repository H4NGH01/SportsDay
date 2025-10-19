package org.macausmp.sportsday.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;

import java.util.Collection;
import java.util.Collections;
import java.util.function.Function;

/**
 * Represents a plugin command.
 */
public abstract class PluginCommand {
    /**
     * Command name.
     * @return name of command
     */
    public abstract String name();

    /**
     * Command description.
     * @return description of command
     */
    public abstract String description();

    /**
     * Command aliases.
     * @return aliases of command
     */
    public Collection<String> aliases() {
        return Collections.emptyList();
    }

    protected abstract Function<LiteralArgumentBuilder<CommandSourceStack>, LiteralArgumentBuilder<CommandSourceStack>> logic();

    public LiteralCommandNode<CommandSourceStack> create() {
        return logic().apply(Commands.literal(name())).build();
    }
}
