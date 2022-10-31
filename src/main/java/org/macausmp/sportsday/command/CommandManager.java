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
    private static final List<PluginCommand> COMMANDS = new ArrayList<>();
    private static final PluginCommand COMPETITION = new CompetitionCommand();
    private static final PluginCommand COMPETITIONGUI = new CompetitionGUICommand();

    public void registry() {
        COMMANDS.add(COMPETITION);
        COMMANDS.add(COMPETITIONGUI);
        for (PluginCommand command : COMMANDS) {
            Objects.requireNonNull(SportsDay.getInstance().getCommand(command.name())).setExecutor(this);
            Objects.requireNonNull(SportsDay.getInstance().getCommand(command.name())).setTabCompleter(command);
        }
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        for (PluginCommand pc : COMMANDS) {
            if (pc.name().equals(command.getName())) {
                pc.onCommand(sender, args);
                return true;
            }
        }
        return false;
    }
}
