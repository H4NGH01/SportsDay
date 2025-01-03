package org.macausmp.sportsday.gui.menu;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.competition.Competitions;
import org.macausmp.sportsday.competition.SportingEvent;
import org.macausmp.sportsday.competition.TrackEvent;
import org.macausmp.sportsday.gui.ButtonHandler;
import org.macausmp.sportsday.gui.PluginGUI;
import org.macausmp.sportsday.util.ItemUtil;
import org.macausmp.sportsday.util.KeyDataType;

import java.util.Objects;

public class PracticeGUI extends PluginGUI {
    public PracticeGUI() {
        super(18, Component.translatable("gui.menu.practice.title"));
        for (int i = 0; i < 9; i++)
            getInventory().setItem(i, BOARD);
        getInventory().setItem(8, BACK);
        getInventory().setItem(9, practice(Competitions.ELYTRA_RACING));
        getInventory().setItem(10, practice(Competitions.ICE_BOAT_RACING));
        getInventory().setItem(11, practice(Competitions.JAVELIN_THROW));
        getInventory().setItem(12, practice(Competitions.OBSTACLE_COURSE));
        getInventory().setItem(13, practice(Competitions.PARKOUR));
        getInventory().setItem(14, practice(Competitions.SUMO));
    }

    @ButtonHandler("back")
    public void back(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        p.openInventory(new MenuGUI().getInventory());
        p.playSound(Sound.sound(Key.key("minecraft:ui.button.click"), Sound.Source.MASTER, 1f, 1f));
    }

    @ButtonHandler("event_practice")
    public void practice(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        if (Competitions.getCurrentEvent() == null) {
            SportingEvent event = Competitions.EVENTS.get(Objects.requireNonNull(item.getItemMeta()
                    .getPersistentDataContainer().get(ItemUtil.EVENT_ID, KeyDataType.KEY_DATA_TYPE)));
            if (event == null)
                return;
            SportingEvent.leavePractice(p);
            event.joinPractice(p);
        }
    }

    private @NotNull ItemStack practice(@NotNull SportingEvent event) {
        ItemStack stack = ItemUtil.item(event.getDisplayItem(), "event_practice",
                Component.translatable("gui.menu.practice").arguments(event.getName()),
                Component.translatable(event instanceof TrackEvent ? "event.type.track" : "event.type.field").color(NamedTextColor.GRAY));
        stack.editMeta(meta -> {
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            meta.getPersistentDataContainer().set(ItemUtil.EVENT_ID, KeyDataType.KEY_DATA_TYPE, event.getKey());
        });
        return stack;
    }
}
