package org.macausmp.sportsday.gui.customize;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.PlayerCustomize;
import org.macausmp.sportsday.SportsDay;
import org.macausmp.sportsday.gui.AbstractGUI;
import org.macausmp.sportsday.gui.GUIButton;
import org.macausmp.sportsday.util.Translation;

import java.util.ArrayList;
import java.util.List;

public class WeaponCustomizeGUI extends AbstractGUI {
    private final Player player;

    public WeaponCustomizeGUI(Player player) {
        super(18, Translation.translatable("gui.customize.weapon.title"));
        this.player = player;
        for (int i = 0; i < 9; i++) {
            getInventory().setItem(i, GUIButton.BOARD);
        }
        getInventory().setItem(8, GUIButton.BACK);
        update();
    }

    @Override
    public void update() {
        getInventory().setItem(9, weapon(Material.BLAZE_ROD));
        getInventory().setItem(10, weapon(Material.BONE));
        getInventory().setItem(11, weapon(Material.SHEARS));
        getInventory().setItem(12, weapon(Material.BAMBOO));
        getInventory().setItem(13, weapon(Material.DEAD_BUSH));
        getInventory().setItem(14, weapon(Material.COD));
        if (player == null) return;
        String weapon = PlayerCustomize.getWeapon(player);
        if (weapon == null) return;
        for (int i = 9; i < 17; i++) {
            ItemStack weapon2 = getInventory().getItem(i);
            if (weapon2 != null && weapon.equals(weapon2.getType().name())) {
                List<Component> lore = new ArrayList<>();
                lore.add(Translation.translatable("gui.selected"));
                weapon2.lore(lore);
                weapon2.addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 0);
                break;
            }
        }
    }

    @Override
    public void onClick(InventoryClickEvent event, Player player, @NotNull ItemStack item) {
        if (GUIButton.isSameButton(item, GUIButton.BACK)) {
            new CustomizeMenuGUI().openTo(player);
            return;
        }
        if (GUIButton.isSameButton(item, "weapon")) {
            PlayerCustomize.setWeapon(player, item.getType());
            update();
        }
    }

    private @NotNull ItemStack weapon(Material material) {
        ItemStack weapon = new ItemStack(material);
        weapon.editMeta(meta -> {
            List<Component> lore = new ArrayList<>();
            lore.add(Translation.translatable("gui.select"));
            meta.lore(lore);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            meta.getPersistentDataContainer().set(SportsDay.ITEM_ID, PersistentDataType.STRING, "weapon");
        });
        return weapon;
    }
}
