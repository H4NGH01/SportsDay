package org.macausmp.sportsday.gui;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

public interface IPluginGUI {
    Inventory getInventory();

    void openTo(@NotNull Player player);

    void update();
}
