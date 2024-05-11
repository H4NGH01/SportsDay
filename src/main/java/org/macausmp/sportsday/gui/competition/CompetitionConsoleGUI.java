package org.macausmp.sportsday.gui.competition;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.competition.Competitions;
import org.macausmp.sportsday.competition.Status;
import org.macausmp.sportsday.gui.ButtonHandler;
import org.macausmp.sportsday.gui.ConfirmGUI;
import org.macausmp.sportsday.gui.GUIButton;
import org.macausmp.sportsday.util.ItemUtil;

import java.util.HashSet;
import java.util.Set;

public class CompetitionConsoleGUI extends AbstractCompetitionGUI {
    private static final Set<CompetitionConsoleGUI> HANDLER = new HashSet<>();

    public CompetitionConsoleGUI() {
        super(36, Component.translatable("gui.console.title"));
        for (int i = 0; i < 9; i++)
            getInventory().setItem(i + 9, GUIButton.BOARD);
        getInventory().setItem(0, ItemUtil.addWrapper(GUIButton.COMPETITION_CONSOLE));
        getInventory().setItem(1, GUIButton.CONTESTANTS_LIST);
        getInventory().setItem(2, GUIButton.COMPETITION_SETTINGS);
        getInventory().setItem(3, GUIButton.VERSION);
        getInventory().setItem(18, GUIButton.START_COMPETITION);
        getInventory().setItem(19, GUIButton.END_COMPETITION);
        update();
    }

    @Override
    public void update() {
        getInventory().setItem(27, status());
        getInventory().setItem(28, player());
        HANDLER.add(this);
    }

    public static void updateGUI() {
        HANDLER.forEach(CompetitionConsoleGUI::update);
    }

    @ButtonHandler("start_competitions")
    public void start(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        if (Competitions.getCurrentEvent() != null && Competitions.getCurrentEvent().getStatus() != Status.ENDED) {
            p.sendMessage(Component.translatable("command.competition.start.failed").color(NamedTextColor.RED));
            p.playSound(Sound.sound(Key.key("minecraft:entity.enderman.teleport"), Sound.Source.MASTER, 1f, 1f));
            return;
        }
        p.playSound(Sound.sound(Key.key("minecraft:ui.button.click"), Sound.Source.MASTER, 1f, 1f));
        p.openInventory(new CompetitionStartGUI().getInventory());
    }

    @ButtonHandler("end_competition")
    public void end(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        if (Competitions.getCurrentEvent() == null || Competitions.getCurrentEvent().getStatus() == Status.ENDED) {
            p.sendMessage(Component.translatable("command.competition.end.failed").color(NamedTextColor.RED));
            p.playSound(Sound.sound(Key.key("minecraft:entity.enderman.teleport"), Sound.Source.MASTER, 1f, 1f));
            return;
        }
        p.openInventory(new ConfirmGUI(this, player -> {
            boolean b = Competitions.forceEnd(p);
            p.playSound(Sound.sound(Key.key(b ?
                    "minecraft:entity.enderman.teleport" : "minecraft:ui.button.click"), Sound.Source.MASTER, 1f, 1f));
            return b;
        }).getInventory());
    }

    private @NotNull ItemStack status() {
        boolean b = Competitions.getCurrentEvent() != null;
        Component display = Component.translatable("competition.current").color(NamedTextColor.GREEN)
                .args(b ? Competitions.getCurrentEvent().getName() : Component.translatable("gui.text.none"));
        Component lore = Component.translatable("competition.status").color(NamedTextColor.GREEN)
                .args(b ? Competitions.getCurrentEvent().getStatus().getName() : Status.IDLE.getName());
        return ItemUtil.item(Material.BEACON, null, display, lore);
    }

    private @NotNull ItemStack player() {
        Component display = Component.translatable("competition.contestants.total").color(NamedTextColor.GREEN)
                .args(Component.text(Competitions.getContestants().size()).color(NamedTextColor.YELLOW));
        Component lore = Component.translatable("competition.contestants.online").color(NamedTextColor.GREEN)
                .args(Component.text(Competitions.getOnlineContestants().size()).color(NamedTextColor.YELLOW));
        return ItemUtil.item(Material.PAPER, null, display, lore);
    }

    @Override
    public void onClose() {
        HANDLER.remove(this);
    }
}
