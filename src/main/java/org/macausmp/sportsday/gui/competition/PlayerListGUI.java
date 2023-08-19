package org.macausmp.sportsday.gui.competition;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.SportsDay;
import org.macausmp.sportsday.competition.Competitions;
import org.macausmp.sportsday.gui.*;
import org.macausmp.sportsday.util.Translation;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class PlayerListGUI extends AbstractGUI implements IPageableGUI {
    private int page = 0;

    public PlayerListGUI() {
        super(54, Translation.translatable("gui.player_list.title"));
        for (int i = 0; i < 9; i++) {
            getInventory().setItem(i + 9, GUIButton.BOARD);
        }
        getInventory().setItem(0, GUIButton.COMPETITION_INFO);
        getInventory().setItem(1, GUIButton.PLAYER_LIST_SELECTED);
        getInventory().setItem(2, GUIButton.START_COMPETITION);
        getInventory().setItem(3, GUIButton.END_COMPETITION);
        getInventory().setItem(4, GUIButton.COMPETITION_SETTINGS);
        getInventory().setItem(5, GUIButton.VERSION);
        getInventory().setItem(9, GUIButton.PREVIOUS_PAGE);
        getInventory().setItem(13, pages());
        getInventory().setItem(17, GUIButton.NEXT_PAGE);
    }

    @Override
    public void update() {
        getInventory().setItem(13, pages());
        for (int i = getStartSlot(); i < getEndSlot(); i++) {
            getInventory().setItem(i, null);
        }
        for (int i = 0; i < getSize(); i++) {
            if (i >= Competitions.getPlayerData().size()) {
                break;
            }
            getInventory().setItem(i + getStartSlot(), icon(Competitions.getPlayerData().get(i + getPage() * getSize()).getUUID()));
        }
    }

    @Override
    public void onClick(@NotNull InventoryClickEvent event, Player p, @NotNull ItemStack item) {
        PlayerListGUI gui = (PlayerListGUI) GUIManager.GUI_MAP.get(p);
        if (GUIButton.isSameButton(item, "player_icon")) {
            SkullMeta meta = (SkullMeta) item.getItemMeta();
            new PlayerProfileGUI(Competitions.getPlayerData(Objects.requireNonNull(meta.getOwningPlayer()).getUniqueId())).openTo(p);
            return;
        }
        if (GUIButton.isSameButton(item, GUIButton.NEXT_PAGE)) {
            p.playSound(p, Sound.ITEM_BOOK_PAGE_TURN, 1f, 1f);
            gui.nextPage();
        } else if (GUIButton.isSameButton(item, GUIButton.PREVIOUS_PAGE)) {
            p.playSound(p, Sound.ITEM_BOOK_PAGE_TURN, 1f, 1f);
            gui.previousPage();
        }
    }

    public static void updateGUI() {
        for (IPluginGUI gui : GUIManager.GUI_MAP.values()) {
            if (gui instanceof PlayerListGUI) {
                gui.update();
            }
        }
    }

    private @NotNull ItemStack pages() {
        ItemStack stack = new ItemStack(Material.YELLOW_STAINED_GLASS_PANE);
        stack.editMeta(meta -> meta.displayName(Component.translatable("book.pageIndicator").args(Component.text(getPage() + 1), Component.text(getMaxPage())).decoration(TextDecoration.ITALIC, false)));
        return stack;
    }

    private @NotNull ItemStack icon(UUID uuid) {
        ItemStack icon = new ItemStack(Material.PLAYER_HEAD);
        icon.editMeta(SkullMeta.class, meta -> {
            OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
            Component online = Translation.translatable("player." + (player.isOnline() ? "online" : "offline"));
            meta.displayName(Component.translatable(Objects.requireNonNull(player.getName()) + " (%s)").args(online).decoration(TextDecoration.ITALIC, false));
            meta.setOwningPlayer(player);
            List<Component> lore = new ArrayList<>();
            lore.add(Translation.translatable("player.number").args(Component.text(Competitions.getPlayerData(uuid).getNumber())).decoration(TextDecoration.ITALIC, false).color(NamedTextColor.YELLOW));
            lore.add(Translation.translatable("player.score").args(Component.text(Competitions.getPlayerData(uuid).getScore())).decoration(TextDecoration.ITALIC, false).color(NamedTextColor.YELLOW));
            lore.add(Component.text(""));
            lore.add(Translation.translatable("gui.player_profile.detail").args(Component.text(player.getName())).decoration(TextDecoration.ITALIC, false).color(NamedTextColor.YELLOW));
            meta.lore(lore);
            meta.getPersistentDataContainer().set(SportsDay.ITEM_ID, PersistentDataType.STRING, "player_icon");
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
