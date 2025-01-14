package org.macausmp.sportsday.gui;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.util.ItemUtil;

import java.util.Arrays;
import java.util.function.BiFunction;

/**
 * Represents a filter for page box.
 *
 * @param <T> type of entries
 */
public class Filter<T> {
    private final String[] contents;
    private int counter = 0;
    private final BiFunction<T, Integer, Boolean> function;

    public Filter(String[] contents, BiFunction<T, Integer, Boolean> function) {
        this.contents = contents;
        this.function = function;
    }

    public void next() {
        if (++counter >= contents.length)
            counter = 0;
    }

    public boolean filter(T entry) {
        return function.apply(entry, counter);
    }

    public @NotNull ItemStack filterItem(Material material, String id) {
        Component[] lore = Arrays.stream(contents)
                .map(s -> Component.translatable("- %s")
                        .arguments(Component.translatable(s)).color(NamedTextColor.DARK_GRAY))
                .toArray(Component[]::new);
        lore[counter] = lore[counter].color(NamedTextColor.YELLOW);
        return ItemUtil.item(material, id, "gui.page_box.filter", (Object[]) lore);
    }
}
