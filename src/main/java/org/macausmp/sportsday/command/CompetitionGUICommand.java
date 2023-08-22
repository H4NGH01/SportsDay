package org.macausmp.sportsday.command;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.SportsDay;
import org.macausmp.sportsday.gui.GUIManager;
import org.macausmp.sportsday.util.TextUtil;

import java.util.ArrayList;
import java.util.List;

public class CompetitionGUICommand implements IPluginCommand {
    public static final ItemStack OP_BOOK = book();

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if (sender instanceof Player p) {
            if (args.length != 0 && args[0].equals("book")) {
                p.getInventory().addItem(OP_BOOK);
                return;
            }
            p.openInventory(GUIManager.MENU_GUI.getInventory());
        } else {
            sender.sendMessage(Component.translatable("permissions.requires.player").color(NamedTextColor.RED));
        }
    }

    private static @NotNull ItemStack book() {
        ItemStack book = new ItemStack(Material.BOOK);
        book.editMeta(meta -> {
            meta.displayName(TextUtil.text(Component.translatable("item.op_book")));
            List<Component> lore = new ArrayList<>();
            lore.add(TextUtil.text(Component.translatable("item.op_book_lore")));
            meta.lore(lore);
            meta.getPersistentDataContainer().set(SportsDay.ITEM_ID, PersistentDataType.STRING, "competition_book");
        });
        return book;
    }

    @Override
    public String name() {
        return "competitiongui";
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        List<String> list = new ArrayList<>();
        if (args.length == 1) list.add("book");
        return list;
    }
}
