package org.macausmp.sportsday.gui;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.SportsDay;
import org.macausmp.sportsday.competition.Competitions;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class GUIButton {
    public static final NamespacedKey ITEM_ID = new NamespacedKey(SportsDay.getInstance(), "item_id");
    public static final NamespacedKey COMPETITION_ID = new NamespacedKey(SportsDay.getInstance(), "competition_id");
    public static final ItemStack COMPETITION_INFO = competitionInfo();
    public static final ItemStack PLAYER_LIST = playerlist();
    public static final ItemStack START_COMPETITION = startCompetition();
    public static final ItemStack END_COMPETITION = endCompetition();
    public static final ItemStack COMPETITION_SETTINGS = competitionSettings();
    public static final ItemStack ELYTRA_RACING = elytraRacing();
    public static final ItemStack ICE_BOAT_RACING = iceBoatRacing();
    public static final ItemStack JAVELIN_THROW = javelinThrow();
    public static final ItemStack OBSTACLE_COURSE = obstacleCourse();
    public static final ItemStack PARKOUR = parkour();
    public static final ItemStack SUMO = sumo();
    public static final ItemStack BOARD = board();

    public static @NotNull ItemStack competitionInfo() {
        ItemStack stack = new ItemStack(Material.GOLD_BLOCK);
        stack.editMeta(meta -> {
            meta.displayName(Component.text("比賽資訊").decoration(TextDecoration.ITALIC, false));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.text("點擊查看比賽資訊").decoration(TextDecoration.ITALIC, false).color(NamedTextColor.GRAY));
            meta.lore(lore);
            meta.getPersistentDataContainer().set(ITEM_ID, PersistentDataType.STRING, "competition_info");
        });
        return stack;
    }

    public static @NotNull ItemStack playerlist() {
        ItemStack stack = new ItemStack(Material.PAPER);
        stack.editMeta(meta -> {
            meta.displayName(Component.text("參賽選手名單").decoration(TextDecoration.ITALIC, false));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.text("點擊查看參賽選手名單").decoration(TextDecoration.ITALIC, false).color(NamedTextColor.GRAY));
            meta.lore(lore);
            meta.getPersistentDataContainer().set(ITEM_ID, PersistentDataType.STRING, "player_list");
        });
        return stack;
    }

    public static @NotNull ItemStack startCompetition() {
        ItemStack stack = new ItemStack(Material.GREEN_CONCRETE);
        stack.editMeta(meta -> {
            meta.displayName(Component.text("開始一場比賽").decoration(TextDecoration.ITALIC, false));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.text("點擊查看可用比賽").decoration(TextDecoration.ITALIC, false).color(NamedTextColor.GRAY));
            meta.lore(lore);
            meta.getPersistentDataContainer().set(ITEM_ID, PersistentDataType.STRING, "start_competition");
        });
        return stack;
    }

    public static @NotNull ItemStack endCompetition() {
        ItemStack stack = new ItemStack(Material.RED_CONCRETE);
        stack.editMeta(meta -> {
            meta.displayName(Component.text("結束當前比賽").decoration(TextDecoration.ITALIC, false));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.text("點擊結束當前比賽").decoration(TextDecoration.ITALIC, false).color(NamedTextColor.GRAY));
            meta.lore(lore);
            meta.getPersistentDataContainer().set(ITEM_ID, PersistentDataType.STRING, "end_competition");
        });
        return stack;
    }

    public static @NotNull ItemStack competitionSettings() {
        ItemStack stack = new ItemStack(Material.REPEATER);
        stack.editMeta(meta -> {
            meta.displayName(Component.text("比賽設定").decoration(TextDecoration.ITALIC, false));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.text("點擊查看比賽設定").decoration(TextDecoration.ITALIC, false).color(NamedTextColor.GRAY));
            meta.lore(lore);
            meta.getPersistentDataContainer().set(ITEM_ID, PersistentDataType.STRING, "competition_settings");
        });
        return stack;
    }

    public static @NotNull ItemStack elytraRacing() {
        ItemStack stack = new ItemStack(Material.ELYTRA);
        stack.editMeta(meta -> {
            meta.displayName(Competitions.ELYTRA_RACING.getName());
            List<Component> lore = new ArrayList<>();
            lore.add(Component.text("點擊選擇該比賽").decoration(TextDecoration.ITALIC, false).color(NamedTextColor.GRAY));
            meta.lore(lore);
            meta.getPersistentDataContainer().set(ITEM_ID, PersistentDataType.STRING, "competition");
            meta.getPersistentDataContainer().set(COMPETITION_ID, PersistentDataType.STRING, Competitions.ELYTRA_RACING.getID());
        });
        return stack;
    }

    public static @NotNull ItemStack iceBoatRacing() {
        ItemStack stack = new ItemStack(Material.OAK_BOAT);
        stack.editMeta(meta -> {
            meta.displayName(Competitions.ICE_BOAT_RACING.getName());
            List<Component> lore = new ArrayList<>();
            lore.add(Component.text("點擊選擇該比賽").decoration(TextDecoration.ITALIC, false).color(NamedTextColor.GRAY));
            meta.lore(lore);
            meta.getPersistentDataContainer().set(ITEM_ID, PersistentDataType.STRING, "competition");
            meta.getPersistentDataContainer().set(COMPETITION_ID, PersistentDataType.STRING, Competitions.ICE_BOAT_RACING.getID());
        });
        return stack;
    }

    public static @NotNull ItemStack javelinThrow() {
        ItemStack stack = new ItemStack(Material.TRIDENT);
        stack.editMeta(meta -> {
            meta.displayName(Competitions.JAVELIN_THROW.getName());
            List<Component> lore = new ArrayList<>();
            lore.add(Component.text("點擊選擇該比賽").decoration(TextDecoration.ITALIC, false).color(NamedTextColor.GRAY));
            meta.lore(lore);
            meta.getPersistentDataContainer().set(ITEM_ID, PersistentDataType.STRING, "competition");
            meta.getPersistentDataContainer().set(COMPETITION_ID, PersistentDataType.STRING, Competitions.JAVELIN_THROW.getID());
        });
        return stack;
    }

    public static @NotNull ItemStack obstacleCourse() {
        ItemStack stack = new ItemStack(Material.OAK_FENCE_GATE);
        stack.editMeta(meta -> {
            meta.displayName(Competitions.OBSTACLE_COURSE.getName());
            List<Component> lore = new ArrayList<>();
            lore.add(Component.text("點擊選擇該比賽").decoration(TextDecoration.ITALIC, false).color(NamedTextColor.GRAY));
            meta.lore(lore);
            meta.getPersistentDataContainer().set(ITEM_ID, PersistentDataType.STRING, "competition");
            meta.getPersistentDataContainer().set(COMPETITION_ID, PersistentDataType.STRING, Competitions.OBSTACLE_COURSE.getID());
        });
        return stack;
    }

    public static @NotNull ItemStack parkour() {
        ItemStack stack = new ItemStack(Material.LEATHER_BOOTS);
        stack.editMeta(meta -> {
            meta.displayName(Competitions.PARKOUR.getName());
            List<Component> lore = new ArrayList<>();
            lore.add(Component.text("點擊選擇該比賽").decoration(TextDecoration.ITALIC, false).color(NamedTextColor.GRAY));
            meta.lore(lore);
            meta.getPersistentDataContainer().set(ITEM_ID, PersistentDataType.STRING, "competition");
            meta.getPersistentDataContainer().set(COMPETITION_ID, PersistentDataType.STRING, Competitions.PARKOUR.getID());
        });
        return stack;
    }

    public static @NotNull ItemStack sumo() {
        ItemStack stack = new ItemStack(Material.COD);
        stack.editMeta(meta -> {
            meta.displayName(Competitions.SUMO.getName());
            List<Component> lore = new ArrayList<>();
            lore.add(Component.text("點擊選擇該比賽").decoration(TextDecoration.ITALIC, false).color(NamedTextColor.GRAY));
            meta.lore(lore);
            meta.getPersistentDataContainer().set(ITEM_ID, PersistentDataType.STRING, "competition");
            meta.getPersistentDataContainer().set(COMPETITION_ID, PersistentDataType.STRING, Competitions.SUMO.getID());
        });
        return stack;
    }

    public static @NotNull ItemStack board() {
        ItemStack stack = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        stack.editMeta(meta -> meta.displayName(Component.text("")));
        return stack;
    }

    @Contract("_ -> param1")
    public static @NotNull ItemStack addEffect(@NotNull ItemStack stack) {
        stack.addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 0);
        stack.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        return stack;
    }

    public static boolean isSameButton(@NotNull ItemStack button, @NotNull ItemStack button2) {
        if (!button.hasItemMeta() || !button2.hasItemMeta()) return false;
        String id1 = button.getItemMeta().getPersistentDataContainer().get(ITEM_ID, PersistentDataType.STRING);
        String id2 = button2.getItemMeta().getPersistentDataContainer().get(ITEM_ID, PersistentDataType.STRING);
        return Objects.equals(id1, id2);
    }
}
