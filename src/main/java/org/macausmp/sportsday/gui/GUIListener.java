package org.macausmp.sportsday.gui;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.competition.Competitions;
import org.macausmp.sportsday.competition.ICompetition;

public class GUIListener implements Listener {
    @EventHandler
    public void onClick(@NotNull InventoryClickEvent e) {
        if (e.getWhoClicked() instanceof Player p) {
            if (CompetitionGUI.GUI_MAP.containsKey(p)) {
                e.setCancelled(true);
                ItemStack item = e.getCurrentItem();
                if (item != null && GUIButton.isButton(item) && e.getClickedInventory() != null && e.getClickedInventory().getType() != InventoryType.PLAYER) {
                    if (GUIButton.isSameButton(item, GUIButton.COMPETITION_INFO)) {
                        p.playSound(p, Sound.BLOCK_WOODEN_BUTTON_CLICK_ON, 1f, 1f);
                        CompetitionGUI.COMPETITION_INFO_GUI.openTo(p);
                        return;
                    } else if (GUIButton.isSameButton(item, GUIButton.PLAYER_LIST)) {
                        p.playSound(p, Sound.ITEM_BOOK_PAGE_TURN, 1f, 1f);
                        new PlayerListGUI().openTo(p);
                    } else if (GUIButton.isSameButton(item, GUIButton.START_COMPETITION)) {
                        if (Competitions.getCurrentlyCompetition() != null && Competitions.getCurrentlyCompetition().getStage() != ICompetition.Stage.ENDED) {
                            p.sendMessage(Component.text("已經有一場比賽正在進行中...").color(NamedTextColor.RED));
                            p.playSound(p, Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f);
                            return;
                        }
                        p.playSound(p, Sound.BLOCK_WOODEN_BUTTON_CLICK_ON, 1f, 1f);
                        CompetitionGUI.COMPETITION_START_GUI.openTo(p);
                    } else if (GUIButton.isSameButton(item, GUIButton.END_COMPETITION)) {
                        boolean b = Competitions.getCurrentlyCompetition() == null || Competitions.getCurrentlyCompetition().getStage() == ICompetition.Stage.ENDED;
                        p.playSound(p, b ? Sound.ENTITY_ENDERMAN_TELEPORT : Sound.BLOCK_WOODEN_BUTTON_CLICK_ON, 1f, 1f);
                        Competitions.end(p);
                        return;
                    } else if (GUIButton.isSameButton(item, GUIButton.COMPETITION_SETTINGS)) {
                        p.playSound(p, Sound.BLOCK_WOODEN_BUTTON_CLICK_ON, 1f, 1f);
                        CompetitionGUI.COMPETITION_SETTINGS_GUI.openTo(p);
                        return;
                    }
                    CompetitionGUI.GUI_MAP.get(p).onClick(e);
                }
            }
        }
    }

    @EventHandler
    public void onClose(@NotNull InventoryCloseEvent e) {
        if (e.getPlayer() instanceof Player p) {
            CompetitionGUI.GUI_MAP.remove(p);
        }
    }

    @EventHandler
    public void onLeave(@NotNull PlayerQuitEvent e) {
        CompetitionGUI.GUI_MAP.remove(e.getPlayer());
        if (Competitions.containPlayer(e.getPlayer())) {
            CompetitionGUI.COMPETITION_INFO_GUI.update();
            PlayerListGUI.updateGUI();
        }
    }
}
