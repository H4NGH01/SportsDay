package org.macausmp.sportsday.gui.competition;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.competition.Competitions;
import org.macausmp.sportsday.gui.ButtonHandler;
import org.macausmp.sportsday.gui.GUIButton;
import org.macausmp.sportsday.util.CompetitorData;
import org.macausmp.sportsday.util.ItemUtil;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class CompetitorProfileGUI extends AbstractCompetitionGUI {
    private static final Set<CompetitorProfileGUI> HANDLER = new HashSet<>();
    private final CompetitorData data;

    public CompetitorProfileGUI(@NotNull CompetitorData data) {
        super(54, Component.translatable("gui.competitor_profile.title").args(Component.text(data.getName())));
        this.data = data;
        for (int i = 0; i < 9; i++) {
            getInventory().setItem(i + 9, GUIButton.BOARD);
        }
        getInventory().setItem(0, GUIButton.COMPETITION_INFO);
        getInventory().setItem(1, GUIButton.COMPETITOR_LIST);
        getInventory().setItem(2, GUIButton.COMPETITION_SETTINGS);
        getInventory().setItem(3, GUIButton.VERSION);
        getInventory().setItem(18, icon());
        getInventory().setItem(19, increase());
        getInventory().setItem(20, decrease());
        getInventory().setItem(26, unregister());
        HANDLER.add(this);
    }

    @Override
    public void update() {
        getInventory().setItem(18, icon());
    }

    public static void updateProfile(UUID uuid) {
        for (CompetitorProfileGUI gui : HANDLER) {
            if (gui.data.getUUID().equals(uuid)) gui.update();
        }
    }

    @ButtonHandler("increase")
    public void increase(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        if (data.isRemoved()) {
            p.sendMessage(Component.translatable("command.competition.unregister.failed.other").args(Component.text(data.getPlayer().getName())).color(NamedTextColor.RED));
            p.closeInventory();
            return;
        }
        data.addScore(e.isLeftClick() ? 1 : e.isRightClick() ? 5 : 0);
        p.playSound(Sound.sound(Key.key("minecraft:ui.button.click"), Sound.Source.MASTER, 1f, 1f));
        update();
    }

    @ButtonHandler("decrease")
    public void decrease(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        if (data.isRemoved()) {
            p.sendMessage(Component.translatable("command.competition.unregister.failed.other").args(Component.text(data.getPlayer().getName())).color(NamedTextColor.RED));
            p.closeInventory();
            return;
        }
        data.addScore(e.isLeftClick() ? -1 : e.isRightClick() ? -5 : 0);
        p.playSound(Sound.sound(Key.key("minecraft:ui.button.click"), Sound.Source.MASTER, 1f, 1f));
        update();
    }

    @ButtonHandler("unregister")
    public void unregister(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        p.sendMessage(Competitions.leave(data.getPlayer()) ? Component.translatable("command.competition.unregister.success.other").args(Component.text(data.getPlayer().getName())).color(NamedTextColor.GREEN) : Component.translatable("command.competition.unregister.failed.other").args(Component.text(data.getPlayer().getName())).color(NamedTextColor.RED));
        p.closeInventory();
    }

    private @NotNull ItemStack icon() {
        Component online = Component.translatable(data.isOnline() ? "competitor.online" : "competitor.offline");
        Component display = Component.translatable(Objects.requireNonNull(data.getName()) + " (%s)").args(online);
        Component number = Component.translatable("competitor.number").args(Component.text(data.getNumber())).color(NamedTextColor.YELLOW);
        Component score = Component.translatable("competitor.score").args(Component.text(data.getScore())).color(NamedTextColor.YELLOW);
        ItemStack icon = ItemUtil.item(Material.PLAYER_HEAD, null, display, number, score);
        icon.editMeta(SkullMeta.class, meta -> meta.setOwningPlayer(data.getOfflinePlayer()));
        return icon;
    }

    private @NotNull ItemStack unregister() {
        Component display = Component.translatable("gui.competitor.unregister").args(Component.text(data.getName())).color(NamedTextColor.RED);
        Component lore = Component.translatable("gui.competitor.unregister_lore");
        return ItemUtil.item(Material.RED_CONCRETE, "unregister", display, lore);
    }

    private @NotNull ItemStack increase() {
        return ItemUtil.item(Material.YELLOW_CONCRETE, "increase", "gui.competitor.increase", "gui.competitor.increase_lore1", "gui.competitor.increase_lore2");
    }

    private @NotNull ItemStack decrease() {
        return ItemUtil.item(Material.RED_CONCRETE, "decrease", "gui.competitor.decrease", "gui.competitor.decrease_lore1", "gui.competitor.decrease_lore2");
    }

    @Override
    public void onClose() {
        HANDLER.remove(this);
    }
}
