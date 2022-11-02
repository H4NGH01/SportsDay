package org.macausmp.sportsday.gui;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.SportsDay;
import org.macausmp.sportsday.competition.Competitions;
import org.macausmp.sportsday.competition.ICompetition;

import java.util.Objects;

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
                        CompetitionGUI.PLAYER_LIST_GUI.openTo(player);
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
                    PersistentDataContainer container = item.getItemMeta().getPersistentDataContainer();
                    if (CompetitionGUI.GUI_MAP.get(player) == CompetitionGUI.COMPETITION_START_GUI) {
                        if (container.has(GUIButton.ITEM_ID, PersistentDataType.STRING) && Objects.equals(container.get(GUIButton.ITEM_ID, PersistentDataType.STRING), "competition")) {
                            for (ICompetition competition : Competitions.COMPETITIONS) {
                                if (competition.getID().equals(container.get(GUIButton.COMPETITION_ID, PersistentDataType.STRING))) {
                                    if (!competition.isEnable()) {
                                        player.sendMessage(Component.text("該比賽項目已被禁用").color(NamedTextColor.RED));
                                        return;
                                    }
                                    if (Competitions.getPlayerDataList().size() >= competition.getLeastPlayersRequired()) {
                                        player.sendMessage(Component.text("開始新一場比賽中..."));
                                        Competitions.setCurrentlyCompetition(competition);
                                        competition.setup();
                                    } else {
                                        player.sendMessage(Component.text("參賽選手人數不足，無法開始比賽，最少需要" + competition.getLeastPlayersRequired() + "人開始比賽").color(NamedTextColor.RED));
                                    }
                                    return;
                                }
                            }
                        }
                        return;
                    }
                    if (CompetitionGUI.GUI_MAP.get(player) == CompetitionGUI.COMPETITION_SETTINGS_GUI) {
                        if (container.has(GUIButton.ITEM_ID, PersistentDataType.STRING) && Objects.equals(container.get(GUIButton.ITEM_ID, PersistentDataType.STRING), "enable_switch")) {
                            for (ICompetition competition : Competitions.COMPETITIONS) {
                                if (competition.getID().equals(container.get(GUIButton.COMPETITION_ID, PersistentDataType.STRING))) {
                                    SportsDay.getInstance().getConfig().set(competition.getID() + ".enable", !competition.isEnable());
                                    SportsDay.getInstance().saveConfig();
                                    CompetitionGUI.COMPETITION_SETTINGS_GUI.update();
                                    player.playSound(player, competition.isEnable() ? Sound.ENTITY_ARROW_HIT_PLAYER : Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f);
                                    return;
                                }
                            }
                        }
                    }
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
