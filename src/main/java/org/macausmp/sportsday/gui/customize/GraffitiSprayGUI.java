package org.macausmp.sportsday.gui.customize;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.SportsDay;
import org.macausmp.sportsday.gui.AbstractGUI;
import org.macausmp.sportsday.gui.GUIButton;
import org.macausmp.sportsday.util.CustomizeGraffitiSpray;
import org.macausmp.sportsday.util.PlayerCustomize;
import org.macausmp.sportsday.util.TextUtil;

import java.util.ArrayList;
import java.util.List;

public class GraffitiSprayGUI extends AbstractGUI {
    private static final NamespacedKey GRAFFITI_SPRAY = NamespacedKey.fromString("graffiti_spray", PLUGIN);
    private static final int START_INDEX = 10;
    private final Player player;

    public GraffitiSprayGUI(Player player) {
        super(54, Component.translatable("gui.customize.graffiti_spray.title"));
        this.player = player;
        for (int i = 0; i < 9; i++) {
            getInventory().setItem(i, GUIButton.BOARD);
        }
        getInventory().setItem(8, GUIButton.BACK);
        getInventory().setItem(9, reset());
        update();
    }

    @Override
    public void update() {
        for (int i = 0; i < CustomizeGraffitiSpray.values().length; i++) {
            getInventory().setItem(i + START_INDEX, graffiti(CustomizeGraffitiSpray.values()[i]));
        }
        if (player == null) return;
        CustomizeGraffitiSpray graffiti = PlayerCustomize.getGraffitiSpray(player);
        if (graffiti == null) return;
        for (int i = START_INDEX; i < getInventory().getSize(); i++) {
            ItemStack stack = getInventory().getItem(i);
            if (stack == null) break;
            String key = stack.getItemMeta().getPersistentDataContainer().get(GRAFFITI_SPRAY, PersistentDataType.STRING);
            if (key != null && key.equals(graffiti.name())) {
                List<Component> lore = new ArrayList<>();
                lore.add(TextUtil.text(Component.translatable("gui.selected")));
                stack.lore(lore);
                stack.addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 0);
                break;
            }
        }
    }

    @Override
    public void onClick(InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        if (GUIButton.isSameButton(item, GUIButton.BACK)) {
            p.openInventory(new CustomizeMenuGUI().getInventory());
            p.playSound(p, Sound.BLOCK_WOODEN_BUTTON_CLICK_ON, 1f, 1f);
            return;
        }
        if (GUIButton.isSameButton(item, "graffiti")) {
            PlayerCustomize.setGraffitiSpray(p, CustomizeGraffitiSpray.values()[e.getSlot() - START_INDEX]);
        } else if (GUIButton.isSameButton(item, reset())) {
            PlayerCustomize.setGraffitiSpray(p, null);
        }
        p.playSound(p, Sound.ENTITY_ARROW_HIT_PLAYER, 1f, 1f);
        update();
    }

    private @NotNull ItemStack graffiti(CustomizeGraffitiSpray graffiti) {
        ItemStack stack = new ItemStack(Material.PAINTING);
        stack.editMeta(meta -> {
            meta.displayName(graffiti.getName());
            List<Component> lore = new ArrayList<>();
            lore.add(TextUtil.text(Component.translatable("gui.select")));
            meta.lore(lore);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            meta.getPersistentDataContainer().set(SportsDay.ITEM_ID, PersistentDataType.STRING, "graffiti");
            meta.getPersistentDataContainer().set(GRAFFITI_SPRAY, PersistentDataType.STRING, graffiti.name());
        });
        return stack;
    }

    private @NotNull ItemStack reset() {
        ItemStack stack = new ItemStack(Material.BARRIER);
        stack.editMeta(meta -> {
            meta.displayName(TextUtil.text(Component.translatable("gui.customize.graffiti_spray.reset")));
            meta.getPersistentDataContainer().set(SportsDay.ITEM_ID, PersistentDataType.STRING, "reset");
        });
        return stack;
    }
}
