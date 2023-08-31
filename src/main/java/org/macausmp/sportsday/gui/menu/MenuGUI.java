package org.macausmp.sportsday.gui.menu;

import net.kyori.adventure.inventory.Book;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.competition.AbstractTrackEvent;
import org.macausmp.sportsday.competition.CompetitionListener;
import org.macausmp.sportsday.competition.Competitions;
import org.macausmp.sportsday.competition.IEvent;
import org.macausmp.sportsday.gui.AbstractGUI;
import org.macausmp.sportsday.gui.GUIButton;
import org.macausmp.sportsday.util.ItemUtil;
import org.macausmp.sportsday.util.PlayerCustomize;

import java.util.Objects;

public class MenuGUI extends AbstractGUI {
    private static final Book GUIDE_BOOK =  guidebook();

    public MenuGUI() {
        super(9, Component.translatable("gui.menu.title"));
        getInventory().setItem(0, GUIButton.GUIDEBOOK);
        getInventory().setItem(1, GUIButton.HOME);
        getInventory().setItem(2, GUIButton.PRACTICE);
    }

    @Override
    public void update() {
    }

    @Override
    public void onClick(InventoryClickEvent e, @NotNull Player p, ItemStack item) {
        p.playSound(Sound.sound(Key.key("minecraft:ui.button.click"), Sound.Source.MASTER, 1f, 1f));
        if (ItemUtil.isSameItem(item, GUIButton.GUIDEBOOK)) {
            p.openBook(GUIDE_BOOK);
        } else if (ItemUtil.isSameItem(item, GUIButton.HOME)) {
            if (Competitions.getCurrentlyEvent() == null) {
                p.teleport(p.getWorld().getSpawnLocation());
                p.playSound(net.kyori.adventure.sound.Sound.sound(Key.key("minecraft:entity.bat.takeoff"), net.kyori.adventure.sound.Sound.Source.MASTER, 1f, 1f), net.kyori.adventure.sound.Sound.Emitter.self());
                p.getInventory().clear();
                PlayerCustomize.suitUp(p);
                p.getInventory().setItem(3, ItemUtil.MENU);
                p.getInventory().setItem(4, ItemUtil.CUSTOMIZE);
            }
        } else if (ItemUtil.isSameItem(item, GUIButton.PRACTICE)) {
            if (Competitions.getCurrentlyEvent() == null) {
                p.openInventory(new PracticeGUI().getInventory());
            }
        }
    }

    private static @NotNull Book guidebook() {
        Book.Builder builder = Book.builder();
        builder.addPage(Component.translatable("book.guidebook.page_main"));
        Component b1 = Component.translatable(Objects.requireNonNull(CompetitionListener.CHECKPOINT).translationKey()).color(NamedTextColor.GREEN);
        Component b2 = Component.translatable(Objects.requireNonNull(CompetitionListener.DEATH).translationKey()).color(NamedTextColor.RED);
        Component b3 = Component.translatable(Objects.requireNonNull(AbstractTrackEvent.FINISH_LINE).translationKey()).color(NamedTextColor.GOLD);
        builder.addPage(Component.translatable("book.guidebook.page_block").args(b1, b2, b3));
        for (IEvent event : Competitions.COMPETITIONS) {
            Component rule = Component.translatable("event.rule." + event.getID());
            builder.addPage(Component.translatable("book.guidebook.page_event").args(event.getName().color(NamedTextColor.BLACK), rule));
        }
        return builder.build();
    }
}
