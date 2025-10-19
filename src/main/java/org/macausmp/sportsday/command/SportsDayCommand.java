package org.macausmp.sportsday.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.entity.Player;
import org.macausmp.sportsday.gui.admin.AdminMenuGUI;
import org.macausmp.sportsday.util.ItemUtil;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;

public class SportsDayCommand extends PluginCommand {
    @Override
    public String name() {
        return "sportsday";
    }

    @Override
    public String description() {
        return "Open sportsday gui";
    }

    @Override
    public Collection<String> aliases() {
        return List.of("sd");
    }

    @Override
    protected Function<LiteralArgumentBuilder<CommandSourceStack>, LiteralArgumentBuilder<CommandSourceStack>> logic() {
        return literal -> literal
                .requires(sender -> sender.getSender().isOp() && sender.getSender() instanceof Player)
                .then(Commands.literal("book")
                        .executes(ctx -> {
                            ((Player) ctx.getSource().getSender()).getInventory().addItem(ItemUtil.OP_BOOK);
                            return Command.SINGLE_SUCCESS;
                        })
                )
                .executes(ctx -> {
                    ((Player) ctx.getSource().getSender()).openInventory(new AdminMenuGUI().getInventory());
                    return Command.SINGLE_SUCCESS;
                });
    }
}
