package org.macausmp.sportsday.gui.competition;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.competition.Competitions;
import org.macausmp.sportsday.competition.Stage;
import org.macausmp.sportsday.gui.AbstractGUI;
import org.macausmp.sportsday.gui.GUIButton;
import org.macausmp.sportsday.util.TextUtil;

import java.util.ArrayList;
import java.util.List;

public class CompetitionInfoGUI extends AbstractGUI {
    public CompetitionInfoGUI() {
        super(36, Component.translatable("gui.info.title"));
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
    public void onClick(InventoryClickEvent e, Player p, ItemStack item) {
    }

    private @NotNull ItemStack status() {
        ItemStack status = new ItemStack(Material.BEACON);
        status.editMeta(meta -> {
            boolean b = Competitions.getCurrentlyCompetition() != null;
            meta.displayName(TextUtil.convert(Component.translatable("competition.current").color(NamedTextColor.GREEN).args(b ? Competitions.getCurrentlyCompetition().getName() : Component.translatable("gui.none"))));
            List<Component> lore = new ArrayList<>();
            lore.add(TextUtil.convert(Component.translatable("competition.stage").color(NamedTextColor.GREEN).args(b ? Competitions.getCurrentlyCompetition().getStage().getName() : Stage.IDLE.getName())));
            meta.lore(lore);
        });
        return status;
    }

    private @NotNull ItemStack player() {
        ItemStack stack = new ItemStack(Material.PAPER);
        stack.editMeta(meta -> {
            meta.displayName(TextUtil.convert(Component.translatable("competition.players").color(NamedTextColor.GREEN).args(Component.text(Competitions.getPlayerData().size()).color(NamedTextColor.YELLOW))));
            List<Component> lore = new ArrayList<>();
            lore.add(TextUtil.convert(Component.translatable("competition.online_players").color(NamedTextColor.GREEN).args(Component.text(Competitions.getOnlinePlayers().size()).color(NamedTextColor.YELLOW))));
            meta.lore(lore);
        });
        return stack;
    }
}
