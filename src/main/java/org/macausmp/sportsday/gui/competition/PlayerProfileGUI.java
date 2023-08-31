package org.macausmp.sportsday.gui.competition;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.competition.Competitions;
import org.macausmp.sportsday.gui.AbstractGUI;
import org.macausmp.sportsday.gui.GUIButton;
import org.macausmp.sportsday.util.ItemUtil;
import org.macausmp.sportsday.util.PlayerData;
import org.macausmp.sportsday.util.TextUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class PlayerProfileGUI extends AbstractGUI {
    private final PlayerData data;

    public PlayerProfileGUI(@NotNull PlayerData data) {
        super(54, Component.translatable("gui.player_profile.title").args(Component.text(data.getName())));
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
        getInventory().setItem(19, unregister(data));
    }

    @Override
    public void update() {
    }

    @Override
    public void onClick(@NotNull InventoryClickEvent e, Player p, ItemStack item) {
        if (ItemUtil.isSameItem(item, unregister(data))) {
            p.sendMessage(Competitions.leave(data.getPlayer()) ? Component.translatable("player.leave").args(Component.text(data.getPlayer().getName())).color(NamedTextColor.GREEN) : Component.translatable( "player.unregistered").args(Component.text(data.getPlayer().getName())).color(NamedTextColor.RED));
            p.closeInventory();
        }
    }

    private @NotNull ItemStack icon(UUID uuid) {
        ItemStack icon = new ItemStack(Material.PLAYER_HEAD);
        icon.editMeta(SkullMeta.class, meta -> {
            OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
            Component online = Component.translatable(player.isOnline() ? "player.online" : "player.offline");
            meta.displayName(TextUtil.text(Component.translatable(Objects.requireNonNull(player.getName()) + " (%s)").args(online)));
            meta.setOwningPlayer(player);
            List<Component> lore = new ArrayList<>();
            lore.add(TextUtil.text(Component.translatable("player.number").args(Component.text(Competitions.getPlayerData(uuid).getNumber()))).color(NamedTextColor.YELLOW));
            lore.add(TextUtil.text(Component.translatable("player.score").args(Component.text(Competitions.getPlayerData(uuid).getScore()))).color(NamedTextColor.YELLOW));
            meta.lore(lore);
        });
        return icon;
    }

    private @NotNull ItemStack unregister(PlayerData data) {
        Component display = Component.translatable("gui.player.unregister").args(Component.text(data.getName())).color(NamedTextColor.RED);
        Component lore = Component.translatable("gui.player.unregister_lore");
        return ItemUtil.item(Material.RED_CONCRETE, "unregister", display, lore);
    }
}
