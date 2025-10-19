package org.macausmp.sportsday.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.entity.Player;
import org.macausmp.sportsday.gui.customize.CustomizeMenuGUI;

import java.util.function.Function;

public class CustomizeCommand extends PluginCommand {
    @Override
    public String name() {
        return "customize";
    }

    @Override
    public String description() {
        return "Open customize gui";
    }

    @Override
    protected Function<LiteralArgumentBuilder<CommandSourceStack>, LiteralArgumentBuilder<CommandSourceStack>> logic() {
        return literal -> literal
                .requires(sender -> sender.getSender() instanceof Player)
                .executes(ctx -> {
                    Player player = (Player) ctx.getSource().getSender();
                    player.openInventory(new CustomizeMenuGUI(player).getInventory());
                    return Command.SINGLE_SUCCESS;
                });
    }
}
