package org.macausmp.sportsday.gui.venue;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.gui.ButtonHandler;
import org.macausmp.sportsday.gui.PageBox;
import org.macausmp.sportsday.sport.Sport;
import org.macausmp.sportsday.util.ItemUtil;
import org.macausmp.sportsday.venue.Track;
import org.macausmp.sportsday.venue.TrackPoint;

public class TrackSettingsGUI extends VenueSettingsGUI<Track> {
    private final PageBox<TrackPoint> pageBox = new PageBox<>(this, 36, 45, venue::getCheckPoints);

    public TrackSettingsGUI(@NotNull Sport sport, @NotNull Track track) {
        super(54, sport, track);
        for (int i = 27; i < 36; i++)
            getInventory().setItem(i, BOARD);
        getInventory().setItem(48, PREVIOUS_PAGE);
        getInventory().setItem(50, NEXT_PAGE);
        getInventory().setItem(45, add());
        update();
    }

    @Override
    public void update() {
        super.update();
        getInventory().setItem(27, start());
        getInventory().setItem(28, end());
        getInventory().setItem(49, pages());
        pageBox.updatePage(this::checkpoint);
    }

    private @NotNull ItemStack start() {
        return ItemUtil.item(Material.RED_BED, "start",
                Component.translatable("gui.venue_settings.start_point.title"),
                Component.translatable("gui.venue_settings.start_point.lore"));
    }

    private @NotNull ItemStack end() {
        return ItemUtil.item(Material.GOLD_BLOCK, "end",
                Component.translatable("gui.venue_settings.end_point.title"),
                Component.translatable("gui.venue_settings.end_point.lore"));
    }

    private @NotNull ItemStack pages() {
        ItemStack stack = new ItemStack(Material.BEACON);
        stack.editMeta(meta -> meta.displayName(Component.translatable("book.pageIndicator")
                .arguments(Component.text(pageBox.getPage() + 1), Component.text(pageBox.getMaxPage()))
                .decoration(TextDecoration.ITALIC, false)));
        return stack;
    }

    private @NotNull ItemStack checkpoint(TrackPoint trackPoint) {
        return ItemUtil.item(Material.BEACON, "checkpoint",
                Component.translatable("gui.venue_settings.checkpoint.title"),
                Component.translatable("gui.venue_settings.checkpoint.lore1"),
                Component.translatable("gui.venue_settings.checkpoint.lore2"));
    }

    private @NotNull ItemStack add() {
        return ItemUtil.head(ItemUtil.PLUS, "add", Component.translatable("gui.venue_settings.checkpoint.add.title"),
                Component.translatable("gui.venue_settings.checkpoint.add.lore"));
    }

    @ButtonHandler("start")
    public void start(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        p.openInventory(new TrackPointSettingsGUI(this, venue.getStartPoint(), "gui.venue_settings.start_point.title").getInventory());
        p.playSound(UI_BUTTON_CLICK_SOUND);
    }

    @ButtonHandler("end")
    public void end(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        p.openInventory(new TrackPointSettingsGUI(this, venue.getEndPoint(), "gui.venue_settings.end_point.title").getInventory());
        p.playSound(UI_BUTTON_CLICK_SOUND);
    }

    @ButtonHandler("checkpoint")
    public void checkpoint(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        int i = e.getSlot() - 36 + pageBox.getSize() * pageBox.getPage();
        if (e.getClick().isRightClick()) {
            venue.getCheckPoints().remove(i);
            updateGUI();
            p.playSound(UI_BUTTON_CLICK_SOUND);
            return;
        }
        p.openInventory(new TrackPointSettingsGUI(this, venue.getCheckPoints().get(i), "gui.venue_settings.checkpoint.title").getInventory());
        p.playSound(UI_BUTTON_CLICK_SOUND);
    }

    @ButtonHandler("add")
    public void add(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        venue.getCheckPoints().add(new TrackPoint(p.getLocation(), new BoundingBox()));
        updateGUI();
        p.playSound(UI_BUTTON_CLICK_SOUND);
    }

    @ButtonHandler("next_page")
    public void next(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        pageBox.nextPage();
        p.playSound(UI_BUTTON_CLICK_SOUND);
    }

    @ButtonHandler("prev_page")
    public void prev(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        pageBox.previousPage();
        p.playSound(UI_BUTTON_CLICK_SOUND);
    }
}
