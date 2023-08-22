package org.macausmp.sportsday.gui.customize;

import net.kyori.adventure.text.Component;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.gui.AbstractGUI;
import org.macausmp.sportsday.gui.GUIButton;

public class CustomizeMenuGUI extends AbstractGUI {
    public CustomizeMenuGUI() {
        super(45, Component.translatable("gui.customize.title"));
        getInventory().setItem(10, GUIButton.CLOTHING);
        getInventory().setItem(12, GUIButton.BOAT_TYPE);
        getInventory().setItem(14, GUIButton.WEAPON_SKIN);
        getInventory().setItem(16, GUIButton.MUSICKIT);
        getInventory().setItem(28, GUIButton.PROJECTILE_TRAIL);
        getInventory().setItem(31, GUIButton.WALKING_EFFECT);
        getInventory().setItem(34, GUIButton.GRAFFITI_SPRAY);
    }

    @Override
    public void update() {
    }

    @Override
    public void onClick(InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        p.playSound(p, Sound.BLOCK_WOODEN_BUTTON_CLICK_ON, 1f, 1f);
        if (GUIButton.isSameButton(item, GUIButton.CLOTHING)) {
            p.openInventory(new ClothingCustomizeGUI(p).getInventory());
        } else if (GUIButton.isSameButton(item, GUIButton.BOAT_TYPE)) {
            p.openInventory(new BoatTypeGUI(p).getInventory());
        } else if (GUIButton.isSameButton(item, GUIButton.WEAPON_SKIN)) {
            p.openInventory(new WeaponSkinGUI(p).getInventory());
        } else if (GUIButton.isSameButton(item, GUIButton.MUSICKIT)) {
            p.openInventory(new MusickitGUI(p).getInventory());
        } else if (GUIButton.isSameButton(item, GUIButton.PROJECTILE_TRAIL)) {
            p.openInventory(new ProjectileTrailGUI(p).getInventory());
        } else if (GUIButton.isSameButton(item, GUIButton.WALKING_EFFECT)) {
            p.openInventory(new WalkingEffectGUI(p).getInventory());
        } else if (GUIButton.isSameButton(item, GUIButton.GRAFFITI_SPRAY)) {
            p.openInventory(new GraffitiSprayGUI(p).getInventory());
        }
    }
}
