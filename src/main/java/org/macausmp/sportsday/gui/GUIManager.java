package org.macausmp.sportsday.gui;

import org.macausmp.sportsday.gui.competition.CompetitionInfoGUI;
import org.macausmp.sportsday.gui.competition.CompetitionMenuGUI;
import org.macausmp.sportsday.gui.competition.CompetitionSettingsGUI;
import org.macausmp.sportsday.gui.competition.CompetitionStartGUI;

import java.util.ArrayList;
import java.util.List;

public abstract class GUIManager {
    public static final List<IPluginGUI> REGISTERED_GUI = new ArrayList<>();
    public static final IPluginGUI MENU_GUI = register(new CompetitionMenuGUI());
    public static final IPluginGUI COMPETITION_INFO_GUI = register(new CompetitionInfoGUI());
    public static final IPluginGUI COMPETITION_START_GUI = register(new CompetitionStartGUI());
    public static final IPluginGUI COMPETITION_SETTINGS_GUI = register(new CompetitionSettingsGUI());

    private static <T extends IPluginGUI> T register(T gui) {
        REGISTERED_GUI.add(gui);
        return gui;
    }
}
