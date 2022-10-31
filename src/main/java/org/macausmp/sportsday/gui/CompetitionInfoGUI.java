package org.macausmp.sportsday.gui;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.competition.Competitions;

import java.util.ArrayList;
import java.util.List;

public class CompetitionInfoGUI extends AbstractGUI {
    private static final ItemStack SELECTED = GUIButton.addEffect(GUIButton.competitionInfo());

    public CompetitionInfoGUI() {
        super(27, Component.text("比賽資訊"));
        for (int i = 0; i < 9; i++) {
            getInventory().setItem(i + 9, GUIButton.BOARD);
        }
        getInventory().setItem(0, SELECTED);
        getInventory().setItem(1, GUIButton.PLAYER_LIST);
        getInventory().setItem(2, GUIButton.START_COMPETITION);
        getInventory().setItem(3, GUIButton.END_COMPETITION);
        getInventory().setItem(4, GUIButton.COMPETITION_SETTINGS);
    }

    @Override
    public void update() {
        if (Competitions.getCurrentlyCompetition() == null) return;
        getInventory().setItem(18, status());
    }

    private @NotNull ItemStack status() {
        ItemStack status = new ItemStack(Material.BEACON);
        status.editMeta(meta -> {
            meta.displayName(Component.text("當前比賽: ").decoration(TextDecoration.ITALIC, false).color(NamedTextColor.YELLOW).append(Competitions.getCurrentlyCompetition().getName()));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.text("比賽階段: " + Competitions.getCurrentlyCompetition().getStage()).decoration(TextDecoration.ITALIC, false).color(NamedTextColor.YELLOW));
            lore.add(Component.text("參賽人數: " + Competitions.getPlayerDataList().size()).decoration(TextDecoration.ITALIC, false).color(NamedTextColor.YELLOW));
            meta.lore(lore);
        });
        return status;
    }
}
