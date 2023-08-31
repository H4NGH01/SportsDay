package org.macausmp.sportsday.gui.competition;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.competition.Competitions;
import org.macausmp.sportsday.gui.AbstractGUI;
import org.macausmp.sportsday.gui.GUIButton;
import org.macausmp.sportsday.gui.IPageableGUI;
import org.macausmp.sportsday.util.ItemUtil;
import org.macausmp.sportsday.util.TextUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class PlayerListGUI extends AbstractGUI implements IPageableGUI {
    private static final List<PlayerListGUI> HANDLER = new ArrayList<>();
    private int page = 0;

    public PlayerListGUI() {
        super(54, Component.translatable("gui.player_list.title"));
        for (int i = 0; i < 9; i++) {
            getInventory().setItem(i + 9, GUIButton.BOARD);
        }
        getInventory().setItem(0, GUIButton.COMPETITION_INFO);
        getInventory().setItem(1, ItemUtil.addEffect(GUIButton.PLAYER_LIST));
        getInventory().setItem(2, GUIButton.START_COMPETITION);
        getInventory().setItem(3, GUIButton.END_COMPETITION);
        getInventory().setItem(4, GUIButton.COMPETITION_SETTINGS);
        getInventory().setItem(5, GUIButton.VERSION);
        getInventory().setItem(9, GUIButton.PREVIOUS_PAGE);
        getInventory().setItem(13, pages());
        getInventory().setItem(17, GUIButton.NEXT_PAGE);
        HANDLER.add(this);
    }

    @Override
    public void update() {
        getInventory().setItem(13, pages());
        for (int i = getStartSlot(); i < getEndSlot(); i++) {
            getInventory().setItem(i, null);
        }
        for (int i = 0; i < getSize(); i++) {
            if (i >= Competitions.getPlayerData().size()) break;
            getInventory().setItem(i + getStartSlot(), icon(Competitions.getPlayerData().get(i + getPage() * getSize()).getUUID()));
        }
    }

    @Override
    public void onClick(@NotNull InventoryClickEvent event, Player p, @NotNull ItemStack item) {
        if (event.getInventory().getHolder() instanceof PlayerListGUI gui) {
            if (ItemUtil.isSameItem(item, "player_icon")) {
                SkullMeta meta = (SkullMeta) item.getItemMeta();
                p.openInventory(new PlayerProfileGUI(Competitions.getPlayerData(Objects.requireNonNull(meta.getOwningPlayer()).getUniqueId())).getInventory());
                return;
            }
            if (ItemUtil.isSameItem(item, GUIButton.NEXT_PAGE)) {
                p.playSound(Sound.sound(Key.key("minecraft:item.book.page_turn"), Sound.Source.MASTER, 1f, 1f));
                gui.nextPage();
            } else if (ItemUtil.isSameItem(item, GUIButton.PREVIOUS_PAGE)) {
                p.playSound(Sound.sound(Key.key("minecraft:item.book.page_turn"), Sound.Source.MASTER, 1f, 1f));
                gui.previousPage();
            }
        }
    }

    public static void updateGUI() {
        for (PlayerListGUI gui : HANDLER) {
            gui.update();
        }
    }

    private @NotNull ItemStack pages() {
        return ItemUtil.item(Material.YELLOW_STAINED_GLASS_PANE, null, Component.translatable("book.pageIndicator").args(Component.text(getPage() + 1), Component.text(getMaxPage())));
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
            lore.add(Component.text(""));
            lore.add(TextUtil.text(Component.translatable("gui.player_profile.detail").args(Component.text(player.getName()))).color(NamedTextColor.YELLOW));
            meta.lore(lore);
            meta.getPersistentDataContainer().set(ItemUtil.ITEM_ID, PersistentDataType.STRING, "player_icon");
        });
        return icon;
    }

    public void nextPage() {
        if (getPage() < getMaxPage() - 1) page++;
        update();
    }

    public void previousPage() {
        if (getPage() > 0) page--;
        update();
    }

    @Override
    public int getPage() {
        return page;
    }

    @Override
    public int getMaxPage() throws ArithmeticException {
        return Competitions.getPlayerData().isEmpty() ? 1 : Competitions.getPlayerData().size() % getSize() == 0 ? Competitions.getPlayerData().size() / getSize() : Competitions.getPlayerData().size() / getSize() + 1;
    }

    @Override
    public int getStartSlot() {
        return 18;
    }

    @Override
    public int getEndSlot() {
        return 54;
    }
}
