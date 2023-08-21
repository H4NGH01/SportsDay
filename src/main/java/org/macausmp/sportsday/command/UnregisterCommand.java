package org.macausmp.sportsday.command;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.macausmp.sportsday.competition.Competitions;

import java.util.ArrayList;
import java.util.List;

public class UnregisterCommand implements IPluginCommand {
    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if (sender instanceof Player p) {
            if (!Competitions.leave(p)) sender.sendMessage(Component.translatable("player.unregistered_message").color(NamedTextColor.RED));
        } else {
            sender.sendMessage(Component.translatable("permissions.requires.player").color(NamedTextColor.RED));
        }
    }

    @Override
    public String name() {
        return "unregister";
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return new ArrayList<>();
    }
}
