package org.macausmp.sportsday.gui;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.competition.Competitions;
import org.macausmp.sportsday.competition.Stage;
import org.macausmp.sportsday.util.Translation;

import java.util.ArrayList;
import java.util.List;

public class CompetitionInfoGUI extends AbstractGUI {
    public CompetitionInfoGUI() {
        super(36, Translation.translatable("gui.title.info"));
        for (int i = 0; i < 9; i++) {
            getInventory().setItem(i + 9, GUIButton.BOARD);
        }
        getInventory().setItem(0, GUIButton.COMPETITION_INFO_SELECTED);
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
    public void onClick(InventoryClickEvent event, Player player, ItemStack item) {

    }

    private @NotNull ItemStack status() {
        ItemStack status = new ItemStack(Material.BEACON);
        status.editMeta(meta -> {
            boolean b = Competitions.getCurrentlyCompetition() != null;
            meta.displayName(Translation.translatable("competition.current").color(NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false).args(b ? Competitions.getCurrentlyCompetition().getName() : Translation.translatable("gui.none")));
            List<Component> lore = new ArrayList<>();
            lore.add(Translation.translatable("competition.stage").color(NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false).args(b ? Competitions.getCurrentlyCompetition().getStage().getName() : Stage.IDLE.getName()));
            meta.lore(lore);
        });
        return status;
    }

    private @NotNull ItemStack player() {
        ItemStack stack = new ItemStack(Material.PAPER);
        stack.editMeta(meta -> {
            meta.displayName(Translation.translatable("competition.players").color(NamedTextColor.GREEN).args(Component.text(Competitions.getPlayerData().size()).color(NamedTextColor.YELLOW)).decoration(TextDecoration.ITALIC, false));
            List<Component> lore = new ArrayList<>();
            lore.add(Translation.translatable("competition.online_players").color(NamedTextColor.GREEN).args(Component.text(Competitions.getOnlinePlayers().size()).color(NamedTextColor.YELLOW)).decoration(TextDecoration.ITALIC, false));
            meta.lore(lore);
        });
        return stack;
    }
}
