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
import org.macausmp.sportsday.util.CustomizeMusickit;
import org.macausmp.sportsday.util.PlayerCustomize;
import org.macausmp.sportsday.util.TextUtil;

import java.util.ArrayList;
import java.util.List;

public class MusickitGUI extends AbstractGUI {
    private static final NamespacedKey MUSICKIT = NamespacedKey.fromString("musickit", PLUGIN);
    private static final int START_INDEX = 10;
    private final Player player;

    public MusickitGUI(Player player) {
        super(54, Component.translatable("gui.customize.musickit.title"));
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
        for (int i = 0; i < CustomizeMusickit.values().length; i++) {
            getInventory().setItem(i + START_INDEX, musickit(CustomizeMusickit.values()[i]));
        }
        if (player == null) return;
        CustomizeMusickit musickit = PlayerCustomize.getMusickit(player);
        if (musickit == null) return;
        for (int i = START_INDEX; i < getInventory().getSize(); i++) {
            ItemStack stack = getInventory().getItem(i);
            if (stack == null) break;
            String key = stack.getItemMeta().getPersistentDataContainer().get(MUSICKIT, PersistentDataType.STRING);
            if (key != null && key.equals(musickit.name())) {
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
        if (GUIButton.isSameButton(item, "musickit")) {
            PlayerCustomize.setMusickit(p, CustomizeMusickit.values()[e.getSlot() - START_INDEX]);
        } else if (GUIButton.isSameButton(item, reset())) {
            PlayerCustomize.setMusickit(p, null);
        }
        p.playSound(p, Sound.ENTITY_ARROW_HIT_PLAYER, 1f, 1f);
        update();
    }

    private @NotNull ItemStack musickit(CustomizeMusickit musickit) {
        ItemStack stack = new ItemStack(Material.JUKEBOX);
        stack.editMeta(meta -> {
            meta.displayName(musickit.getName());
            List<Component> lore = new ArrayList<>();
            lore.add(TextUtil.text(Component.translatable("gui.select")));
            meta.lore(lore);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            meta.getPersistentDataContainer().set(SportsDay.ITEM_ID, PersistentDataType.STRING, "musickit");
            meta.getPersistentDataContainer().set(MUSICKIT, PersistentDataType.STRING, musickit.name());
        });
        return stack;
    }

    private @NotNull ItemStack reset() {
        ItemStack stack = new ItemStack(Material.BARRIER);
        stack.editMeta(meta -> {
            meta.displayName(TextUtil.text(Component.translatable("gui.customize.musickit.reset")));
            meta.getPersistentDataContainer().set(SportsDay.ITEM_ID, PersistentDataType.STRING, "reset");
        });
        return stack;
    }
}
