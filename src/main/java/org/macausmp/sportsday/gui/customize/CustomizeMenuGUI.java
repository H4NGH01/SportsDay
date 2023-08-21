package org.macausmp.sportsday.gui.customize;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.gui.AbstractGUI;
import org.macausmp.sportsday.gui.GUIButton;

public class CustomizeMenuGUI extends AbstractGUI {
    public CustomizeMenuGUI() {
        super(27, Component.translatable("gui.customize.title"));
        getInventory().setItem(10, GUIButton.CLOTHING);
        getInventory().setItem(12, GUIButton.BOAT);
        getInventory().setItem(14, GUIButton.WEAPON);
        getInventory().setItem(16, GUIButton.MUSICKIT);
    }

    @Override
    public void update() {
    }

    @Override
    public void onClick(InventoryClickEvent e, Player p, @NotNull ItemStack item) {
        if (GUIButton.isSameButton(item, GUIButton.CLOTHING)) {
            p.openInventory(new ClothingCustomizeGUI(p).getInventory());
        } else if (GUIButton.isSameButton(item, GUIButton.BOAT)) {
            p.openInventory(new BoatCustomizeGUI(p).getInventory());
        } else if (GUIButton.isSameButton(item, GUIButton.WEAPON)) {
            p.openInventory(new WeaponCustomizeGUI(p).getInventory());
        }
    }
}
