package org.macausmp.sportsday.gui;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.PlayerData;
import org.macausmp.sportsday.SportsDay;
import org.macausmp.sportsday.competition.Competitions;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class PlayerProfileGUI extends AbstractGUI {
    private final PlayerData data;

    public PlayerProfileGUI(@NotNull PlayerData data) {
        super(54, Component.translatable("%s的個人檔案").args(Component.text(data.getName())));
        this.data = data;
        for (int i = 0; i < 9; i++) {
            getInventory().setItem(i + 9, GUIButton.BOARD);
        }
        getInventory().setItem(0, GUIButton.COMPETITION_INFO);
        getInventory().setItem(1, GUIButton.PLAYER_LIST);
        getInventory().setItem(2, GUIButton.START_COMPETITION);
        getInventory().setItem(3, GUIButton.END_COMPETITION);
        getInventory().setItem(4, GUIButton.COMPETITION_SETTINGS);
        getInventory().setItem(5, GUIButton.VERSION);
        getInventory().setItem(18, icon(data.getUUID()));
        getInventory().setItem(19, kick(data));
    }

    @Override
    public void update() {
    }

    private @NotNull ItemStack icon(UUID uuid) {
        ItemStack icon = new ItemStack(Material.PLAYER_HEAD);
        icon.editMeta(SkullMeta.class, meta -> {
            OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
            Component online = player.isOnline() ? Component.text("在線").color(NamedTextColor.YELLOW) : Component.text("離線").color(NamedTextColor.RED);
            meta.displayName(Component.translatable(Objects.requireNonNull(player.getName()) + " (%s)").args(online).decoration(TextDecoration.ITALIC, false));
            meta.setOwningPlayer(player);
            List<Component> lore = new ArrayList<>();
            lore.add(Component.translatable("參賽號碼: %s").args(Component.text(Competitions.getPlayerData(uuid).getNumber())).decoration(TextDecoration.ITALIC, false).color(NamedTextColor.YELLOW));
            lore.add(Component.translatable("總體得分: %s").args(Component.text(Competitions.getPlayerData(uuid).getScore())).decoration(TextDecoration.ITALIC, false).color(NamedTextColor.YELLOW));
            meta.lore(lore);
        });
        return icon;
    }

    private @NotNull ItemStack kick(PlayerData data) {
        ItemStack stack = new ItemStack(Material.RED_CONCRETE);
        stack.editMeta(meta -> {
            meta.displayName(Component.translatable("將%s從參賽選手名單中移除").args(Component.text(data.getName())).decoration(TextDecoration.ITALIC, false).color(NamedTextColor.RED));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.text("點擊移除").decoration(TextDecoration.ITALIC, false).color(NamedTextColor.RED));
            meta.lore(lore);
            meta.getPersistentDataContainer().set(SportsDay.ITEM_ID, PersistentDataType.STRING, "kick");
        });
        return stack;
    }

    @Override
    public void onClick(@NotNull InventoryClickEvent event) {
        Player p = (Player) event.getWhoClicked();
        ItemStack item = Objects.requireNonNull(event.getCurrentItem());
        if (GUIButton.isSameButton(item, kick(data))) {
            p.sendMessage(Competitions.leave(p) ? Component.translatable("已將%s從參賽選手名單中移除").args(Component.text(p.getName())).color(NamedTextColor.GREEN) : Component.translatable( "%s不在參賽選手名單上").args(Component.text(p.getName())).color(NamedTextColor.RED));
            p.closeInventory();
        }
    }
}
