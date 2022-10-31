package org.macausmp.sportsday.command;

import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

public abstract class PluginCommand implements TabCompleter {
    public abstract void onCommand(CommandSender sender, String[] args);

    public abstract String name();

    public abstract String info();
}
