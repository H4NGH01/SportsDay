package org.macausmp.sportsday.gui;

import net.kyori.adventure.text.Component;
import org.bukkit.event.inventory.InventoryClickEvent;

public class MenuGUI extends AbstractGUI {
    public MenuGUI() {
        super(45, Component.text("運動會菜單"));
        for (int i = 0; i < 45; i++) {
            getInventory().setItem(i, GUIButton.BOARD);
        }
        getInventory().setItem(10, GUIButton.COMPETITION_INFO);
        getInventory().setItem(13, GUIButton.PLAYER_LIST);
        getInventory().setItem(16, GUIButton.START_COMPETITION);
        getInventory().setItem(28, GUIButton.END_COMPETITION);
        getInventory().setItem(31, GUIButton.COMPETITION_SETTINGS);
        getInventory().setItem(34, GUIButton.VERSION);
    }

    @Override
    public void update() {
    }

    @Override
    public void onClick(InventoryClickEvent event) {

    }
}
