package org.macausmp.sportsday.gui;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.competition.Competitions;

public class GUIListener implements Listener {
    @EventHandler
    public void onClick(@NotNull InventoryClickEvent e) {
        if (e.getWhoClicked() instanceof Player player) {
            if (CompetitionGUI.GUI_MAP.containsKey(player)) {
                e.setCancelled(true);
                ItemStack item = e.getCurrentItem();
                if (item != null) {
                    if (GUIButton.isSameButton(item, GUIButton.COMPETITION_INFO)) {
                        if (Competitions.getCurrentlyCompetition() != null) {
                            CompetitionGUI.COMPETITION_INFO_GUI.openTo(player);
                            return;
                        }
                        player.sendMessage(Component.text("現在沒有比賽進行中").color(NamedTextColor.RED));
                    } else if (GUIButton.isSameButton(item, GUIButton.PLAYER_LIST)) {
                        new PlayerListGUI().openTo(player);
                    } else if (GUIButton.isSameButton(item, GUIButton.START_COMPETITION)) {
                        if (Competitions.getCurrentlyCompetition() != null) {
                            player.sendMessage(Component.text("已經有一場比賽正在進行中...").color(NamedTextColor.RED));
                            return;
                        }
                        CompetitionGUI.COMPETITION_START_GUI.openTo(player);
                    } else if (GUIButton.isSameButton(item, GUIButton.END_COMPETITION)) {
                        if (Competitions.getCurrentlyCompetition() != null) {
                            Competitions.getCurrentlyCompetition().end(true);
                            player.sendMessage(Component.text("已強制結束一場比賽"));
                        } else {
                            player.sendMessage(Component.text("現在沒有比賽進行中").color(NamedTextColor.RED));
                        }
                        return;
                    } else if (GUIButton.isSameButton(item, GUIButton.COMPETITION_SETTINGS)) {
                        CompetitionGUI.COMPETITION_SETTINGS_GUI.openTo(player);
                        return;
                    }
                    CompetitionGUI.GUI_MAP.get(player).onClick(e);
                }
            }
        }
    }

    @EventHandler
    public void onClose(@NotNull InventoryCloseEvent e) {
        if (e.getPlayer() instanceof Player player) {
            CompetitionGUI.GUI_MAP.remove(player);
        }
    }

    @EventHandler
    public void onLeave(@NotNull PlayerQuitEvent e) {
        CompetitionGUI.GUI_MAP.remove(e.getPlayer());
    }
}
