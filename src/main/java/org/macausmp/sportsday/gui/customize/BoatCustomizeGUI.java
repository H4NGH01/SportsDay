package org.macausmp.sportsday.gui.customize;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.PlayerCustomize;
import org.macausmp.sportsday.SportsDay;
import org.macausmp.sportsday.gui.AbstractGUI;
import org.macausmp.sportsday.gui.GUIButton;
import org.macausmp.sportsday.util.Translation;

import java.util.ArrayList;
import java.util.List;

public class BoatCustomizeGUI extends AbstractGUI {
    private final Player player;

    public BoatCustomizeGUI(Player player) {
        super(18, Translation.translatable("gui.customize.boat.title"));
        this.player = player;
        for (int i = 0; i < 9; i++) {
            getInventory().setItem(i, GUIButton.BOARD);
        }
        getInventory().setItem(8, GUIButton.BACK);
        update();
    }

    @Override
    public void update() {
        getInventory().setItem(9, boat(Material.OAK_BOAT));
        getInventory().setItem(10, boat(Material.SPRUCE_BOAT));
        getInventory().setItem(11, boat(Material.BIRCH_BOAT));
        getInventory().setItem(12, boat(Material.JUNGLE_BOAT));
        getInventory().setItem(13, boat(Material.ACACIA_BOAT));
        getInventory().setItem(14, boat(Material.DARK_OAK_BOAT));
        getInventory().setItem(15, boat(Material.MANGROVE_BOAT));
        getInventory().setItem(16, boat(Material.CHERRY_BOAT));
        if (player == null) return;
        String boat = PlayerCustomize.getBoat(player);
        if (boat == null) return;
        for (int i = 9; i < 17; i++) {
            ItemStack boat2 = getInventory().getItem(i);
            if (boat2 != null && boat.equals(boat2.getType().name())) {
                List<Component> lore = new ArrayList<>();
                lore.add(Translation.translatable("gui.selected"));
                boat2.lore(lore);
                boat2.addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 0);
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
        if (isBoat(item.getType())) {
            PlayerCustomize.setBoat(player, item.getType());
            update();
        }
    }

    private @NotNull ItemStack boat(Material material) {
        ItemStack boat = new ItemStack(material);
        boat.editMeta(meta -> {
            List<Component> lore = new ArrayList<>();
            lore.add(Translation.translatable("gui.select"));
            meta.lore(lore);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            meta.getPersistentDataContainer().set(SportsDay.ITEM_ID, PersistentDataType.STRING, "boat");
        });
        return boat;
    }

    @Contract(pure = true)
    private boolean isBoat(@NotNull Material material) {
        return switch (material) {
            case ACACIA_BOAT, BIRCH_BOAT, CHERRY_BOAT, DARK_OAK_BOAT, JUNGLE_BOAT, MANGROVE_BOAT, OAK_BOAT, SPRUCE_BOAT ->
                    true;
            default -> false;
        };
    }
}
