package org.macausmp.sportsday.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.SportsDay;
import org.macausmp.sportsday.competition.Competitions;
import org.macausmp.sportsday.util.Translation;

import java.util.Objects;

public class CompetitionStartGUI extends AbstractGUI {
    public CompetitionStartGUI() {
        super(54, Translation.translatable("gui.title.start"));
        for (int i = 0; i < 9; i++) {
            getInventory().setItem(i + 9, GUIButton.BOARD);
        }
        getInventory().setItem(0, GUIButton.COMPETITION_INFO);
        getInventory().setItem(1, GUIButton.PLAYER_LIST);
        getInventory().setItem(2, GUIButton.START_COMPETITION_SELECTED);
        getInventory().setItem(3, GUIButton.END_COMPETITION);
        getInventory().setItem(4, GUIButton.COMPETITION_SETTINGS);
        getInventory().setItem(5, GUIButton.VERSION);
    }

    @Override
    public void update() {
        getInventory().setItem(18, GUIButton.ELYTRA_RACING);
        getInventory().setItem(19, GUIButton.ICE_BOAT_RACING);
        getInventory().setItem(20, GUIButton.JAVELIN_THROW);
        getInventory().setItem(21, GUIButton.OBSTACLE_COURSE);
        getInventory().setItem(22, GUIButton.PARKOUR);
        getInventory().setItem(23, GUIButton.SUMO);
    }

    @Override
    public void onClick(@NotNull InventoryClickEvent event, Player p, @NotNull ItemStack item) {
        PersistentDataContainer container = item.getItemMeta().getPersistentDataContainer();
        if (container.has(SportsDay.ITEM_ID, PersistentDataType.STRING) && Objects.equals(container.get(SportsDay.ITEM_ID, PersistentDataType.STRING), "competition")) {
            Competitions.start(p, container.get(SportsDay.COMPETITION_ID, PersistentDataType.STRING));
        }
    }
}
