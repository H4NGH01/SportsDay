package org.macausmp.sportsday.gui;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.competition.Competitions;
import org.macausmp.sportsday.competition.Stage;
import org.macausmp.sportsday.gui.competition.PlayerListGUI;

public final class GUIListener implements Listener {
    @EventHandler
    public void onClick(@NotNull InventoryClickEvent e) {
        if (e.getWhoClicked() instanceof Player p) {
            if (!p.isOp()) e.setCancelled(true);
            if (e.getInventory().getHolder() instanceof AbstractGUI gui) {
                e.setCancelled(true);
                ItemStack item = e.getCurrentItem();
                if (item != null && GUIButton.isButton(item) && e.getClickedInventory() != null && e.getClickedInventory().getType() != InventoryType.PLAYER) {
                    if (GUIButton.isSameButton(item, GUIButton.COMPETITION_INFO)) {
                        p.playSound(p, Sound.BLOCK_WOODEN_BUTTON_CLICK_ON, 1f, 1f);
                        p.openInventory(GUIManager.COMPETITION_INFO_GUI.getInventory());
                        return;
                    } else if (GUIButton.isSameButton(item, GUIButton.PLAYER_LIST)) {
                        p.playSound(p, Sound.ITEM_BOOK_PAGE_TURN, 1f, 1f);
                        p.openInventory(new PlayerListGUI().getInventory());
                        return;
                    } else if (GUIButton.isSameButton(item, GUIButton.START_COMPETITION)) {
                        if (Competitions.getCurrentlyEvent() != null && Competitions.getCurrentlyEvent().getStage() != Stage.ENDED) {
                            p.sendMessage(Component.translatable("competition.already_in_progress").color(NamedTextColor.RED));
                            p.playSound(p, Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f);
                            return;
                        }
                        p.playSound(p, Sound.BLOCK_WOODEN_BUTTON_CLICK_ON, 1f, 1f);
                        p.openInventory(GUIManager.COMPETITION_START_GUI.getInventory());
                    } else if (GUIButton.isSameButton(item, GUIButton.END_COMPETITION)) {
                        boolean b = Competitions.getCurrentlyEvent() == null || Competitions.getCurrentlyEvent().getStage() == Stage.ENDED;
                        p.playSound(p, b ? Sound.ENTITY_ENDERMAN_TELEPORT : Sound.BLOCK_WOODEN_BUTTON_CLICK_ON, 1f, 1f);
                        Competitions.forceEnd(p);
                        return;
                    } else if (GUIButton.isSameButton(item, GUIButton.COMPETITION_SETTINGS)) {
                        p.playSound(p, Sound.BLOCK_WOODEN_BUTTON_CLICK_ON, 1f, 1f);
                        p.openInventory(GUIManager.COMPETITION_SETTINGS_GUI.getInventory());
                        return;
                    }
                    gui.onClick(e, p, item);
                }
            }
        }
    }
}
