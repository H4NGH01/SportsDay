package org.macausmp.sportsday.command;

import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.plugin.Plugin;
import org.jspecify.annotations.NullMarked;
import org.macausmp.sportsday.SportsDay;

@NullMarked
public final class CommandManager {
    private static final SportsDay PLUGIN = SportsDay.getInstance();

    public void register() {
        register(new SportsDayCommand());
        register(new SportingEventCommand());
        register(new ContestantCommand());
        register(new RegisterCommand());
        register(new UnregisterCommand());
        register(new MenuCommand());
        register(new CustomizeCommand());
        register(new PingCommand());
    }

    private void register(PluginCommand command) {
        LifecycleEventManager<Plugin> manager = PLUGIN.getLifecycleManager();
        manager.registerEventHandler(LifecycleEvents.COMMANDS, cmd -> cmd.registrar()
                .register(command.create(),  command.description(), command.aliases()));
    }
}
