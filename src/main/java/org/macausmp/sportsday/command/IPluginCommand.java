package org.macausmp.sportsday.command;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a plugin command
 */
public abstract class IPluginCommand implements CommandExecutor, TabCompleter {
    /**
     * Execute command
     * @param sender command sender
     * @param args arguments
     */
    abstract void onCommand(CommandSender sender, String[] args);

    /**
     * Command name
     * @return name of command
     */
    abstract String name();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        onCommand(sender, args);
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return new ArrayList<>();
    }

    protected void requirePlayer(@NotNull CommandSender sender, @NotNull Function function) {
        if (sender instanceof Player p)
            function.apply(p);
        else
            sender.sendMessage(Component.translatable("permissions.requires.player").color(NamedTextColor.RED));
    }

    protected interface Function {
        void apply(Player p);
    }
}
