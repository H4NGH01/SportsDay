package org.macausmp.sportsday.gui.competition;

import net.kyori.adventure.text.Component;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.macausmp.sportsday.SportsDay;
import org.macausmp.sportsday.competition.Competitions;
import org.macausmp.sportsday.competition.ICompetition;
import org.macausmp.sportsday.gui.AbstractGUI;
import org.macausmp.sportsday.gui.GUIButton;
import org.macausmp.sportsday.util.SkullTextureUtil;
import org.macausmp.sportsday.util.TextUtil;

public class CompetitionStartGUI extends AbstractGUI {
    public CompetitionStartGUI() {
        super(54, Component.translatable("gui.start.title"));
        for (int i = 0; i < 9; i++) {
            getInventory().setItem(i + 9, GUIButton.BOARD);
        }
        getInventory().setItem(0, GUIButton.COMPETITION_INFO);
        getInventory().setItem(1, GUIButton.PLAYER_LIST);
        getInventory().setItem(2, GUIButton.START_COMPETITION_SELECTED);
        getInventory().setItem(3, GUIButton.END_COMPETITION);
        getInventory().setItem(4, GUIButton.COMPETITION_SETTINGS);
        getInventory().setItem(5, GUIButton.VERSION);
    }

    @Override
    public void update() {
        getInventory().setItem(18, GUIButton.ELYTRA_RACING);
        getInventory().setItem(19, GUIButton.ICE_BOAT_RACING);
        getInventory().setItem(20, GUIButton.JAVELIN_THROW);
        getInventory().setItem(21, GUIButton.OBSTACLE_COURSE);
        getInventory().setItem(22, GUIButton.PARKOUR);
        getInventory().setItem(23, GUIButton.SUMO);
        getInventory().setItem(27, start(Competitions.ELYTRA_RACING));
        getInventory().setItem(28, start(Competitions.ICE_BOAT_RACING));
        getInventory().setItem(29, start(Competitions.JAVELIN_THROW));
        getInventory().setItem(30, start(Competitions.OBSTACLE_COURSE));
        getInventory().setItem(31, start(Competitions.PARKOUR));
        getInventory().setItem(32, start(Competitions.SUMO));
    }

    @Override
    public void onClick(@NotNull InventoryClickEvent e, Player p, @NotNull ItemStack item) {
        if (GUIButton.isSameButton(item, "start_competition")) p.playSound(p, Competitions.start(p, item.getItemMeta().getPersistentDataContainer().get(SportsDay.COMPETITION_ID, PersistentDataType.STRING)) ? Sound.ENTITY_ARROW_HIT_PLAYER : Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f);
    }

    private @NotNull ItemStack start(@NotNull ICompetition competition) {
        @SuppressWarnings("SpellCheckingInspection") ItemStack stack = SkullTextureUtil.getSkull("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMmEzYjhmNjgxZGFhZDhiZjQzNmNhZThkYTNmZTgxMzFmNjJhMTYyYWI4MWFmNjM5YzNlMDY0NGFhNmFiYWMyZiJ9fX0=");
        stack.editMeta(meta -> {
            meta.displayName(TextUtil.text(Component.translatable("gui.start_competition")));
            meta.getPersistentDataContainer().set(SportsDay.ITEM_ID, PersistentDataType.STRING, "start_competition");
            meta.getPersistentDataContainer().set(SportsDay.COMPETITION_ID, PersistentDataType.STRING, competition.getID());
        });
        return stack;
    }
}
