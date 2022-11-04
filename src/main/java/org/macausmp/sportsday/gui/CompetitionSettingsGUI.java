package org.macausmp.sportsday.gui;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.SportsDay;
import org.macausmp.sportsday.competition.Competitions;
import org.macausmp.sportsday.competition.ICompetition;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CompetitionSettingsGUI extends AbstractGUI {
    private static final ItemStack SELECTED = GUIButton.addEffect(GUIButton.competitionSettings());

    public CompetitionSettingsGUI() {
        super(54, Component.text("比賽設定"));
        for (int i = 0; i < 9; i++) {
            getInventory().setItem(i + 9, GUIButton.BOARD);
        }
        getInventory().setItem(0, GUIButton.COMPETITION_INFO);
        getInventory().setItem(1, GUIButton.PLAYER_LIST);
        getInventory().setItem(2, GUIButton.START_COMPETITION);
        getInventory().setItem(3, GUIButton.END_COMPETITION);
        getInventory().setItem(4, SELECTED);
        getInventory().setItem(5, GUIButton.VERSION);
    }

    @Override
    public void update() {
        getInventory().setItem(18, GUIButton.ELYTRA_RACING);
        getInventory().setItem(19, GUIButton.ICE_BOAT_RACING);
        getInventory().setItem(20, GUIButton.JAVELIN_THROW);
        getInventory().setItem(21, GUIButton.OBSTACLE_COURSE);
        getInventory().setItem(22, GUIButton.PARKOUR);
        getInventory().setItem(23, GUIButton.SUMO);
        getInventory().setItem(27, enable(Competitions.ELYTRA_RACING));
        getInventory().setItem(28, enable(Competitions.ICE_BOAT_RACING));
        getInventory().setItem(29, enable(Competitions.JAVELIN_THROW));
        getInventory().setItem(30, enable(Competitions.OBSTACLE_COURSE));
        getInventory().setItem(31, enable(Competitions.PARKOUR));
        getInventory().setItem(32, enable(Competitions.SUMO));
    }

    @Override
    public void onClick(@NotNull InventoryClickEvent event) {
        Player p = (Player) event.getWhoClicked();
        ItemStack item = Objects.requireNonNull(event.getCurrentItem());
        PersistentDataContainer container = item.getItemMeta().getPersistentDataContainer();
        if (container.has(SportsDay.ITEM_ID, PersistentDataType.STRING) && Objects.equals(container.get(SportsDay.ITEM_ID, PersistentDataType.STRING), "enable_switch")) {
            for (ICompetition competition : Competitions.COMPETITIONS) {
                if (competition.getID().equals(container.get(SportsDay.COMPETITION_ID, PersistentDataType.STRING))) {
                    SportsDay.getInstance().getConfig().set(competition.getID() + ".enable", !competition.isEnable());
                    SportsDay.getInstance().saveConfig();
                    CompetitionGUI.COMPETITION_SETTINGS_GUI.update();
                    p.playSound(p, competition.isEnable() ? Sound.ENTITY_ARROW_HIT_PLAYER : Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f);
                    return;
                }
            }
        }
    }

    private @NotNull ItemStack enable(@NotNull ICompetition competition) {
        ItemStack stack = new ItemStack(competition.isEnable() ? Material.LIME_DYE : Material.BARRIER);
        stack.editMeta(meta -> {
            Component c = competition.isEnable() ? Component.text("是").color(NamedTextColor.YELLOW) : Component.text("否").color(NamedTextColor.RED);
            meta.displayName(Component.text("啟用: ").decoration(TextDecoration.ITALIC, false).color(NamedTextColor.YELLOW).append(c));
            meta.getPersistentDataContainer().set(SportsDay.ITEM_ID, PersistentDataType.STRING, "enable_switch");
            meta.getPersistentDataContainer().set(SportsDay.COMPETITION_ID, PersistentDataType.STRING, competition.getID());
            List<Component> lore = new ArrayList<>();
            lore.add(Component.text("點擊切換").decoration(TextDecoration.ITALIC, false).color(NamedTextColor.GRAY));
            meta.lore(lore);
        });
        return stack;
    }
}
