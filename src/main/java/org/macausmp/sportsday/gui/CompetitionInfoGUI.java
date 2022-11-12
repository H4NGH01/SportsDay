package org.macausmp.sportsday.gui;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.PlayerData;
import org.macausmp.sportsday.competition.Competitions;
import org.macausmp.sportsday.competition.ICompetition;

import java.util.ArrayList;
import java.util.List;

public class CompetitionInfoGUI extends AbstractGUI {
    private static final ItemStack SELECTED = GUIButton.addEffect(GUIButton.competitionInfo());

    public CompetitionInfoGUI() {
        super(36, Component.text("比賽資訊"));
        for (int i = 0; i < 9; i++) {
            getInventory().setItem(i + 9, GUIButton.BOARD);
        }
        getInventory().setItem(0, SELECTED);
        getInventory().setItem(1, GUIButton.PLAYER_LIST);
        getInventory().setItem(2, GUIButton.START_COMPETITION);
        getInventory().setItem(3, GUIButton.END_COMPETITION);
        getInventory().setItem(4, GUIButton.COMPETITION_SETTINGS);
        getInventory().setItem(5, GUIButton.VERSION);
    }

    @Override
    public void update() {
        getInventory().setItem(18, status());
        getInventory().setItem(27, player());
    }

    @Override
    public void onClick(InventoryClickEvent event) {

    }

    private @NotNull ItemStack status() {
        ItemStack status = new ItemStack(Material.BEACON);
        status.editMeta(meta -> {
            boolean b = Competitions.getCurrentlyCompetition() != null;
            meta.displayName(Component.translatable("當前比賽: %s").color(NamedTextColor.GREEN).args(b ? Competitions.getCurrentlyCompetition().getName() : Component.text("無").color(NamedTextColor.GRAY)).decoration(TextDecoration.ITALIC, false));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.translatable("比賽階段: %s").color(NamedTextColor.GREEN).args(b ? Competitions.getCurrentlyCompetition().getStage().getName() : ICompetition.Stage.IDLE.getName()).decoration(TextDecoration.ITALIC, false));
            meta.lore(lore);
        });
        return status;
    }

    private @NotNull ItemStack player() {
        ItemStack stack = new ItemStack(Material.PAPER);
        stack.editMeta(meta -> {
            meta.displayName(Component.translatable("參賽人數: %s").color(NamedTextColor.GREEN).args(Component.text(Competitions.getPlayerDataList().size()).color(NamedTextColor.YELLOW)).decoration(TextDecoration.ITALIC, false));
            int i = 0;
            for (PlayerData d : Competitions.getPlayerDataList()) {
                if (d.isPlayerOnline()) i++;
            }
            List<Component> lore = new ArrayList<>();
            lore.add(Component.translatable("線上參賽人數: %s").color(NamedTextColor.GREEN).args(Component.text(i).color(NamedTextColor.YELLOW)).decoration(TextDecoration.ITALIC, false));
            meta.lore(lore);
        });
        return stack;
    }
}
