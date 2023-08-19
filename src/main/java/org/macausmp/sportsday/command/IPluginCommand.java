package org.macausmp.sportsday.command;

import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

/**
 * Represents a plugin command
 */
public interface IPluginCommand extends TabCompleter {
    /**
     * Execute command
     * @param sender command sender
     * @param args arguments
     */
    void onCommand(CommandSender sender, String[] args);

    /**
     * Command name
     * @return name of command
     */
    String name();
}
