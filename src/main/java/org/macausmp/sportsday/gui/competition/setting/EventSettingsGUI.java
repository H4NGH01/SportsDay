package org.macausmp.sportsday.gui.competition.setting;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.competition.SportingEvent;
import org.macausmp.sportsday.gui.ButtonHandler;
import org.macausmp.sportsday.gui.competition.AbstractCompetitionGUI;
import org.macausmp.sportsday.util.ItemUtil;

public class EventSettingsGUI<T extends SportingEvent> extends AbstractCompetitionGUI {
    protected final T event;

    public EventSettingsGUI(@NotNull T event) {
        super(27, Component.translatable("gui.event_settings.title").arguments(event.getName().color(NamedTextColor.BLACK)));
        this.event = event;
        for (int i = 0; i < 9; i++)
            getInventory().setItem(i + 9, BOARD);
        getInventory().setItem(0, ItemUtil.setGlint(COMPETITION_CONSOLE));
        getInventory().setItem(1, CONTESTANTS_LIST);
        getInventory().setItem(2, COMPETITION_SETTINGS);
        getInventory().setItem(3, VERSION);
        update();
    }

    @Override
    public void update() {
        getInventory().setItem(18, enable());
        getInventory().setItem(19, amount());
        getInventory().setItem(20, location());
    }

    public static void updateGUI() {
        PLUGIN.getServer().getOnlinePlayers().stream().map(p -> p.getOpenInventory().getTopInventory())
                .filter(inv -> inv.getHolder() instanceof EventSettingsGUI)
                .map(inv -> (EventSettingsGUI<? extends SportingEvent>) inv.getHolder())
                .forEach(EventSettingsGUI::update);
    }

    @ButtonHandler("enable")
    public void enable(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        event.setSetting(SportingEvent.ENABLE, !event.isEnable());
        p.playSound(Sound.sound(Key.key(event.isEnable() ?
                "minecraft:entity.arrow.hit_player" : "minecraft:entity.enderman.teleport"), Sound.Source.MASTER, 1f, 1f));
    }

    @ButtonHandler("amount")
    public void amount(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        if (!e.getClick().isLeftClick() && !e.getClick().isRightClick())
            return;
        int amount = event.getSetting(SportingEvent.LEAST_PLAYERS_REQUIRED) + (e.getClick().isLeftClick() ? 1 : -1);
        if (amount > 0)
            event.setSetting(SportingEvent.LEAST_PLAYERS_REQUIRED, amount);
        p.playSound(Sound.sound(Key.key(e.getClick().isLeftClick() ?
                "minecraft:entity.arrow.hit_player" : "minecraft:entity.enderman.teleport"), Sound.Source.MASTER, 1f, 1f));
    }

    @ButtonHandler("location")
    public void location(@NotNull InventoryClickEvent e, @NotNull Player p, @NotNull ItemStack item) {
        event.setSetting(SportingEvent.LOCATION, p.getLocation());
        p.playSound(Sound.sound(Key.key("minecraft:entity.arrow.hit_player"), Sound.Source.MASTER, 1f, 1f));
    }

    private @NotNull ItemStack enable() {
        return ItemUtil.item(
                event.isEnable() ? Material.LIME_DYE : Material.BARRIER,
                "enable",
                Component.translatable("gui.event_settings.enable").color(NamedTextColor.YELLOW)
                        .arguments(Component.translatable(event.isEnable() ? "gui.enabled" : "gui.disabled")),
                "gui.toggle");
    }

    private @NotNull ItemStack amount() {
        int amount = event.getSetting(SportingEvent.LEAST_PLAYERS_REQUIRED);
        ItemStack stack = ItemUtil.item(
                Material.PLAYER_HEAD,
                "amount",
                Component.translatable("gui.event_settings.amount").arguments(Component.text(amount)).color(NamedTextColor.YELLOW),
                "gui.increase", "gui.decrease");
        stack.setAmount(amount);
        return stack;
    }

    private @NotNull ItemStack location() {
        Location location = event.getSetting(SportingEvent.LOCATION);
        return ItemUtil.item(
                Material.BEACON,
                "location",
                Component.translatable("gui.event_settings.location").color(NamedTextColor.YELLOW),
                Component.text("world=" + location.getWorld()).color(NamedTextColor.YELLOW),
                Component.text("x=" + location.x() + ", y=" + location.y() + ", z=" + location.z()).color(NamedTextColor.YELLOW),
                Component.text("pitch=" + location.getPitch() + ", yaw=" + location.getYaw()).color(NamedTextColor.YELLOW),
                "gui.event_settings.location_lore");
    }
}
