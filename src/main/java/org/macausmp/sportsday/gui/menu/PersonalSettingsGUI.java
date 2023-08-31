package org.macausmp.sportsday.gui.menu;

import net.kyori.adventure.text.Component;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.macausmp.sportsday.gui.AbstractGUI;
import org.macausmp.sportsday.gui.GUIButton;
import org.macausmp.sportsday.util.ItemUtil;

public class PersonalSettingsGUI extends AbstractGUI {
    private final Player player;

    public PersonalSettingsGUI(Player player) {
        super(54, Component.translatable("gui.menu.personal_settings.title"));
        for (int i = 0; i < 9; i++) {
            getInventory().setItem(i, GUIButton.BOARD);
        }
        getInventory().setItem(8, GUIButton.BACK);
        this.player = player;
        update();
    }

    @Override
    public void update() {
        if (player == null) return;
    }

    @Override
    public void onClick(InventoryClickEvent e, Player p, ItemStack item) {
        if (ItemUtil.isSameItem(item, GUIButton.BACK)) {
            p.openInventory(new MenuGUI().getInventory());
            p.playSound(p, Sound.BLOCK_WOODEN_BUTTON_CLICK_ON, 1f, 1f);
        }
    }
}
