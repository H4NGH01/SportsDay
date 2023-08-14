package org.macausmp.sportsday.gui;

import net.kyori.adventure.text.Component;
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
import org.macausmp.sportsday.util.Translation;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CompetitionSettingsGUI extends AbstractGUI {
    public CompetitionSettingsGUI() {
        super(54, Translation.translatable("gui.title.settings"));
        for (int i = 0; i < 9; i++) {
            getInventory().setItem(i + 9, GUIButton.BOARD);
        }
        getInventory().setItem(0, GUIButton.COMPETITION_INFO);
        getInventory().setItem(1, GUIButton.PLAYER_LIST);
        getInventory().setItem(2, GUIButton.START_COMPETITION);
        getInventory().setItem(3, GUIButton.END_COMPETITION);
        getInventory().setItem(4, GUIButton.COMPETITION_SETTINGS_SELECTED);
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
        getInventory().setItem(27, status(Competitions.ELYTRA_RACING));
        getInventory().setItem(28, status(Competitions.ICE_BOAT_RACING));
        getInventory().setItem(29, status(Competitions.JAVELIN_THROW));
        getInventory().setItem(30, status(Competitions.OBSTACLE_COURSE));
        getInventory().setItem(31, status(Competitions.PARKOUR));
        getInventory().setItem(32, status(Competitions.SUMO));
    }

    @Override
    public void onClick(@NotNull InventoryClickEvent event, Player p, @NotNull ItemStack item) {
        PersistentDataContainer container = item.getItemMeta().getPersistentDataContainer();
        if (container.has(SportsDay.ITEM_ID, PersistentDataType.STRING) && Objects.equals(container.get(SportsDay.ITEM_ID, PersistentDataType.STRING), "status_toggle")) {
            for (ICompetition competition : Competitions.COMPETITIONS) {
                if (competition.getID().equals(container.get(SportsDay.COMPETITION_ID, PersistentDataType.STRING))) {
                    plugin.getConfig().set(competition.getID() + ".enable", !competition.isEnable());
                    plugin.saveConfig();
                    CompetitionGUI.COMPETITION_SETTINGS_GUI.update();
                    p.playSound(p, competition.isEnable() ? Sound.ENTITY_ARROW_HIT_PLAYER : Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f);
                    return;
                }
            }
        }
    }

    private @NotNull ItemStack status(@NotNull ICompetition competition) {
        ItemStack stack = new ItemStack(competition.isEnable() ? Material.LIME_DYE : Material.BARRIER);
        stack.editMeta(meta -> {
            meta.displayName(Translation.translatable(competition.isEnable() ? "gui.enabled" : "gui.disabled"));
            meta.getPersistentDataContainer().set(SportsDay.ITEM_ID, PersistentDataType.STRING, "status_toggle");
            meta.getPersistentDataContainer().set(SportsDay.COMPETITION_ID, PersistentDataType.STRING, competition.getID());
            List<Component> lore = new ArrayList<>();
            lore.add(Translation.translatable("gui.toggle"));
            meta.lore(lore);
        });
        return stack;
    }
}
