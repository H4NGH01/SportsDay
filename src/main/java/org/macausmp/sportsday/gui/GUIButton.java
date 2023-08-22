package org.macausmp.sportsday.gui;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.SportsDay;
import org.macausmp.sportsday.competition.Competitions;
import org.macausmp.sportsday.util.SkullTextureUtil;
import org.macausmp.sportsday.util.TextUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class GUIButton {
    public static final ItemStack COMPETITION_INFO = competitionInfo();
    public static final ItemStack COMPETITION_INFO_SELECTED = addEffect(competitionInfo());
    public static final ItemStack PLAYER_LIST = playerlist();
    public static final ItemStack PLAYER_LIST_SELECTED = addEffect(playerlist());
    public static final ItemStack START_COMPETITION = startCompetition();
    public static final ItemStack START_COMPETITION_SELECTED = addEffect(startCompetition());
    public static final ItemStack END_COMPETITION = endCompetition();
    public static final ItemStack COMPETITION_SETTINGS = competitionSettings();
    public static final ItemStack COMPETITION_SETTINGS_SELECTED = addEffect(competitionSettings());
    public static final ItemStack VERSION = version();
    public static final ItemStack NEXT_PAGE = nextPage();
    public static final ItemStack PREVIOUS_PAGE = previousPage();
    public static final ItemStack BACK = back();
    public static final ItemStack ELYTRA_RACING = elytraRacing();
    public static final ItemStack ICE_BOAT_RACING = iceBoatRacing();
    public static final ItemStack JAVELIN_THROW = javelinThrow();
    public static final ItemStack OBSTACLE_COURSE = obstacleCourse();
    public static final ItemStack PARKOUR = parkour();
    public static final ItemStack SUMO = sumo();
    public static final ItemStack CLOTHING = clothing();
    public static final ItemStack BOAT_TYPE = boat();
    public static final ItemStack WEAPON_SKIN = weapon();
    public static final ItemStack MUSICKIT = musickit();
    public static final ItemStack PROJECTILE_TRAIL = projectile();
    public static final ItemStack WALKING_EFFECT = walking();
    public static final ItemStack GRAFFITI_SPRAY = graffiti();
    public static final ItemStack BOARD = board();

    private static @NotNull ItemStack competitionInfo() {
        ItemStack stack = new ItemStack(Material.GOLD_BLOCK);
        stack.editMeta(meta -> {
            meta.displayName(TextUtil.text(Component.translatable("gui.info.title")));
            List<Component> lore = new ArrayList<>();
            lore.add(TextUtil.text(Component.translatable("gui.info.lore")));
            meta.lore(lore);
            meta.getPersistentDataContainer().set(SportsDay.ITEM_ID, PersistentDataType.STRING, "competition_info");
        });
        return stack;
    }

    private static @NotNull ItemStack playerlist() {
        ItemStack stack = new ItemStack(Material.PAPER);
        stack.editMeta(meta -> {
            meta.displayName(TextUtil.text(Component.translatable("gui.player_list.title")));
            List<Component> lore = new ArrayList<>();
            lore.add(TextUtil.text(Component.translatable("gui.player_list.lore")));
            meta.lore(lore);
            meta.getPersistentDataContainer().set(SportsDay.ITEM_ID, PersistentDataType.STRING, "player_list");
        });
        return stack;
    }

    private static @NotNull ItemStack startCompetition() {
        @SuppressWarnings("SpellCheckingInspection") ItemStack stack = SkullTextureUtil.getSkull("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMmEzYjhmNjgxZGFhZDhiZjQzNmNhZThkYTNmZTgxMzFmNjJhMTYyYWI4MWFmNjM5YzNlMDY0NGFhNmFiYWMyZiJ9fX0=");
        stack.editMeta(meta -> {
            meta.displayName(TextUtil.text(Component.translatable("gui.start.title")));
            List<Component> lore = new ArrayList<>();
            lore.add(TextUtil.text(Component.translatable("gui.start.lore")));
            meta.lore(lore);
            meta.getPersistentDataContainer().set(SportsDay.ITEM_ID, PersistentDataType.STRING, "start_competitions");
        });
        return stack;
    }

    private static @NotNull ItemStack endCompetition() {
        ItemStack stack = new ItemStack(Material.RED_CONCRETE);
        stack.editMeta(meta -> {
            meta.displayName(TextUtil.text(Component.translatable("gui.end.title")));
            List<Component> lore = new ArrayList<>();
            lore.add(TextUtil.text(Component.translatable("gui.end.lore")));
            meta.lore(lore);
            meta.getPersistentDataContainer().set(SportsDay.ITEM_ID, PersistentDataType.STRING, "end_competition");
        });
        return stack;
    }

    private static @NotNull ItemStack competitionSettings() {
        ItemStack stack = new ItemStack(Material.REPEATER);
        stack.editMeta(meta -> {
            meta.displayName(TextUtil.text(Component.translatable("gui.settings.title")));
            List<Component> lore = new ArrayList<>();
            lore.add(TextUtil.text(Component.translatable("gui.settings.lore")));
            meta.lore(lore);
            meta.getPersistentDataContainer().set(SportsDay.ITEM_ID, PersistentDataType.STRING, "competition_settings");
        });
        return stack;
    }

    private static @NotNull ItemStack version() {
        ItemStack version = new ItemStack(Material.OAK_SIGN);
        version.editMeta(meta -> {
            //noinspection deprecation
            meta.displayName(TextUtil.text(Component.translatable("gui.plugin_version").args(Component.text(SportsDay.getInstance().getDescription().getVersion()))));
            meta.getPersistentDataContainer().set(SportsDay.ITEM_ID, PersistentDataType.STRING, "version");
        });
        return version;
    }

    private static @NotNull ItemStack nextPage() {
        ItemStack version = new ItemStack(Material.BLUE_STAINED_GLASS_PANE);
        version.editMeta(meta -> {
            meta.displayName(TextUtil.text(Component.translatable("gui.page.next")));
            meta.getPersistentDataContainer().set(SportsDay.ITEM_ID, PersistentDataType.STRING, "next_page");
        });
        return version;
    }

    private static @NotNull ItemStack previousPage() {
        ItemStack version = new ItemStack(Material.BLUE_STAINED_GLASS_PANE);
        version.editMeta(meta -> {
            meta.displayName(TextUtil.text(Component.translatable("gui.page.prev")));
            meta.getPersistentDataContainer().set(SportsDay.ITEM_ID, PersistentDataType.STRING, "prev_page");
        });
        return version;
    }

    private static @NotNull ItemStack back() {
        ItemStack version = new ItemStack(Material.ARROW);
        version.editMeta(meta -> {
            meta.displayName(Component.translatable("gui.back").decoration(TextDecoration.ITALIC, false));
            meta.getPersistentDataContainer().set(SportsDay.ITEM_ID, PersistentDataType.STRING, "back");
        });
        return version;
    }

    private static @NotNull ItemStack elytraRacing() {
        ItemStack stack = new ItemStack(Material.ELYTRA);
        stack.editMeta(meta -> {
            meta.displayName(Competitions.ELYTRA_RACING.getName());
            meta.getPersistentDataContainer().set(SportsDay.ITEM_ID, PersistentDataType.STRING, "competition");
            meta.getPersistentDataContainer().set(SportsDay.COMPETITION_ID, PersistentDataType.STRING, Competitions.ELYTRA_RACING.getID());
        });
        return stack;
    }

    private static @NotNull ItemStack iceBoatRacing() {
        ItemStack stack = new ItemStack(Material.OAK_BOAT);
        stack.editMeta(meta -> {
            meta.displayName(Competitions.ICE_BOAT_RACING.getName());
            meta.getPersistentDataContainer().set(SportsDay.ITEM_ID, PersistentDataType.STRING, "competition");
            meta.getPersistentDataContainer().set(SportsDay.COMPETITION_ID, PersistentDataType.STRING, Competitions.ICE_BOAT_RACING.getID());
        });
        return stack;
    }

    private static @NotNull ItemStack javelinThrow() {
        ItemStack stack = new ItemStack(Material.TRIDENT);
        stack.editMeta(meta -> {
            meta.displayName(Competitions.JAVELIN_THROW.getName());
            meta.getPersistentDataContainer().set(SportsDay.ITEM_ID, PersistentDataType.STRING, "competition");
            meta.getPersistentDataContainer().set(SportsDay.COMPETITION_ID, PersistentDataType.STRING, Competitions.JAVELIN_THROW.getID());
        });
        return stack;
    }

    private static @NotNull ItemStack obstacleCourse() {
        ItemStack stack = new ItemStack(Material.OAK_FENCE_GATE);
        stack.editMeta(meta -> {
            meta.displayName(Competitions.OBSTACLE_COURSE.getName());
            meta.getPersistentDataContainer().set(SportsDay.ITEM_ID, PersistentDataType.STRING, "competition");
            meta.getPersistentDataContainer().set(SportsDay.COMPETITION_ID, PersistentDataType.STRING, Competitions.OBSTACLE_COURSE.getID());
        });
        return stack;
    }

    private static @NotNull ItemStack parkour() {
        ItemStack stack = new ItemStack(Material.LEATHER_BOOTS);
        stack.editMeta(meta -> {
            meta.displayName(Competitions.PARKOUR.getName());
            meta.getPersistentDataContainer().set(SportsDay.ITEM_ID, PersistentDataType.STRING, "competition");
            meta.getPersistentDataContainer().set(SportsDay.COMPETITION_ID, PersistentDataType.STRING, Competitions.PARKOUR.getID());
        });
        return stack;
    }

    private static @NotNull ItemStack sumo() {
        ItemStack stack = new ItemStack(Material.COD);
        stack.editMeta(meta -> {
            meta.displayName(Competitions.SUMO.getName());
            meta.getPersistentDataContainer().set(SportsDay.ITEM_ID, PersistentDataType.STRING, "competition");
            meta.getPersistentDataContainer().set(SportsDay.COMPETITION_ID, PersistentDataType.STRING, Competitions.SUMO.getID());
        });
        return stack;
    }

    private static @NotNull ItemStack clothing() {
        ItemStack stack = new ItemStack(Material.LEATHER_CHESTPLATE);
        stack.editMeta(meta -> {
            meta.displayName(TextUtil.text(Component.translatable("gui.customize.clothing.title").color(NamedTextColor.YELLOW)));
            meta.getPersistentDataContainer().set(SportsDay.ITEM_ID, PersistentDataType.STRING, "customize_clothing");
        });
        return stack;
    }

    private static @NotNull ItemStack boat() {
        ItemStack stack = new ItemStack(Material.OAK_BOAT);
        stack.editMeta(meta -> {
            meta.displayName(TextUtil.text(Component.translatable("gui.customize.boat_type.title").color(NamedTextColor.YELLOW)));
            meta.getPersistentDataContainer().set(SportsDay.ITEM_ID, PersistentDataType.STRING, "customize_boat");
        });
        return stack;
    }

    private static @NotNull ItemStack weapon() {
        ItemStack stack = new ItemStack(Material.BONE);
        stack.editMeta(meta -> {
            meta.displayName(TextUtil.text(Component.translatable("gui.customize.weapon_skin.title").color(NamedTextColor.YELLOW)));
            meta.getPersistentDataContainer().set(SportsDay.ITEM_ID, PersistentDataType.STRING, "customize_weapon");
        });
        return stack;
    }

    private static @NotNull ItemStack musickit() {
        ItemStack stack = new ItemStack(Material.JUKEBOX);
        stack.editMeta(meta -> {
            meta.displayName(TextUtil.text(Component.translatable("gui.customize.musickit.title").color(NamedTextColor.YELLOW)));
            meta.getPersistentDataContainer().set(SportsDay.ITEM_ID, PersistentDataType.STRING, "customize_musickit");
        });
        return stack;
    }

    private static @NotNull ItemStack projectile() {
        ItemStack stack = new ItemStack(Material.ARROW);
        stack.editMeta(meta -> {
            meta.displayName(TextUtil.text(Component.translatable("gui.customize.projectile_trail.title").color(NamedTextColor.YELLOW)));
            meta.getPersistentDataContainer().set(SportsDay.ITEM_ID, PersistentDataType.STRING, "customize_projectile_trail");
        });
        return stack;
    }

    private static @NotNull ItemStack walking() {
        ItemStack stack = new ItemStack(Material.NETHER_STAR);
        stack.editMeta(meta -> {
            meta.displayName(TextUtil.text(Component.translatable("gui.customize.walking_effect.title").color(NamedTextColor.YELLOW)));
            meta.getPersistentDataContainer().set(SportsDay.ITEM_ID, PersistentDataType.STRING, "customize_walking_effect");
        });
        return stack;
    }

    private static @NotNull ItemStack graffiti() {
        ItemStack stack = new ItemStack(Material.PAINTING);
        stack.editMeta(meta -> {
            meta.displayName(TextUtil.text(Component.translatable("gui.customize.graffiti_spray.title").color(NamedTextColor.YELLOW)));
            meta.getPersistentDataContainer().set(SportsDay.ITEM_ID, PersistentDataType.STRING, "customize_graffiti_spray");
        });
        return stack;
    }

    private static @NotNull ItemStack board() {
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

    public static boolean isButton(@NotNull ItemStack button) {
        return button.hasItemMeta() && button.getItemMeta().getPersistentDataContainer().has(SportsDay.ITEM_ID, PersistentDataType.STRING);
    }

    public static boolean isSameButton(@NotNull ItemStack button, @NotNull ItemStack button2) {
        if (!button.hasItemMeta() || !button2.hasItemMeta()) return false;
        String id1 = button.getItemMeta().getPersistentDataContainer().get(SportsDay.ITEM_ID, PersistentDataType.STRING);
        String id2 = button2.getItemMeta().getPersistentDataContainer().get(SportsDay.ITEM_ID, PersistentDataType.STRING);
        return Objects.equals(id1, id2);
    }

    public static boolean isSameButton(@NotNull ItemStack button, @NotNull String key) {
        if (!button.hasItemMeta()) return false;
        String id = button.getItemMeta().getPersistentDataContainer().get(SportsDay.ITEM_ID, PersistentDataType.STRING);
        return Objects.equals(id, key);
    }
}
