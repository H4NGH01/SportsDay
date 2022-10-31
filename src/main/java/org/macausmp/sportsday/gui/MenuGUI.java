package org.macausmp.sportsday.gui;

import net.kyori.adventure.text.Component;

public class MenuGUI extends AbstractGUI {
    public MenuGUI() {
        super(27, Component.text("運動會菜單"));
        for (int i = 0; i < 27; i++) {
            getInventory().setItem(i, GUIButton.BOARD);
        }
        getInventory().setItem(9, GUIButton.COMPETITION_INFO);
        getInventory().setItem(11, GUIButton.PLAYER_LIST);
        getInventory().setItem(13, GUIButton.START_COMPETITION);
        getInventory().setItem(15, GUIButton.END_COMPETITION);
        getInventory().setItem(17, GUIButton.COMPETITION_SETTINGS);
    }

    @Override
    public void update() {
    }
}
