package org.macausmp.sportsday.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public interface IPluginGUI {
    /**
     * Gets GUI {@link Inventory} content
     * @return GUI {@link Inventory} content
     */
    Inventory getInventory();

    /**
     * Open this GUI to player
     * @param player GUI open to
     */
    void openTo(@NotNull Player player);

    /**
     * Update GUI content
     */
    void update();

    /**
     * Listener call from {@link GUIListener}
     * @param event event given from {@link GUIListener}
     */
    void onClick(InventoryClickEvent event, Player player, ItemStack item);
}
