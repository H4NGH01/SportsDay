package org.macausmp.sportsday.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.entity.Player;
import org.macausmp.sportsday.gui.menu.MenuGUI;

import java.util.function.Function;

public class MenuCommand extends PluginCommand {
    @Override
    public String name() {
        return "menu";
    }

    @Override
    public String description() {
        return "Open menu gui";
    }

    @Override
    protected Function<LiteralArgumentBuilder<CommandSourceStack>, LiteralArgumentBuilder<CommandSourceStack>> logic() {
        return literal -> literal
                .requires(sender -> sender.getSender() instanceof Player)
                .executes(ctx -> {
                    ((Player) ctx.getSource().getSender()).openInventory(new MenuGUI().getInventory());
                    return Command.SINGLE_SUCCESS;
                });
    }
}
