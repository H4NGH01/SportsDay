package org.macausmp.sportsday.gui;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractGUI implements IPluginGUI {
    private final Inventory inventory;
    private final Component title;
    private IPluginGUI previous;
    private IPluginGUI next;

    public AbstractGUI(int size, Component title) {
        this.inventory = Bukkit.createInventory(null, size, this.title = title);
        update();
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    @Override
    public Component getTitle() {
        return title;
    }

    @Override
    public IPluginGUI getPreviousPage() {
        return previous;
    }

    public void setPreviousPage(IPluginGUI previous) {
        this.previous = previous;
    }

    @Override
    public IPluginGUI getNextPage() {
        return next;
    }

    public void setNextPage(IPluginGUI next) {
        this.next = next;
    }

    @Override
    public void openTo(@NotNull Player player) {
        if (CompetitionGUI.GUI_MAP.containsKey(player)) {
            setPreviousPage(CompetitionGUI.GUI_MAP.get(player));
        }
        player.openInventory(this.inventory);
        CompetitionGUI.GUI_MAP.put(player, this);
    }
}
