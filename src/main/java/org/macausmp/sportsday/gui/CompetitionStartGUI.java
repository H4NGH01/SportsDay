package org.macausmp.sportsday.gui;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.SportsDay;
import org.macausmp.sportsday.competition.Competitions;
import org.macausmp.sportsday.competition.ICompetition;

import java.util.Objects;

public class CompetitionStartGUI extends AbstractGUI {
    private static final ItemStack SELECTED = GUIButton.addEffect(GUIButton.startCompetition());

    public CompetitionStartGUI() {
        super(54, Component.text("開始一場比賽"));
        for (int i = 0; i < 9; i++) {
            getInventory().setItem(i + 9, GUIButton.BOARD);
        }
        getInventory().setItem(0, GUIButton.COMPETITION_INFO);
        getInventory().setItem(1, GUIButton.PLAYER_LIST);
        getInventory().setItem(2, SELECTED);
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
    public void onClick(@NotNull InventoryClickEvent event) {
        Player p = (Player) event.getWhoClicked();
        ItemStack item = Objects.requireNonNull(event.getCurrentItem());
        PersistentDataContainer container = item.getItemMeta().getPersistentDataContainer();
        if (container.has(SportsDay.ITEM_ID, PersistentDataType.STRING) && Objects.equals(container.get(SportsDay.ITEM_ID, PersistentDataType.STRING), "competition")) {
            for (ICompetition competition : Competitions.COMPETITIONS) {
                if (competition.getID().equals(container.get(SportsDay.COMPETITION_ID, PersistentDataType.STRING))) {
                    if (!competition.isEnable()) {
                        p.sendMessage(Component.text("該比賽項目已被禁用").color(NamedTextColor.RED));
                        return;
                    }
                    if (Competitions.getPlayerDataList().size() >= competition.getLeastPlayersRequired()) {
                        p.sendMessage(Component.text("開始新一場比賽中..."));
                        Competitions.setCurrentlyCompetition(competition);
                        competition.setup();
                    } else {
                        p.sendMessage(Component.text("參賽選手人數不足，無法開始比賽，最少需要" + competition.getLeastPlayersRequired() + "人開始比賽").color(NamedTextColor.RED));
                    }
                    return;
                }
            }
        }
    }
}
