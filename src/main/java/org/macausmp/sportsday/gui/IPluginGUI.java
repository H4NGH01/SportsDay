package org.macausmp.sportsday.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

/**
 * Represents a plugin gui
 */
public interface IPluginGUI extends InventoryHolder {
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
