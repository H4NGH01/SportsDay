package org.macausmp.sportsday.gui.customize;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.gui.AbstractGUI;
import org.macausmp.sportsday.gui.GUIButton;
import org.macausmp.sportsday.util.Translation;

public class CustomizeMenuGUI extends AbstractGUI {
    public CustomizeMenuGUI() {
        super(27, Translation.translatable("gui.customize.title"));
        getInventory().setItem(10, GUIButton.CLOTHING);
        getInventory().setItem(12, GUIButton.BOAT);
        getInventory().setItem(14, GUIButton.WEAPON);
        getInventory().setItem(16, GUIButton.MUSICKIT);
    }

    @Override
    public void update() {
    }

    @Override
    public void onClick(InventoryClickEvent event, Player player, @NotNull ItemStack item) {
        if (GUIButton.isSameButton(item, GUIButton.CLOTHING)) {
            new ClothingCustomizeGUI(player).openTo(player);
        } else if (GUIButton.isSameButton(item, GUIButton.BOAT)) {
            new BoatCustomizeGUI(player).openTo(player);
        } else if (GUIButton.isSameButton(item, GUIButton.WEAPON)) {
            new WeaponCustomizeGUI(player).openTo(player);
        }
    }
}
