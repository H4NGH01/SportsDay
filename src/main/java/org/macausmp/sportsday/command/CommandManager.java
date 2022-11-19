package org.macausmp.sportsday.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.SportsDay;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CommandManager implements CommandExecutor {
    private static final List<IPluginCommand> COMMANDS = new ArrayList<>();
    private static final IPluginCommand COMPETITION = new CompetitionCommand();
    private static final IPluginCommand COMPETITIONGUI = new CompetitionGUICommand();

    public void registry() {
        COMMANDS.add(COMPETITION);
        COMMANDS.add(COMPETITIONGUI);
        for (IPluginCommand command : COMMANDS) {
            Objects.requireNonNull(SportsDay.getInstance().getCommand(command.name())).setExecutor(this);
            Objects.requireNonNull(SportsDay.getInstance().getCommand(command.name())).setTabCompleter(command);
        }
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        for (IPluginCommand pc : COMMANDS) {
            if (pc.name().equals(command.getName())) {
                pc.onCommand(sender, args);
                return true;
            }
        }
        return false;
    }
}
