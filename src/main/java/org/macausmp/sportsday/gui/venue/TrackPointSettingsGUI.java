package org.macausmp.sportsday.gui.venue;

import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.gui.ButtonHandler;
import org.macausmp.sportsday.gui.PermissionRequired;
import org.macausmp.sportsday.gui.PluginGUI;
import org.macausmp.sportsday.util.ItemUtil;
import org.macausmp.sportsday.venue.TrackPoint;

@PermissionRequired
public class TrackPointSettingsGUI extends PluginGUI {
    private final PluginGUI prev;
    private final TrackPoint trackPoint;

    public TrackPointSettingsGUI(@NotNull PluginGUI prev, @NotNull TrackPoint trackPoint, @NotNull String title) {
        super(36, Component.translatable(title));
        this.prev = prev;
        this.trackPoint = trackPoint;
        for (int i = 27; i < 36; i++)
            getInventory().setItem(i, BOARD);
        getInventory().setItem(35, BACK);
        update();
    }

    @Override
    public void update() {
        getInventory().setItem(12, location());
        getInventory().setItem(14, bb());
    }

    public static void updateTrackPoint(TrackPoint point) {
        PLUGIN.getServer().getOnlinePlayers().stream().map(p -> p.getOpenInventory().getTopInventory())
                .filter(inv -> inv.getHolder() instanceof TrackPointSettingsGUI)
                .map(inv -> (TrackPointSettingsGUI) inv.getHolder())
                .filter(gui -> gui.trackPoint == point)
                .forEach(TrackPointSettingsGUI::update);
    }

    private @NotNull ItemStack location() {
        Location loc = trackPoint.getLocation();
        return ItemUtil.item(Material.ENDER_PEARL, "location",
                Component.translatable("gui.venue_settings.track_point.location.title"),
                Component.translatable("gui.venue_settings.track_point.location.lore")
                        .arguments(Component.text(loc.x() + ", " + loc.y() + ", " + loc.z())));
    }

    private @NotNull ItemStack bb() {
        BoundingBox bb = trackPoint.getBoundingBox();
        return ItemUtil.item(Material.SPAWNER, "bb",
                Component.translatable("gui.venue_settings.track_point.bounding_box"),
                Component.translatable("gui.venue_settings.track_point.bounding_box.lore1")
                        .arguments(Component.text(bb.getMinX() + ", " + bb.getMinY() + ", " + bb.getMinZ()).appendNewline()
                                .append(Component.text(bb.getMaxX() + ", " + bb.getMaxY() + ", " + bb.getMaxZ()))),
                Component.translatable("gui.venue_settings.track_point.bounding_box.lore2"),
                Component.translatable("gui.venue_settings.track_point.bounding_box.lore3"));
    }

    @ButtonHandler("location")
    public void location(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        trackPoint.setLocation(p.getLocation());
        updateTrackPoint(trackPoint);
        p.playSound(UI_BUTTON_CLICK_SOUND);
    }

    @ButtonHandler("bb")
    public void bb(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        ClickType type = e.getClick();
        if (!type.isLeftClick() && !type.isRightClick())
            return;
        Location loc = p.getLocation();
        BoundingBox bb = trackPoint.getBoundingBox();
        if (type.isLeftClick()) {
            bb.resize(bb.getMinX(), bb.getMinY(), bb.getMinX(), loc.x(), loc.y(), loc.z());
        } else {
            bb.resize(loc.x(), loc.y(), loc.z(), bb.getMaxX(), bb.getMaxY(), bb.getMaxZ());
        }
        updateTrackPoint(trackPoint);
        p.playSound(UI_BUTTON_CLICK_SOUND);
    }

    @ButtonHandler("back")
    public void back(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        p.openInventory(prev.getInventory());
        p.playSound(UI_BUTTON_CLICK_SOUND);
    }
}
