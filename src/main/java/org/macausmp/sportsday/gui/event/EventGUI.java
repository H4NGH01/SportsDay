package org.macausmp.sportsday.gui.event;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.SportsDay;
import org.macausmp.sportsday.event.EventStatus;
import org.macausmp.sportsday.event.SportingEvent;
import org.macausmp.sportsday.gui.ButtonHandler;
import org.macausmp.sportsday.gui.ConfirmationGUI;
import org.macausmp.sportsday.gui.PluginGUI;
import org.macausmp.sportsday.gui.admin.AdminMenuGUI;
import org.macausmp.sportsday.util.ItemUtil;

public class EventGUI<T extends SportingEvent> extends PluginGUI {
    private static final ItemStack PAUSE_EVENT = ItemUtil.head(ItemUtil.PAUSE, "pause", "gui.event.pause.title", "gui.event.pause.lore");
    private static final ItemStack UNPAUSE_EVENT = ItemUtil.head(ItemUtil.START, "unpause", "gui.event.unpause.title", "gui.event.unpause.lore");
    private static final ItemStack SAVE_EVENT = ItemUtil.item(Material.CHEST, "save", "gui.event.save.title", "gui.event.save.lore");
    private static final ItemStack TERMINATE_EVENT = ItemUtil.item(Material.RED_CONCRETE, "terminate", "gui.event.terminate.title", "gui.event.terminate.lore");
    protected final T event;

    public EventGUI(@NotNull T event) {
        super(54, Component.translatable("gui.event_console.title").arguments(event.asComponent().color(NamedTextColor.BLACK)));
        this.event = event;
        for (int i = 45; i < 54; i++)
            getInventory().setItem(i, BOARD);
        getInventory().setItem(0, status());
        getInventory().setItem(1, player());
        getInventory().setItem(6, event.isPaused() ? UNPAUSE_EVENT : PAUSE_EVENT);
        getInventory().setItem(7, SAVE_EVENT);
        getInventory().setItem(8, TERMINATE_EVENT);
        getInventory().setItem(53, BACK);
    }

    @Override
    public void update() {
        getInventory().setItem(0, status());
        getInventory().setItem(1, player());
        getInventory().setItem(6, event.isPaused() ? UNPAUSE_EVENT : PAUSE_EVENT);
    }

    public static void updateGUI() {
        PLUGIN.getServer().getOnlinePlayers().stream().map(p -> p.getOpenInventory().getTopInventory())
                .filter(inv -> inv.getHolder() instanceof EventGUI)
                .map(inv -> (EventGUI<? extends SportingEvent>) inv.getHolder())
                .forEach(EventGUI::update);
    }

    private @NotNull ItemStack status() {
        Component display = Component.translatable("competition.current").color(NamedTextColor.GREEN).arguments(event);
        Component lore = Component.translatable("competition.status").color(NamedTextColor.GREEN).arguments(event.getStatus());
        return ItemUtil.item(Material.BEACON, null, display, lore);
    }

    private @NotNull ItemStack player() {
        Component display = Component.translatable("competition.contestants.total").color(NamedTextColor.GREEN)
                .arguments(Component.text(event.getContestants().size()).color(NamedTextColor.YELLOW));
        return ItemUtil.item(Material.PAPER, null, display);
    }

    @ButtonHandler("pause")
    public void pause(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        if (event.getStatus() == EventStatus.CLOSED) {
            p.sendMessage(Component.translatable("command.competition.null_event").color(NamedTextColor.RED));
            p.playSound(EXECUTION_FAIL_SOUND);
            return;
        }
        if (event.getStatus() == EventStatus.ENDED) {
            p.sendMessage(Component.translatable("command.competition.invalid_status").color(NamedTextColor.RED));
            p.playSound(EXECUTION_FAIL_SOUND);
            return;
        }
        p.playSound(event.pause(p) ? UI_BUTTON_CLICK_SOUND : EXECUTION_FAIL_SOUND);
        updateGUI();
    }

    @ButtonHandler("unpause")
    public void unpause(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        if (event.getStatus() == EventStatus.CLOSED) {
            p.sendMessage(Component.translatable("command.competition.null_event").color(NamedTextColor.RED));
            p.playSound(EXECUTION_FAIL_SOUND);
            return;
        }
        if (event.getStatus() == EventStatus.ENDED) {
            p.sendMessage(Component.translatable("command.competition.invalid_status").color(NamedTextColor.RED));
            p.playSound(EXECUTION_FAIL_SOUND);
            return;
        }
        p.playSound(event.unpause(p) ? UI_BUTTON_CLICK_SOUND : EXECUTION_FAIL_SOUND);
        updateGUI();
    }

    @ButtonHandler("save")
    public void save(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        if (event.getStatus() == EventStatus.CLOSED) {
            p.sendMessage(Component.translatable("command.competition.null_event").color(NamedTextColor.RED));
            p.playSound(EXECUTION_FAIL_SOUND);
            return;
        }
        SportsDay.saveEvent(p);
        p.playSound(UI_BUTTON_CLICK_SOUND);
    }

    @ButtonHandler("terminate")
    public void terminate(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        if (event.getStatus() == EventStatus.CLOSED) {
            p.sendMessage(Component.translatable("command.competition.null_event").color(NamedTextColor.RED));
            p.playSound(EXECUTION_FAIL_SOUND);
            return;
        }
        if (event.getStatus() == EventStatus.ENDED) {
            p.sendMessage(Component.translatable("command.competition.invalid_status").color(NamedTextColor.RED));
            p.playSound(EXECUTION_FAIL_SOUND);
            return;
        }
        p.playSound(UI_BUTTON_CLICK_SOUND);
        p.openInventory(new ConfirmationGUI(this, player -> {
            event.terminate(player);
            player.playSound(UI_BUTTON_CLICK_SOUND);
            return true;
        }).getInventory());
    }

    @ButtonHandler("back")
    public void back(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        p.openInventory(new AdminMenuGUI().getInventory());
        p.playSound(UI_BUTTON_CLICK_SOUND);
    }
}
