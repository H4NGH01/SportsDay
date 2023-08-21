package org.macausmp.sportsday.gui.customize;

import net.kyori.adventure.text.Component;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.util.PlayerCustomize;
import org.macausmp.sportsday.SportsDay;
import org.macausmp.sportsday.gui.AbstractGUI;
import org.macausmp.sportsday.gui.GUIButton;
import org.macausmp.sportsday.util.TextUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ClothingColorGUI extends AbstractGUI {
    private final Player player;
    private final EquipmentSlot slot;
    public ClothingColorGUI(@NotNull Player player, @NotNull EquipmentSlot slot) {
        super(27, Component.translatable("gui.customize.clothing.color.title"));
        this.player = player;
        this.slot = slot;
        for (int i = 0; i < 9; i++) {
            getInventory().setItem(i, GUIButton.BOARD);
        }
        getInventory().setItem(8, GUIButton.BACK);
        getInventory().setItem(26, reset());
        update();
    }

    @Override
    public void update() {
        for (int i = 0; i < DyeColor.values().length; i++) {
            getInventory().setItem(i + 9, dye(Material.getMaterial(DyeColor.values()[i].name() + "_DYE")));
        }
        if (player == null || slot == null) return;
        for (int i = 9; i < 25; i++) {
            ItemStack dye = getInventory().getItem(i);
            Color color = PlayerCustomize.getClothColor(player, slot);
            if (color == null || DyeColor.getByColor(color) == null) return;
            if (dye != null && dye.getType().name().equals(Objects.requireNonNull(DyeColor.getByColor(color)).name() + "_DYE")) {
                List<Component> lore = new ArrayList<>();
                lore.add(TextUtil.convert(Component.translatable("gui.selected")));
                dye.lore(lore);
                dye.addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 0);
                break;
            }
        }
    }

    @Override
    public void onClick(InventoryClickEvent e, Player p, ItemStack item) {
        if (GUIButton.isSameButton(item, GUIButton.BACK)) {
            p.openInventory(new ClothingCustomizeGUI(p).getInventory());
            return;
        }
        String s = item.getType().name();
        if (s.endsWith("_DYE")) {
            PlayerCustomize.setClothColor(p, slot, DyeColor.valueOf(s.substring(0, s.length() - 4)).getColor());
        } else if (GUIButton.isSameButton(item, reset())) {
            PlayerCustomize.setClothColor(p, slot, null);
        }
        update();
        PlayerCustomize.suitUp(p);
    }

    private @NotNull ItemStack dye(Material material) {
        ItemStack stack = new ItemStack(material);
        stack.editMeta(meta -> {
            List<Component> lore = new ArrayList<>();
            lore.add(TextUtil.convert(Component.translatable("gui.select")));
            meta.lore(lore);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            meta.getPersistentDataContainer().set(SportsDay.ITEM_ID, PersistentDataType.STRING, "dye");
        });
        return stack;
    }

    private @NotNull ItemStack reset() {
        ItemStack stack = new ItemStack(Material.BARRIER);
        stack.editMeta(meta -> {
            meta.displayName(TextUtil.convert(Component.translatable("gui.customize.clothing.reset").args(Component.translatable("gui.customize.clothing.reset_color"))));
            meta.getPersistentDataContainer().set(SportsDay.ITEM_ID, PersistentDataType.STRING, "reset");
        });
        return stack;
    }
}
