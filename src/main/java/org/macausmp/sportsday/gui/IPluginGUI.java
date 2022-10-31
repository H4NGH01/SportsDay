package org.macausmp.sportsday.gui;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

public interface IPluginGUI {
    Inventory getInventory();

    Component getTitle();

    IPluginGUI getPreviousPage();

    IPluginGUI getNextPage();

    void openTo(@NotNull Player player);

    void update();
}
