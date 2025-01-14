package org.macausmp.sportsday.command;

import org.macausmp.sportsday.SportsDay;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public final class CommandManager {
    private static final SportsDay PLUGIN = SportsDay.getInstance();
    private static final Set<PluginCommand> COMMANDS = new HashSet<>();
    private static final PluginCommand EVENT = new SportingEventCommand();
    private static final PluginCommand ADMIN_MENU = new SGBCommand();
    private static final PluginCommand CONTESTANT = new ContestantCommand();
    private static final PluginCommand REGISTER = new RegisterCommand();
    private static final PluginCommand UNREGISTER = new UnregisterCommand();
    private static final PluginCommand MENU = new MenuCommand();
    private static final PluginCommand CUSTOMIZE = new CustomizeCommand();
    private static final PluginCommand PING = new PingCommand();

    public void register() {
        COMMANDS.add(EVENT);
        COMMANDS.add(ADMIN_MENU);
        COMMANDS.add(CONTESTANT);
        COMMANDS.add(REGISTER);
        COMMANDS.add(UNREGISTER);
        COMMANDS.add(MENU);
        COMMANDS.add(CUSTOMIZE);
        COMMANDS.add(PING);
        for (PluginCommand command : COMMANDS) {
            Objects.requireNonNull(PLUGIN.getCommand(command.name())).setExecutor(command);
            Objects.requireNonNull(PLUGIN.getCommand(command.name())).setTabCompleter(command);
        }
    }
}
