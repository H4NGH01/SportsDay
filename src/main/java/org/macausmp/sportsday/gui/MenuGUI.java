package org.macausmp.sportsday.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.util.Translation;

public class MenuGUI extends AbstractGUI {
    public MenuGUI() {
        super(45, Translation.translatable("gui.title.menu"));
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
    public void onClick(@NotNull InventoryClickEvent e, Player p, ItemStack item) {
    }
}
