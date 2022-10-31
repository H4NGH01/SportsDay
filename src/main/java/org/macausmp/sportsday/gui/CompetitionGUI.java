package org.macausmp.sportsday.gui;

import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public abstract class CompetitionGUI {
    public static final List<AbstractGUI> REGISTERED_GUI = new ArrayList<>();
    public static final HashMap<Player, AbstractGUI> GUI_MAP = new HashMap<>();
    public static final AbstractGUI MENU_GUI = register(new MenuGUI());
    public static final AbstractGUI COMPETITION_INFO_GUI = register(new CompetitionInfoGUI());
    public static final AbstractGUI PLAYER_LIST_GUI = register(new PlayerListGUI());
    public static final AbstractGUI COMPETITION_START_GUI = register(new CompetitionStartGUI());
    public static final AbstractGUI COMPETITION_SETTINGS_GUI = register(new CompetitionSettingsGUI());

    private static <T extends AbstractGUI> T register(T gui) {
        REGISTERED_GUI.add(gui);
        return gui;
    }
}
