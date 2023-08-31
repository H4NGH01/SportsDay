package org.macausmp.sportsday.gui.competition;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.competition.Competitions;
import org.macausmp.sportsday.competition.IEvent;
import org.macausmp.sportsday.gui.AbstractGUI;
import org.macausmp.sportsday.gui.GUIButton;
import org.macausmp.sportsday.gui.GUIManager;
import org.macausmp.sportsday.util.ItemUtil;

public class CompetitionSettingsGUI extends AbstractGUI {
    public CompetitionSettingsGUI() {
        super(54, Component.translatable("gui.settings.title"));
        for (int i = 0; i < 9; i++) {
            getInventory().setItem(i + 9, GUIButton.BOARD);
        }
        getInventory().setItem(0, GUIButton.COMPETITION_INFO);
        getInventory().setItem(1, GUIButton.PLAYER_LIST);
        getInventory().setItem(2, GUIButton.START_COMPETITION);
        getInventory().setItem(3, GUIButton.END_COMPETITION);
        getInventory().setItem(4, ItemUtil.addEffect(GUIButton.COMPETITION_SETTINGS));
        getInventory().setItem(5, GUIButton.VERSION);
        getInventory().setItem(18, GUIButton.ELYTRA_RACING);
        getInventory().setItem(19, GUIButton.ICE_BOAT_RACING);
        getInventory().setItem(20, GUIButton.JAVELIN_THROW);
        getInventory().setItem(21, GUIButton.OBSTACLE_COURSE);
        getInventory().setItem(22, GUIButton.PARKOUR);
        getInventory().setItem(23, GUIButton.SUMO);
    }

    @Override
    public void update() {
        getInventory().setItem(27, status(Competitions.ELYTRA_RACING));
        getInventory().setItem(28, status(Competitions.ICE_BOAT_RACING));
        getInventory().setItem(29, status(Competitions.JAVELIN_THROW));
        getInventory().setItem(30, status(Competitions.OBSTACLE_COURSE));
        getInventory().setItem(31, status(Competitions.PARKOUR));
        getInventory().setItem(32, status(Competitions.SUMO));
    }

    @Override
    public void onClick(@NotNull InventoryClickEvent e, Player p, @NotNull ItemStack item) {
        String id = item.getItemMeta().getPersistentDataContainer().get(ItemUtil.COMPETITION_ID, PersistentDataType.STRING);
        if (ItemUtil.isSameItem(item, "status_toggle")) {
            for (IEvent event : Competitions.COMPETITIONS) {
                if (event.getID().equals(id)) {
                    PLUGIN.getConfig().set(event.getID() + ".enable", !event.isEnable());
                    PLUGIN.saveConfig();
                    GUIManager.COMPETITION_SETTINGS_GUI.update();
                    p.playSound(p, event.isEnable() ? Sound.ENTITY_ARROW_HIT_PLAYER : Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f);
                    return;
                }
            }
        }
    }

    private @NotNull ItemStack status(@NotNull IEvent event) {
        ItemStack stack = ItemUtil.item(event.isEnable() ? Material.LIME_DYE : Material.BARRIER, "status_toggle", event.isEnable() ? "gui.enabled" : "gui.disabled", "gui.toggle");
        stack.editMeta(meta -> meta.getPersistentDataContainer().set(ItemUtil.COMPETITION_ID, PersistentDataType.STRING, event.getID()));
        return stack;
    }
}
