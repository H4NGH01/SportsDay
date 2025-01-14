package org.macausmp.sportsday.gui.admin;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.SportsDay;
import org.macausmp.sportsday.event.SportingEvent;
import org.macausmp.sportsday.gui.ButtonHandler;
import org.macausmp.sportsday.gui.PermissionRequired;
import org.macausmp.sportsday.gui.PluginGUI;
import org.macausmp.sportsday.gui.SportsSelectGUI;
import org.macausmp.sportsday.gui.venue.VenueListGUI;
import org.macausmp.sportsday.util.ItemUtil;

@PermissionRequired
public class AdminMenuGUI extends PluginGUI {
    private static final ItemStack EVENT_START = ItemUtil.head(ItemUtil.START, "event_start", "gui.start.title", "gui.start.lore");
    private static final ItemStack EVENT_CONSOLE = ItemUtil.item(Material.COMMAND_BLOCK, "event_console", "gui.console.title", "gui.console.lore");
    private static final ItemStack SPORTS_SETTINGS = ItemUtil.item(Material.REPEATER, "sports_settings", "gui.settings.title", "gui.settings.lore");
    private static final ItemStack PLAYERS_LIST = ItemUtil.item(Material.PLAYER_HEAD, "players_list", "gui.players_list.title", "gui.players_list.lore");
    private static final ItemStack VENUES_LIST = ItemUtil.item(Material.MAP, "venues_list", "gui.venues_list.title", "gui.venues_list.lore");
    @SuppressWarnings("deprecation")
    private static final ItemStack VERSION = ItemUtil.item(Material.OAK_SIGN, "version", Component.translatable("gui.plugin_version").arguments(Component.text(SportsDay.getInstance().getDescription().getVersion())));

    public AdminMenuGUI() {
        super(45, Component.translatable("gui.menu.title"));
        getInventory().setItem(11, EVENT_START);
        getInventory().setItem(13, EVENT_CONSOLE);
        getInventory().setItem(15, SPORTS_SETTINGS);
        getInventory().setItem(29, PLAYERS_LIST);
        getInventory().setItem(31, VENUES_LIST);
        getInventory().setItem(33, VERSION);
    }

    @ButtonHandler("event_start")
    public void eventStart(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        p.openInventory(new EventStartGUI().getInventory());
        p.playSound(UI_BUTTON_CLICK_SOUND);
    }

    @ButtonHandler("event_console")
    public void eventConsole(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        SportingEvent event = SportsDay.getCurrentEvent();
        if (event == null) {
            return;
        }
        p.openInventory(event.getEventGUI().getInventory());
        p.playSound(UI_BUTTON_CLICK_SOUND);
    }

    @ButtonHandler("sports_settings")
    public void sportsSettings(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        p.openInventory(new SportsSelectGUI(this,
                sport -> p.openInventory(sport.getSettingGUI().getInventory())).getInventory());
        p.playSound(UI_BUTTON_CLICK_SOUND);
    }

    @ButtonHandler("players_list")
    public void playersList(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        p.openInventory(new PlayersListGUI().getInventory());
        p.playSound(UI_BUTTON_CLICK_SOUND);
    }

    @ButtonHandler("venues_list")
    public void venuesList(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        p.openInventory(new SportsSelectGUI(this,
                sport -> p.openInventory(new VenueListGUI<>(sport).getInventory())).getInventory());
        p.playSound(UI_BUTTON_CLICK_SOUND);
    }
}
