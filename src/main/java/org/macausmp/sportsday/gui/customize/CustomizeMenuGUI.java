package org.macausmp.sportsday.gui.customize;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
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
    public void onClick(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        p.playSound(Sound.sound(Key.key("minecraft:ui.button.click"), Sound.Source.MASTER, 1f, 1f));
        switch (e.getSlot()) {
            case 10 -> p.openInventory(new ClothingCustomizeGUI(p).getInventory());
            case 12 -> p.openInventory(new BoatTypeGUI(p).getInventory());
            case 14 -> p.openInventory(new WeaponSkinGUI(p).getInventory());
            case 16 -> p.openInventory(new MusickitGUI(p).getInventory());
            case 28 -> p.openInventory(new ProjectileTrailGUI(p).getInventory());
            case 31 -> p.openInventory(new WalkingEffectGUI(p).getInventory());
            case 34 -> p.openInventory(new GraffitiSprayGUI(p).getInventory());
        }
    }
}
