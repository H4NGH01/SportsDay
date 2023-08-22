package org.macausmp.sportsday.gui.customize;

import net.kyori.adventure.text.Component;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ArmorMeta;
import org.bukkit.inventory.meta.ColorableArmorMeta;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.macausmp.sportsday.SportsDay;
import org.macausmp.sportsday.gui.AbstractGUI;
import org.macausmp.sportsday.gui.GUIButton;
import org.macausmp.sportsday.util.PlayerCustomize;
import org.macausmp.sportsday.util.TextUtil;

import java.util.ArrayList;
import java.util.List;

public class ClothingCustomizeGUI extends AbstractGUI {
    private static final String[] MATERIAL = {"LEATHER", "CHAINMAIL", "IRON", "GOLDEN", "DIAMOND", "NETHERITE"};
    private static final String[] ARMOR = {"HELMET", "CHESTPLATE", "LEGGINGS", "BOOTS"};

    private final Player player;

    public ClothingCustomizeGUI(Player player) {
        super(45, Component.translatable("gui.customize.clothing.title"));
        this.player = player;
        for (int i = 0; i < 9; i++) {
            getInventory().setItem(i, GUIButton.BOARD);
        }
        getInventory().setItem(8, GUIButton.BACK);
        getInventory().setItem(17, reset("gui.customize.clothing.head"));
        getInventory().setItem(26, reset("gui.customize.clothing.chest"));
        getInventory().setItem(35, reset("gui.customize.clothing.legs"));
        getInventory().setItem(44, reset("gui.customize.clothing.feet"));
        update();
    }

    @Override
    public void update() {
        if (player == null) return;
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 6; j++) {
                getInventory().setItem(11 + i * 9 + j, select(Material.getMaterial(MATERIAL[j] + "_" + ARMOR[i])));
            }
        }
        getInventory().setItem(9, present(EquipmentSlot.HEAD));
        getInventory().setItem(18, present(EquipmentSlot.CHEST));
        getInventory().setItem(27, present(EquipmentSlot.LEGS));
        getInventory().setItem(36, present(EquipmentSlot.FEET));
    }

    @Override
    public void onClick(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        if (GUIButton.isSameButton(item, GUIButton.BACK)) {
            p.openInventory(new CustomizeMenuGUI().getInventory());
            p.playSound(p, Sound.BLOCK_WOODEN_BUTTON_CLICK_ON, 1f, 1f);
            return;
        }
        if (item.getType().getEquipmentSlot().isArmor()) {
            if (GUIButton.isSameButton(item, "select_cloth")) {
                if (e.isLeftClick()) {
                    PlayerCustomize.setClothItem(p, item.getType());
                } else if (e.isRightClick() && item.getItemMeta() instanceof ColorableArmorMeta) {
                    p.openInventory(new ClothingColorGUI(p, item.getType().getEquipmentSlot()).getInventory());
                    return;
                }
            } else if (GUIButton.isSameButton(item, "cloth") && e.isRightClick()) {
                p.openInventory(new ClothingTrimGUI(p, item.getType().getEquipmentSlot()).getInventory());
            } else {
                return;
            }
        } else {
            switch (e.getSlot()) {
                case 17 -> PlayerCustomize.resetCloth(p, EquipmentSlot.HEAD);
                case 26 -> PlayerCustomize.resetCloth(p, EquipmentSlot.CHEST);
                case 35 -> PlayerCustomize.resetCloth(p, EquipmentSlot.LEGS);
                case 44 -> PlayerCustomize.resetCloth(p, EquipmentSlot.FEET);
            }
        }
        p.playSound(p, Sound.BLOCK_WOODEN_BUTTON_CLICK_ON, 1f, 1f);
        update();
        PlayerCustomize.suitUp(p);
    }

    private @Nullable ItemStack present(@NotNull EquipmentSlot slot) {
        ItemStack cloth = PlayerCustomize.getClothItem(player, slot);
        if (cloth == null) return null;
        cloth.editMeta(ArmorMeta.class, meta -> {
            List<Component> lore = new ArrayList<>();
            lore.add(TextUtil.text(Component.translatable("gui.customize.clothing.trim.lore")));
            meta.lore(lore);
            if (PlayerCustomize.hasClothTrim(player, slot)) meta.setTrim(new ArmorTrim(PlayerCustomize.getClothTrimMaterial(player, slot), PlayerCustomize.getClothTrimPattern(player, slot)));
            meta.getPersistentDataContainer().set(SportsDay.ITEM_ID, PersistentDataType.STRING, "cloth");
        });
        if (cloth.getItemMeta() instanceof ColorableArmorMeta) {
            cloth.editMeta(ColorableArmorMeta.class, meta -> {
                Color color = PlayerCustomize.getClothColor(player, slot);
                meta.setColor(color);
            });
        }
        return cloth;
    }

    private @NotNull ItemStack select(Material material) {
        ItemStack cloth = new ItemStack(material);
        cloth.editMeta(ArmorMeta.class, meta -> {
            List<Component> lore = new ArrayList<>();
            lore.add(TextUtil.text(Component.translatable("gui.select")));
            if (cloth.getItemMeta() instanceof ColorableArmorMeta) lore.add(TextUtil.text(Component.translatable("gui.customize.clothing.color.lore")));
            meta.lore(lore);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            meta.getPersistentDataContainer().set(SportsDay.ITEM_ID, PersistentDataType.STRING, "select_cloth");
        });
        if (cloth.getItemMeta() instanceof ColorableArmorMeta) {
            cloth.editMeta(ColorableArmorMeta.class, meta -> {
                Color color = PlayerCustomize.getClothColor(player, material.getEquipmentSlot());
                meta.setColor(color);
            });
        }
        return cloth;
    }

    private @NotNull ItemStack reset(String slot) {
        ItemStack stack = new ItemStack(Material.BARRIER);
        stack.editMeta(meta -> {
            meta.displayName(TextUtil.text(Component.translatable("gui.customize.clothing.reset").args(Component.translatable(slot))));
            meta.getPersistentDataContainer().set(SportsDay.ITEM_ID, PersistentDataType.STRING, "reset");
        });
        return stack;
    }
}
