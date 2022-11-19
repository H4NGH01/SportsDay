package org.macausmp.sportsday.command;

import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

public interface IPluginCommand extends TabCompleter {
    void onCommand(CommandSender sender, String[] args);
    String name();
}
